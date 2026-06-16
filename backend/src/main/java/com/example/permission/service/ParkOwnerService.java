
package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.*;
import com.example.permission.mapper.*;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import org.springframework.web.multipart.MultipartFile;

import static com.example.permission.entity.table.ParkOwnerTableDef.PARK_OWNER;
import static com.example.permission.entity.table.ParkOwnerPropertyTableDef.PARK_OWNER_PROPERTY;
import static com.example.permission.entity.table.ParkOwnerAuditLogTableDef.PARK_OWNER_AUDIT_LOG;
import static com.example.permission.entity.table.ParkPropertyTableDef.PARK_PROPERTY;
import static com.example.permission.entity.table.ParkBuildingTableDef.PARK_BUILDING;
import static com.example.permission.entity.table.ParkFloorTableDef.PARK_FLOOR;

@Service
public class ParkOwnerService {

    @Autowired
    private ParkOwnerMapper parkOwnerMapper;

    @Autowired
    private ParkOwnerPropertyMapper parkOwnerPropertyMapper;

    @Autowired
    private ParkOwnerAuditLogMapper parkOwnerAuditLogMapper;

    @Autowired
    private ParkPropertyMapper parkPropertyMapper;

    @Autowired
    private ParkBuildingMapper parkBuildingMapper;

    @Autowired
    private ParkFloorMapper parkFloorMapper;

    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    public PageResult<ParkOwner> pageList(Integer pageNum, Integer pageSize, String keyword,
                                          Integer ownerType, Integer occupancyStatus, Integer authStatus,
                                          String ownerTags, Integer minPropertyCount, Integer maxPropertyCount,
                                          LocalDate moveInStartDate, LocalDate moveInEndDate,
                                          String sortField, String sortOrder) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkOwner.class)
                .where(PARK_OWNER.DELETED.eq(0));

        if (keyword != null && !keyword.isEmpty()) {
            query.and(PARK_OWNER.NAME.like(keyword)
                    .or(PARK_OWNER.PHONE.like(keyword))
                    .or(PARK_OWNER.ID_CARD.like(keyword)));
        }
        if (ownerType != null) {
            query.and(PARK_OWNER.OWNER_TYPE.eq(ownerType));
        }
        if (occupancyStatus != null) {
            query.and(PARK_OWNER.OCCUPANCY_STATUS.eq(occupancyStatus));
        }
        if (authStatus != null) {
            query.and(PARK_OWNER.AUTH_STATUS.eq(authStatus));
        }
        if (ownerTags != null && !ownerTags.isEmpty()) {
            query.and(PARK_OWNER.OWNER_TAGS.like(ownerTags));
        }

        if (sortField != null && !sortField.isEmpty()) {
            switch (sortField) {
                case "ownerCode":
                    query.orderBy("asc".equals(sortOrder) ? PARK_OWNER.OWNER_CODE.asc() : PARK_OWNER.OWNER_CODE.desc());
                    break;
                case "name":
                    query.orderBy("asc".equals(sortOrder) ? PARK_OWNER.NAME.asc() : PARK_OWNER.NAME.desc());
                    break;
                default:
                    query.orderBy(PARK_OWNER.CREATE_TIME.desc());
            }
        } else {
            query.orderBy(PARK_OWNER.OWNER_CODE.asc());
        }

        Page<ParkOwner> page = parkOwnerMapper.paginate(Page.of(pageNum, pageSize), query);

        List<ParkOwner> list = page.getRecords();
        List<ParkOwner> filteredList = new ArrayList<>();
        for (ParkOwner owner : list) {
            enrichOwner(owner);
            int propCount = getOwnerPropertyCount(owner.getId());
            owner.setPropertyCount(propCount);

            boolean matchCount = true;
            if (minPropertyCount != null && propCount < minPropertyCount) {
                matchCount = false;
            }
            if (maxPropertyCount != null && propCount > maxPropertyCount) {
                matchCount = false;
            }
            if (matchCount) {
                filteredList.add(owner);
            }

            if (owner.getOwnerTags() != null && !owner.getOwnerTags().isEmpty()) {
                owner.setOwnerTagList(Arrays.asList(owner.getOwnerTags().split(",")));
            }
        }

        if ("propertyCount".equals(sortField)) {
            final boolean asc = "asc".equals(sortOrder);
            filteredList.sort((o1, o2) -> asc ?
                    Integer.compare(o1.getPropertyCount(), o2.getPropertyCount()) :
                    Integer.compare(o2.getPropertyCount(), o1.getPropertyCount()));
        }

        return new PageResult<>(page.getTotalRow(), filteredList,
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public ParkOwner getById(Long id) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkOwner.class)
                .where(PARK_OWNER.ID.eq(id))
                .and(PARK_OWNER.DELETED.eq(0));
        ParkOwner owner = parkOwnerMapper.selectOneByQuery(query);
        if (owner != null) {
            enrichOwner(owner);
            if (owner.getOwnerTags() != null && !owner.getOwnerTags().isEmpty()) {
                owner.setOwnerTagList(Arrays.asList(owner.getOwnerTags().split(",")));
            }
            owner.setPropertyList(getOwnerProperties(id));
            owner.setAuditLogs(getAuditLogs(id));
            owner.setPropertyCount(owner.getPropertyList().size());
        }
        return owner;
    }

    public ParkOwner getByCode(String ownerCode) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkOwner.class)
                .where(PARK_OWNER.OWNER_CODE.eq(ownerCode))
                .and(PARK_OWNER.DELETED.eq(0));
        return parkOwnerMapper.selectOneByQuery(query);
    }

    public ParkOwner getByIdCard(String idCard) {
        if (idCard == null || idCard.isEmpty()) return null;
        QueryWrapper query = QueryWrapper.create()
                .from(ParkOwner.class)
                .where(PARK_OWNER.ID_CARD.eq(idCard))
                .and(PARK_OWNER.DELETED.eq(0));
        return parkOwnerMapper.selectOneByQuery(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(ParkOwner owner, List<ParkOwnerProperty> propertyList, Long operatorId, String operatorName) {
        validateOwner(owner, null);

        if (owner.getOwnerType() == 2 && (owner.getBusinessLicenseUrl() == null || owner.getBusinessLicenseUrl().isEmpty())) {
            throw new BusinessException("企业业主必须上传营业执照");
        }

        owner.setOwnerCode(generateOwnerCode());
        owner.setDeleted(0);
        owner.setAuthStatus(owner.getAuthStatus() != null ? owner.getAuthStatus() : 1);
        owner.setOccupancyStatus(owner.getOccupancyStatus() != null ? owner.getOccupancyStatus() : 2);

        if (owner.getBirthDate() != null) {
            owner.setAge(Period.between(owner.getBirthDate(), LocalDate.now()).getYears());
        }

        owner.setCreateTime(LocalDateTime.now());
        owner.setUpdateTime(LocalDateTime.now());
        parkOwnerMapper.insert(owner);

        if (propertyList != null && !propertyList.isEmpty()) {
            for (ParkOwnerProperty op : propertyList) {
                bindProperty(owner.getId(), owner.getOwnerCode(), op, operatorId, operatorName);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(ParkOwner owner, boolean canEditContactOnly) {
        ParkOwner existOwner = parkOwnerMapper.selectOneById(owner.getId());
        if (existOwner == null) {
            throw new BusinessException("业主不存在");
        }

        if (canEditContactOnly) {
            owner.setName(null);
            owner.setGender(null);
            owner.setIdCard(null);
            owner.setBirthDate(null);
            owner.setNation(null);
            owner.setMaritalStatus(null);
            owner.setBackupPhone(null);
            owner.setEmail(null);
            owner.setWechat(null);
            owner.setHouseholdAddress(null);
            owner.setCurrentAddress(null);
            owner.setWorkUnit(null);
            owner.setOccupation(null);
            owner.setFamilyCount(null);
            owner.setEmergencyContact(null);
            owner.setEmergencyRelation(null);
            owner.setEmergencyPhone(null);
            owner.setOccupancyStatus(null);
            owner.setOwnerType(null);
            owner.setEnterpriseCreditCode(null);
            owner.setEnterpriseType(null);
            owner.setLegalPersonName(null);
            owner.setLegalPersonIdCard(null);
            owner.setContactPerson(null);
            owner.setContactPosition(null);
            owner.setContactPhone(null);
            owner.setEnterpriseAddress(null);
            owner.setRegisteredCapital(null);
            owner.setBusinessLicenseUrl(null);
        }

        validateOwner(owner, existOwner.getId());

        owner.setOwnerCode(null);
        owner.setOwnerType(null);
        owner.setAuthStatus(null);

        if (owner.getBirthDate() != null) {
            owner.setAge(Period.between(owner.getBirthDate(), LocalDate.now()).getYears());
        }

        owner.setUpdateTime(LocalDateTime.now());
        parkOwnerMapper.update(owner);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ParkOwner owner = parkOwnerMapper.selectOneById(id);
        if (owner == null) {
            throw new BusinessException("业主不存在");
        }

        QueryWrapper query = QueryWrapper.create()
                .from(ParkOwnerProperty.class)
                .where(PARK_OWNER_PROPERTY.OWNER_ID.eq(id))
                .and(PARK_OWNER_PROPERTY.RELATION_STATUS.eq(1));
        Long count = parkOwnerPropertyMapper.selectCountByQuery(query);
        if (count != null && count > 0) {
            throw new BusinessException("该业主还有关联房产，请先解除关联后再删除");
        }

        owner.setDeleted(1);
        owner.setUpdateTime(LocalDateTime.now());
        parkOwnerMapper.update(owner);
    }

    @Transactional(rollbackFor = Exception.class)
    public void audit(Long ownerId, Integer auditResult, String auditOpinion, String rejectReason,
                      Long operatorId, String operatorName) {
        ParkOwner owner = parkOwnerMapper.selectOneById(ownerId);
        if (owner == null) {
            throw new BusinessException("业主不存在");
        }

        if (auditResult != 2 && auditResult != 3) {
            throw new BusinessException("无效的审核结果");
        }

        if (auditResult == 3 && (rejectReason == null || rejectReason.isEmpty())) {
            throw new BusinessException("审核拒绝时必须填写拒绝原因");
        }

        owner.setAuthStatus(auditResult);
        if (auditResult == 3) {
            owner.setAuthRejectReason(rejectReason);
        } else {
            owner.setAuthRejectReason(null);
        }
        owner.setUpdateTime(LocalDateTime.now());
        parkOwnerMapper.update(owner);

        ParkOwnerAuditLog log = new ParkOwnerAuditLog();
        log.setOwnerId(ownerId);
        log.setOwnerCode(owner.getOwnerCode());
        log.setOwnerName(owner.getName());
        log.setAuditResult(auditResult);
        log.setAuditOpinion(auditOpinion);
        log.setRejectReason(rejectReason);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setCreateTime(LocalDateTime.now());
        parkOwnerAuditLogMapper.insert(log);
    }

    @Transactional(rollbackFor = Exception.class)
    public void bindProperty(Long ownerId, String ownerCode, ParkOwnerProperty propertyRelation,
                             Long operatorId, String operatorName) {
        ParkOwner owner = parkOwnerMapper.selectOneById(ownerId);
        if (owner == null) {
            throw new BusinessException("业主不存在");
        }

        ParkProperty property = parkPropertyMapper.selectOneById(propertyRelation.getPropertyId());
        if (property == null) {
            throw new BusinessException("房产不存在");
        }

        QueryWrapper existQuery = QueryWrapper.create()
                .from(ParkOwnerProperty.class)
                .where(PARK_OWNER_PROPERTY.OWNER_ID.eq(ownerId))
                .and(PARK_OWNER_PROPERTY.PROPERTY_ID.eq(propertyRelation.getPropertyId()))
                .and(PARK_OWNER_PROPERTY.RELATION_STATUS.eq(1));
        Long existCount = parkOwnerPropertyMapper.selectCountByQuery(existQuery);
        if (existCount != null && existCount > 0) {
            throw new BusinessException("该房产已与此业主关联");
        }

        if (propertyRelation.getPropertyRightType() == 2) {
            if (propertyRelation.getPropertyRightRatio() == null) {
                throw new BusinessException("共有产权时必须设置产权比例");
            }
            QueryWrapper coOwnerQuery = QueryWrapper.create()
                    .from(ParkOwnerProperty.class)
                    .where(PARK_OWNER_PROPERTY.PROPERTY_ID.eq(propertyRelation.getPropertyId()))
                    .and(PARK_OWNER_PROPERTY.RELATION_STATUS.eq(1))
                    .and(PARK_OWNER_PROPERTY.PROPERTY_RIGHT_TYPE.eq(2));
            List<ParkOwnerProperty> coOwners = parkOwnerPropertyMapper.selectListByQuery(coOwnerQuery);
            BigDecimal totalRatio = propertyRelation.getPropertyRightRatio();
            for (ParkOwnerProperty co : coOwners) {
                if (co.getPropertyRightRatio() != null) {
                    totalRatio = totalRatio.add(co.getPropertyRightRatio());
                }
            }
            if (totalRatio.compareTo(new BigDecimal(100)) > 0) {
                throw new BusinessException("共有产权比例之和不能超过100%");
            }
        } else {
            propertyRelation.setPropertyRightRatio(new BigDecimal(100));
        }

        propertyRelation.setOwnerId(ownerId);
        propertyRelation.setOwnerCode(ownerCode);
        propertyRelation.setPropertyCode(property.getPropertyCode());
        propertyRelation.setRelationStatus(1);
        propertyRelation.setCreateTime(LocalDateTime.now());
        propertyRelation.setUpdateTime(LocalDateTime.now());
        parkOwnerPropertyMapper.insert(propertyRelation);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unbindProperty(Long relationId, String unbindReason, Long operatorId, String operatorName) {
        ParkOwnerProperty relation = parkOwnerPropertyMapper.selectOneById(relationId);
        if (relation == null) {
            throw new BusinessException("关联记录不存在");
        }
        if (relation.getRelationStatus() != 1) {
            throw new BusinessException("该关联已解除或冻结");
        }

        relation.setRelationStatus(2);
        relation.setUnbindTime(LocalDateTime.now());
        relation.setUnbindOperatorId(operatorId);
        relation.setUnbindOperatorName(operatorName);
        relation.setUnbindReason(unbindReason);
        relation.setUpdateTime(LocalDateTime.now());
        parkOwnerPropertyMapper.update(relation);
    }

    public List<ParkOwnerProperty> getOwnerProperties(Long ownerId) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkOwnerProperty.class)
                .leftJoin(ParkProperty.class).on(PARK_PROPERTY.ID.eq(PARK_OWNER_PROPERTY.PROPERTY_ID))
                .leftJoin(ParkBuilding.class).on(PARK_BUILDING.ID.eq(PARK_PROPERTY.BUILDING_ID))
                .leftJoin(ParkFloor.class).on(PARK_FLOOR.ID.eq(PARK_PROPERTY.FLOOR_ID))
                .where(PARK_OWNER_PROPERTY.OWNER_ID.eq(ownerId))
                .orderBy(PARK_OWNER_PROPERTY.CREATE_TIME.desc());

        List<ParkOwnerProperty> list = parkOwnerPropertyMapper.selectListByQuery(query);
        for (ParkOwnerProperty op : list) {
            enrichOwnerProperty(op);
        }
        return list;
    }

    public List<ParkOwnerAuditLog> getAuditLogs(Long ownerId) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkOwnerAuditLog.class)
                .where(PARK_OWNER_AUDIT_LOG.OWNER_ID.eq(ownerId))
                .orderBy(PARK_OWNER_AUDIT_LOG.CREATE_TIME.desc());
        List<ParkOwnerAuditLog> list = parkOwnerAuditLogMapper.selectListByQuery(query);
        for (ParkOwnerAuditLog log : list) {
            log.setAuditResultText(log.getAuditResult() == 2 ? "认证通过" : "认证失败");
        }
        return list;
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();

        Long totalCount = parkOwnerMapper.selectCountByQuery(
                QueryWrapper.create().from(ParkOwner.class).where(PARK_OWNER.DELETED.eq(0))
        );
        result.put("totalCount", totalCount);

        Long personalCount = countByOwnerType(1);
        Long enterpriseCount = countByOwnerType(2);
        Long investmentCount = countByOwnerType(3);
        result.put("personalCount", personalCount);
        result.put("enterpriseCount", enterpriseCount);
        result.put("investmentCount", investmentCount);

        Long authPassedCount = countByAuthStatus(2);
        Long authPendingCount = countByAuthStatus(1);
        Long authRejectedCount = countByAuthStatus(3);
        result.put("authPassedCount", authPassedCount);
        result.put("authPendingCount", authPendingCount);
        result.put("authRejectedCount", authRejectedCount);
        result.put("authRate", totalCount > 0 ? authPassedCount * 100.0 / totalCount : 0);

        Long occupiedCount = countByOccupancyStatus(1);
        Long notOccupiedCount = countByOccupancyStatus(2);
        Long rentedCount = countByOccupancyStatus(3);
        result.put("occupiedCount", occupiedCount);
        result.put("notOccupiedCount", notOccupiedCount);
        result.put("rentedCount", rentedCount);
        result.put("occupancyRate", totalCount > 0 ? occupiedCount * 100.0 / totalCount : 0);

        Map<String, Long> propertyHolding = new LinkedHashMap<>();
        propertyHolding.put("1套", countByPropertyCount(1, 1));
        propertyHolding.put("2套", countByPropertyCount(2, 2));
        propertyHolding.put("3套及以上", countByPropertyCount(3, null));
        result.put("propertyHolding", propertyHolding);

        Long multiPropertyCount = countByPropertyCount(2, null);
        result.put("avgPropertyCount", totalCount > 0 ? getTotalPropertyCount() * 1.0 / totalCount : 0);
        result.put("multiPropertyRate", totalCount > 0 ? multiPropertyCount * 100.0 / totalCount : 0);

        Map<String, Long> typeDistribution = new LinkedHashMap<>();
        typeDistribution.put("个人业主", personalCount);
        typeDistribution.put("企业业主", enterpriseCount);
        typeDistribution.put("投资业主", investmentCount);
        result.put("typeDistribution", typeDistribution);

        Map<String, Long> authDistribution = new LinkedHashMap<>();
        authDistribution.put("已认证", authPassedCount);
        authDistribution.put("未认证", authPendingCount);
        authDistribution.put("认证失败", authRejectedCount);
        result.put("authDistribution", authDistribution);

        Map<String, Long> occupancyDistribution = new LinkedHashMap<>();
        occupancyDistribution.put("已入住", occupiedCount);
        occupancyDistribution.put("未入住", notOccupiedCount);
        occupancyDistribution.put("委托出租", rentedCount);
        result.put("occupancyDistribution", occupancyDistribution);

        result.put("moveInTrend", getMoveInTrend());
        result.put("tagStatistics", getTagStatistics());

        return result;
    }

    public ExcelWriter getTemplateWriter() {
        ExcelWriter writer = ExcelUtil.getWriter();

        writer.addHeaderAlias("ownerType", "业主类型");
        writer.addHeaderAlias("name", "姓名/企业名称");
        writer.addHeaderAlias("gender", "性别");
        writer.addHeaderAlias("idCard", "身份证号");
        writer.addHeaderAlias("birthDate", "出生日期(yyyy-MM-dd)");
        writer.addHeaderAlias("nation", "民族");
        writer.addHeaderAlias("maritalStatus", "婚姻状况");
        writer.addHeaderAlias("phone", "手机号");
        writer.addHeaderAlias("backupPhone", "备用手机");
        writer.addHeaderAlias("email", "电子邮箱");
        writer.addHeaderAlias("wechat", "微信号");
        writer.addHeaderAlias("householdAddress", "户籍地址");
        writer.addHeaderAlias("currentAddress", "现居住地址");
        writer.addHeaderAlias("workUnit", "工作单位");
        writer.addHeaderAlias("occupation", "职业");
        writer.addHeaderAlias("familyCount", "家庭人口数");
        writer.addHeaderAlias("emergencyContact", "紧急联系人");
        writer.addHeaderAlias("emergencyRelation", "联系人关系");
        writer.addHeaderAlias("emergencyPhone", "联系人电话");
        writer.addHeaderAlias("ownerTags", "业主标签(逗号分隔)");
        writer.addHeaderAlias("remark", "备注");
        writer.addHeaderAlias("enterpriseCreditCode", "统一社会信用代码");
        writer.addHeaderAlias("enterpriseType", "企业类型");
        writer.addHeaderAlias("legalPersonName", "法定代表人");
        writer.addHeaderAlias("legalPersonIdCard", "法人身份证号");
        writer.addHeaderAlias("contactPerson", "联系人");
        writer.addHeaderAlias("contactPosition", "联系人职务");
        writer.addHeaderAlias("contactPhone", "联系人电话");
        writer.addHeaderAlias("enterpriseAddress", "企业地址");
        writer.addHeaderAlias("registeredCapital", "注册资本(万元)");

        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("ownerType", "个人业主");
        example.put("name", "张三");
        example.put("gender", "男");
        example.put("idCard", "110101199001011234");
        example.put("birthDate", "1990-01-01");
        example.put("nation", "汉族");
        example.put("maritalStatus", "已婚");
        example.put("phone", "13800138001");
        example.put("backupPhone", "13900139001");
        example.put("email", "zhangsan@example.com");
        example.put("wechat", "zhangsan_wx");
        example.put("householdAddress", "北京市朝阳区");
        example.put("currentAddress", "北京市朝阳区阳光花园1-101");
        example.put("workUnit", "某科技公司");
        example.put("occupation", "工程师");
        example.put("familyCount", 3);
        example.put("emergencyContact", "李四");
        example.put("emergencyRelation", "配偶");
        example.put("emergencyPhone", "13800138002");
        example.put("ownerTags", "VIP业主,友好邻居");
        example.put("remark", "示例数据");
        example.put("enterpriseCreditCode", "");
        example.put("enterpriseType", "");
        example.put("legalPersonName", "");
        example.put("legalPersonIdCard", "");
        example.put("contactPerson", "");
        example.put("contactPosition", "");
        example.put("contactPhone", "");
        example.put("enterpriseAddress", "");
        example.put("registeredCapital", "");
        rows.add(example);

        writer.write(rows, true);
        return writer;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importOwners(MultipartFile file, Long operatorId, String operatorName) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<Map<String, String>> failList = new ArrayList<>();
        Set<String> idCardSet = new HashSet<>();
        Set<String> phoneSet = new HashSet<>();

        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
            List<List<Object>> rows = reader.read();

            if (rows.size() <= 1) {
                throw new BusinessException("Excel文件为空或只有表头");
            }

            for (int i = 1; i < rows.size(); i++) {
                List<Object> row = rows.get(i);
                int rowNum = i + 1;
                try {
                    String ownerTypeStr = getCellString(row, 0);
                    String name = getCellString(row, 1);
                    String genderStr = getCellString(row, 2);
                    String idCard = getCellString(row, 3);
                    String birthDateStr = getCellString(row, 4);
                    String nation = getCellString(row, 5);
                    String maritalStatusStr = getCellString(row, 6);
                    String phone = getCellString(row, 7);
                    String backupPhone = getCellString(row, 8);
                    String email = getCellString(row, 9);
                    String wechat = getCellString(row, 10);
                    String householdAddress = getCellString(row, 11);
                    String currentAddress = getCellString(row, 12);
                    String workUnit = getCellString(row, 13);
                    String occupation = getCellString(row, 14);
                    String familyCountStr = getCellString(row, 15);
                    String emergencyContact = getCellString(row, 16);
                    String emergencyRelation = getCellString(row, 17);
                    String emergencyPhone = getCellString(row, 18);
                    String ownerTags = getCellString(row, 19);
                    String remark = getCellString(row, 20);
                    String enterpriseCreditCode = getCellString(row, 21);
                    String enterpriseType = getCellString(row, 22);
                    String legalPersonName = getCellString(row, 23);
                    String legalPersonIdCard = getCellString(row, 24);
                    String contactPerson = getCellString(row, 25);
                    String contactPosition = getCellString(row, 26);
                    String contactPhone = getCellString(row, 27);
                    String enterpriseAddress = getCellString(row, 28);
                    String registeredCapitalStr = getCellString(row, 29);

                    if (name == null || name.isEmpty()) {
                        throw new BusinessException("姓名不能为空");
                    }
                    if (phone == null || phone.isEmpty()) {
                        throw new BusinessException("手机号不能为空");
                    }
                    if (!PHONE_PATTERN.matcher(phone).matches()) {
                        throw new BusinessException("手机号格式错误，必须为11位有效手机号");
                    }
                    if (idCard == null || idCard.isEmpty()) {
                        throw new BusinessException("身份证号不能为空");
                    }
                    if (!ID_CARD_PATTERN.matcher(idCard).matches()) {
                        throw new BusinessException("身份证号格式错误，必须为18位有效身份证号");
                    }
                    if (idCardSet.contains(idCard)) {
                        throw new BusinessException("身份证号在导入文件中重复");
                    }
                    if (phoneSet.contains(phone)) {
                        throw new BusinessException("手机号在导入文件中重复");
                    }

                    ParkOwner existByIdCard = getByIdCard(idCard);
                    if (existByIdCard != null) {
                        throw new BusinessException("身份证号已存在");
                    }
                    QueryWrapper phoneQuery = QueryWrapper.create()
                            .from(ParkOwner.class)
                            .where(PARK_OWNER.PHONE.eq(phone))
                            .and(PARK_OWNER.DELETED.eq(0));
                    Long phoneCount = parkOwnerMapper.selectCountByQuery(phoneQuery);
                    if (phoneCount != null && phoneCount > 0) {
                        throw new BusinessException("手机号已存在");
                    }

                    idCardSet.add(idCard);
                    phoneSet.add(phone);

                    Integer ownerType = 1;
                    if ("个人业主".equals(ownerTypeStr)) ownerType = 1;
                    else if ("企业业主".equals(ownerTypeStr)) ownerType = 2;
                    else if ("投资业主".equals(ownerTypeStr)) ownerType = 3;

                    Integer gender = null;
                    if ("男".equals(genderStr)) gender = 1;
                    else if ("女".equals(genderStr)) gender = 2;

                    Integer maritalStatus = null;
                    if ("未婚".equals(maritalStatusStr)) maritalStatus = 1;
                    else if ("已婚".equals(maritalStatusStr)) maritalStatus = 2;
                    else if ("离异".equals(maritalStatusStr)) maritalStatus = 3;
                    else if ("丧偶".equals(maritalStatusStr)) maritalStatus = 4;

                    ParkOwner owner = new ParkOwner();
                    owner.setOwnerType(ownerType);
                    owner.setName(name);
                    owner.setGender(gender);
                    owner.setIdCard(idCard);
                    owner.setNation(nation);
                    owner.setMaritalStatus(maritalStatus);
                    owner.setPhone(phone);
                    owner.setBackupPhone(backupPhone);
                    owner.setEmail(email);
                    owner.setWechat(wechat);
                    owner.setHouseholdAddress(householdAddress);
                    owner.setCurrentAddress(currentAddress);
                    owner.setWorkUnit(workUnit);
                    owner.setOccupation(occupation);
                    owner.setEmergencyContact(emergencyContact);
                    owner.setEmergencyRelation(emergencyRelation);
                    owner.setEmergencyPhone(emergencyPhone);
                    owner.setOwnerTags(ownerTags);
                    owner.setRemark(remark);
                    owner.setEnterpriseCreditCode(enterpriseCreditCode);
                    owner.setEnterpriseType(enterpriseType);
                    owner.setLegalPersonName(legalPersonName);
                    owner.setLegalPersonIdCard(legalPersonIdCard);
                    owner.setContactPerson(contactPerson);
                    owner.setContactPosition(contactPosition);
                    owner.setContactPhone(contactPhone);
                    owner.setEnterpriseAddress(enterpriseAddress);
                    owner.setAuthStatus(1);
                    owner.setOccupancyStatus(2);
                    owner.setDeleted(0);

                    if (birthDateStr != null && !birthDateStr.isEmpty()) {
                        try {
                            owner.setBirthDate(LocalDate.parse(birthDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        } catch (Exception e) {
                            throw new BusinessException("出生日期格式错误");
                        }
                    }
                    if (familyCountStr != null && !familyCountStr.isEmpty()) {
                        owner.setFamilyCount(Integer.parseInt(familyCountStr));
                    }
                    if (registeredCapitalStr != null && !registeredCapitalStr.isEmpty()) {
                        owner.setRegisteredCapital(new BigDecimal(registeredCapitalStr));
                    }

                    add(owner, null, operatorId, operatorName);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    Map<String, String> failItem = new HashMap<>();
                    failItem.put("rowNum", String.valueOf(rowNum));
                    failItem.put("name", getCellString(row, 1));
                    failItem.put("idCard", getCellString(row, 3));
                    failItem.put("reason", e.getMessage());
                    failList.add(failItem);
                }
            }

            reader.close();
        } catch (Exception e) {
            throw new BusinessException("导入失败: " + e.getMessage());
        }

        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failList", failList);
        return result;
    }

    public List<ParkOwner> listAllForExport(String keyword, Integer ownerType, Integer occupancyStatus,
                                            Integer authStatus, String ownerTags) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkOwner.class)
                .where(PARK_OWNER.DELETED.eq(0));

        if (keyword != null && !keyword.isEmpty()) {
            query.and(PARK_OWNER.NAME.like(keyword)
                    .or(PARK_OWNER.PHONE.like(keyword))
                    .or(PARK_OWNER.ID_CARD.like(keyword)));
        }
        if (ownerType != null) {
            query.and(PARK_OWNER.OWNER_TYPE.eq(ownerType));
        }
        if (occupancyStatus != null) {
            query.and(PARK_OWNER.OCCUPANCY_STATUS.eq(occupancyStatus));
        }
        if (authStatus != null) {
            query.and(PARK_OWNER.AUTH_STATUS.eq(authStatus));
        }
        if (ownerTags != null && !ownerTags.isEmpty()) {
            query.and(PARK_OWNER.OWNER_TAGS.like(ownerTags));
        }

        query.orderBy(PARK_OWNER.OWNER_CODE.asc());

        List<ParkOwner> list = parkOwnerMapper.selectListByQuery(query);
        for (ParkOwner owner : list) {
            enrichOwner(owner);
            QueryWrapper propQuery = QueryWrapper.create()
                    .from(ParkOwnerProperty.class)
                    .where(PARK_OWNER_PROPERTY.OWNER_ID.eq(owner.getId()))
                    .and(PARK_OWNER_PROPERTY.RELATION_STATUS.eq(1));
            Long propCount = parkOwnerPropertyMapper.selectCountByQuery(propQuery);
            owner.setPropertyCount(propCount != null ? propCount.intValue() : 0);
            if (owner.getOwnerTags() != null && !owner.getOwnerTags().isEmpty()) {
                owner.setOwnerTagList(Arrays.asList(owner.getOwnerTags().split(",")));
            }
        }
        return list;
    }

    private void validateOwner(ParkOwner owner, Long excludeId) {
        if (owner.getName() == null || owner.getName().isEmpty()) {
            throw new BusinessException("姓名不能为空");
        }
        if (owner.getPhone() == null || owner.getPhone().isEmpty()) {
            throw new BusinessException("手机号不能为空");
        }
        if (!PHONE_PATTERN.matcher(owner.getPhone()).matches()) {
            throw new BusinessException("手机号格式错误");
        }
        if (owner.getOwnerType() != null && owner.getOwnerType() == 1) {
            if (owner.getIdCard() != null && !owner.getIdCard().isEmpty()) {
                if (!ID_CARD_PATTERN.matcher(owner.getIdCard()).matches()) {
                    throw new BusinessException("身份证号格式错误");
                }
                ParkOwner exist = getByIdCard(owner.getIdCard());
                if (exist != null && (excludeId == null || !exist.getId().equals(excludeId))) {
                    throw new BusinessException("身份证号已存在");
                }
            }
        }

        if (owner.getIdCard() != null && !owner.getIdCard().isEmpty()) {
            if (!ID_CARD_PATTERN.matcher(owner.getIdCard()).matches()) {
                throw new BusinessException("身份证号格式错误");
            }
            ParkOwner existByIdCard = getByIdCard(owner.getIdCard());
            if (existByIdCard != null && (excludeId == null || !existByIdCard.getId().equals(excludeId))) {
                throw new BusinessException("身份证号已存在");
            }
        }

        QueryWrapper phoneQuery = QueryWrapper.create()
                .from(ParkOwner.class)
                .where(PARK_OWNER.PHONE.eq(owner.getPhone()))
                .and(PARK_OWNER.DELETED.eq(0));
        if (excludeId != null) {
            phoneQuery.and(PARK_OWNER.ID.ne(excludeId));
        }
        Long phoneCount = parkOwnerMapper.selectCountByQuery(phoneQuery);
        if (phoneCount != null && phoneCount > 0) {
            throw new BusinessException("手机号已存在");
        }
    }

    private String generateOwnerCode() {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkOwner.class)
                .orderBy(PARK_OWNER.OWNER_CODE.desc())
                .limit(1);
        ParkOwner last = parkOwnerMapper.selectOneByQuery(query);
        int seq = 1;
        if (last != null && last.getOwnerCode() != null) {
            try {
                seq = Integer.parseInt(last.getOwnerCode().replace("YZ", "")) + 1;
            } catch (Exception ignored) {}
        }
        return String.format("YZ%03d", seq);
    }

    private void enrichOwner(ParkOwner owner) {
        owner.setGenderText(owner.getGender() != null ? (owner.getGender() == 1 ? "男" : "女") : "");
        owner.setOwnerTypeText(owner.getOwnerType() != null ?
                (owner.getOwnerType() == 1 ? "个人业主" :
                        owner.getOwnerType() == 2 ? "企业业主" : "投资业主") : "");
        owner.setOccupancyStatusText(owner.getOccupancyStatus() != null ?
                (owner.getOccupancyStatus() == 1 ? "已入住" :
                        owner.getOccupancyStatus() == 2 ? "未入住" : "委托出租") : "");
        owner.setAuthStatusText(owner.getAuthStatus() != null ?
                (owner.getAuthStatus() == 1 ? "未认证" :
                        owner.getAuthStatus() == 2 ? "已认证" : "认证失败") : "");
        owner.setMaritalStatusText(owner.getMaritalStatus() != null ?
                (owner.getMaritalStatus() == 1 ? "未婚" :
                        owner.getMaritalStatus() == 2 ? "已婚" :
                                owner.getMaritalStatus() == 3 ? "离异" : "丧偶") : "");
    }

    private void enrichOwnerProperty(ParkOwnerProperty op) {
        op.setPropertyRightTypeText(op.getPropertyRightType() != null ?
                (op.getPropertyRightType() == 1 ? "完全产权" :
                        op.getPropertyRightType() == 2 ? "共有产权" : "使用权") : "");
        op.setAcquireTypeText(op.getAcquireType() != null ?
                (op.getAcquireType() == 1 ? "购买" :
                        op.getAcquireType() == 2 ? "继承" :
                                op.getAcquireType() == 3 ? "赠与" : "拆迁") : "");
        op.setRelationStatusText(op.getRelationStatus() != null ?
                (op.getRelationStatus() == 1 ? "正常" :
                        op.getRelationStatus() == 2 ? "已转让" : "冻结") : "");
        op.setIsSelfOccupyText(op.getIsSelfOccupy() != null ?
                (op.getIsSelfOccupy() == 1 ? "是" : "否") : "");

        if (op.getPropertyId() != null) {
            ParkProperty property = parkPropertyMapper.selectOneById(op.getPropertyId());
            if (property != null) {
                op.setBuildingArea(property.getBuildingArea());
                op.setHouseType(property.getHouseType());
                if (property.getBuildingId() != null) {
                    ParkBuilding building = parkBuildingMapper.selectOneById(property.getBuildingId());
                    if (building != null) {
                        op.setBuildingName(building.getBuildingName());
                    }
                }
                if (property.getFloorId() != null) {
                    ParkFloor floor = parkFloorMapper.selectOneById(property.getFloorId());
                    if (floor != null) {
                        op.setFloorNumber(floor.getFloorNumber());
                    }
                }
            }
        }
    }

    private Long countByOwnerType(Integer type) {
        return parkOwnerMapper.selectCountByQuery(
                QueryWrapper.create().from(ParkOwner.class)
                        .where(PARK_OWNER.OWNER_TYPE.eq(type))
                        .and(PARK_OWNER.DELETED.eq(0))
        );
    }

    private Long countByAuthStatus(Integer status) {
        return parkOwnerMapper.selectCountByQuery(
                QueryWrapper.create().from(ParkOwner.class)
                        .where(PARK_OWNER.AUTH_STATUS.eq(status))
                        .and(PARK_OWNER.DELETED.eq(0))
        );
    }

    private Long countByOccupancyStatus(Integer status) {
        return parkOwnerMapper.selectCountByQuery(
                QueryWrapper.create().from(ParkOwner.class)
                        .where(PARK_OWNER.OCCUPANCY_STATUS.eq(status))
                        .and(PARK_OWNER.DELETED.eq(0))
        );
    }

    private int getOwnerPropertyCount(Long ownerId) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkOwnerProperty.class)
                .where(PARK_OWNER_PROPERTY.OWNER_ID.eq(ownerId))
                .and(PARK_OWNER_PROPERTY.RELATION_STATUS.eq(1));
        Long count = parkOwnerPropertyMapper.selectCountByQuery(query);
        return count != null ? count.intValue() : 0;
    }

    private Long countByPropertyCount(Integer min, Integer max) {
        String sql = "SELECT COUNT(*) FROM (" +
                "SELECT o.id FROM park_owner o " +
                "LEFT JOIN park_owner_property op ON o.id = op.owner_id AND op.relation_status = 1 " +
                "WHERE o.deleted = 0 " +
                "GROUP BY o.id " +
                "HAVING COUNT(op.id) >= " + min +
                (max != null ? " AND COUNT(op.id) <= " + max : "") +
                ") t";
        Object result = parkOwnerMapper.selectObjectByQuery(QueryWrapper.create().select(sql));
        return result != null ? Long.parseLong(result.toString()) : 0L;
    }

    private long getTotalPropertyCount() {
        Object result = parkOwnerMapper.selectObjectByQuery(
                QueryWrapper.create()
                        .select("COUNT(*)")
                        .from(ParkOwnerProperty.class)
                        .where(PARK_OWNER_PROPERTY.RELATION_STATUS.eq(1))
        );
        return result != null ? Long.parseLong(result.toString()) : 0L;
    }

    private List<Map<String, Object>> getMoveInTrend() {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String monthStr = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            LocalDate startDate = month.withDayOfMonth(1);
            LocalDate endDate = month.plusMonths(1).withDayOfMonth(1);

            Long count = parkOwnerPropertyMapper.selectCountByQuery(
                    QueryWrapper.create()
                            .from(ParkOwnerProperty.class)
                            .where(PARK_OWNER_PROPERTY.MOVE_IN_DATE.ge(startDate))
                            .and(PARK_OWNER_PROPERTY.MOVE_IN_DATE.lt(endDate))
                            .and(PARK_OWNER_PROPERTY.RELATION_STATUS.eq(1))
            );

            Map<String, Object> item = new HashMap<>();
            item.put("month", monthStr);
            item.put("count", count != null ? count : 0);
            result.add(item);
        }
        return result;
    }

    private Map<String, Long> getTagStatistics() {
        Map<String, Long> result = new LinkedHashMap<>();
        String[] tags = {"VIP业主", "长期欠费", "友好邻居", "投诉较多"};
        for (String tag : tags) {
            Long count = parkOwnerMapper.selectCountByQuery(
                    QueryWrapper.create()
                            .from(ParkOwner.class)
                            .where(PARK_OWNER.OWNER_TAGS.like(tag))
                            .and(PARK_OWNER.DELETED.eq(0))
            );
            result.put(tag, count != null ? count : 0);
        }
        return result;
    }

    private String getCellString(List<Object> row, int index) {
        if (index >= row.size()) return null;
        Object val = row.get(index);
        return val != null ? val.toString().trim() : null;
    }
}
