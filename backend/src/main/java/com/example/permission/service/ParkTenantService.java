
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
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;

import static com.example.permission.entity.table.ParkTenantTableDef.PARK_TENANT;
import static com.example.permission.entity.table.ParkLeaseContractTableDef.PARK_LEASE_CONTRACT;
import static com.example.permission.entity.table.ParkTenantPropertyTableDef.PARK_TENANT_PROPERTY;
import static com.example.permission.entity.table.ParkPropertyTableDef.PARK_PROPERTY;
import static com.example.permission.entity.table.ParkBuildingTableDef.PARK_BUILDING;
import static com.example.permission.entity.table.ParkFloorTableDef.PARK_FLOOR;

@Service
public class ParkTenantService {

    @Autowired
    private ParkTenantMapper parkTenantMapper;

    @Autowired
    private ParkLeaseContractMapper parkLeaseContractMapper;

    @Autowired
    private ParkLeaseContractLogMapper parkLeaseContractLogMapper;

    @Autowired
    private ParkTenantPropertyMapper parkTenantPropertyMapper;

    @Autowired
    private ParkPropertyMapper parkPropertyMapper;

    @Autowired
    private ParkBuildingMapper parkBuildingMapper;

    @Autowired
    private ParkFloorMapper parkFloorMapper;

    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    public PageResult<ParkTenant> pageList(Integer pageNum, Integer pageSize, String keyword,
                                           Integer tenantType, Integer tenantStatus,
                                           Long propertyId, LocalDate moveInStartDate, LocalDate moveInEndDate,
                                           String sortField, String sortOrder) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkTenant.class)
                .where(PARK_TENANT.DELETED.eq(0));

        if (keyword != null && !keyword.isEmpty()) {
            query.and(PARK_TENANT.NAME.like(keyword)
                    .or(PARK_TENANT.PHONE.like(keyword))
                    .or(PARK_TENANT.ID_CARD.like(keyword)));
        }
        if (tenantType != null) {
            query.and(PARK_TENANT.TENANT_TYPE.eq(tenantType));
        }
        if (tenantStatus != null) {
            query.and(PARK_TENANT.TENANT_STATUS.eq(tenantStatus));
        }

        if (sortField != null && !sortField.isEmpty()) {
            switch (sortField) {
                case "tenantCode":
                    query.orderBy("asc".equals(sortOrder) ? PARK_TENANT.TENANT_CODE.asc() : PARK_TENANT.TENANT_CODE.desc());
                    break;
                case "name":
                    query.orderBy("asc".equals(sortOrder) ? PARK_TENANT.NAME.asc() : PARK_TENANT.NAME.desc());
                    break;
                default:
                    query.orderBy(PARK_TENANT.CREATE_TIME.desc());
            }
        } else {
            query.orderBy(PARK_TENANT.TENANT_CODE.asc());
        }

        Page<ParkTenant> page = parkTenantMapper.paginate(Page.of(pageNum, pageSize), query);

        List<ParkTenant> list = page.getRecords();
        for (ParkTenant tenant : list) {
            enrichTenant(tenant);
            loadCurrentLeaseInfo(tenant);
        }

        return new PageResult<>(page.getTotalRow(), list,
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public ParkTenant getById(Long id) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkTenant.class)
                .where(PARK_TENANT.ID.eq(id))
                .and(PARK_TENANT.DELETED.eq(0));
        ParkTenant tenant = parkTenantMapper.selectOneByQuery(query);
        if (tenant != null) {
            enrichTenant(tenant);
            tenant.setContractList(getTenantContracts(id));
            tenant.setPropertyList(getTenantProperties(id));
            loadCurrentLeaseInfo(tenant);
        }
        return tenant;
    }

    public ParkTenant getByCode(String tenantCode) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkTenant.class)
                .where(PARK_TENANT.TENANT_CODE.eq(tenantCode))
                .and(PARK_TENANT.DELETED.eq(0));
        return parkTenantMapper.selectOneByQuery(query);
    }

    public ParkTenant getByIdCard(String idCard) {
        if (idCard == null || idCard.isEmpty()) return null;
        QueryWrapper query = QueryWrapper.create()
                .from(ParkTenant.class)
                .where(PARK_TENANT.ID_CARD.eq(idCard))
                .and(PARK_TENANT.DELETED.eq(0));
        return parkTenantMapper.selectOneByQuery(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(ParkTenant tenant, Long operatorId, String operatorName) {
        validateTenant(tenant, null);

        if (tenant.getTenantType() == 2 && (tenant.getBusinessLicenseUrl() == null || tenant.getBusinessLicenseUrl().isEmpty())) {
            throw new BusinessException("企业租户必须上传营业执照");
        }

        tenant.setTenantCode(generateTenantCode());
        tenant.setDeleted(0);
        tenant.setTenantStatus(tenant.getTenantStatus() != null ? tenant.getTenantStatus() : 1);

        tenant.setCreateTime(LocalDateTime.now());
        tenant.setUpdateTime(LocalDateTime.now());
        parkTenantMapper.insert(tenant);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(ParkTenant tenant) {
        ParkTenant existTenant = parkTenantMapper.selectOneById(tenant.getId());
        if (existTenant == null) {
            throw new BusinessException("租户不存在");
        }

        tenant.setTenantCode(null);
        tenant.setTenantType(null);

        validateTenant(tenant, existTenant.getId());

        tenant.setUpdateTime(LocalDateTime.now());
        parkTenantMapper.update(tenant);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ParkTenant tenant = parkTenantMapper.selectOneById(id);
        if (tenant == null) {
            throw new BusinessException("租户不存在");
        }

        QueryWrapper query = QueryWrapper.create()
                .from(ParkLeaseContract.class)
                .where(PARK_LEASE_CONTRACT.TENANT_ID.eq(id))
                .and(PARK_LEASE_CONTRACT.CONTRACT_STATUS.eq(2))
                .and(PARK_LEASE_CONTRACT.DELETED.eq(0));
        Long count = parkLeaseContractMapper.selectCountByQuery(query);
        if (count != null && count > 0) {
            throw new BusinessException("该租户还有生效中的合约，请先解除合约后再删除");
        }

        tenant.setDeleted(1);
        tenant.setUpdateTime(LocalDateTime.now());
        parkTenantMapper.update(tenant);
    }

    @Transactional(rollbackFor = Exception.class)
    public void setBlacklist(Long id, boolean isBlacklist, String reason) {
        ParkTenant tenant = parkTenantMapper.selectOneById(id);
        if (tenant == null) {
            throw new BusinessException("租户不存在");
        }

        tenant.setTenantStatus(isBlacklist ? 3 : 1);
        tenant.setUpdateTime(LocalDateTime.now());
        parkTenantMapper.update(tenant);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();

        Long totalCount = parkTenantMapper.selectCountByQuery(
                QueryWrapper.create().from(ParkTenant.class).where(PARK_TENANT.DELETED.eq(0))
        );
        result.put("totalCount", totalCount);

        Long inRentCount = countByStatus(1);
        Long retiredCount = countByStatus(2);
        Long blacklistCount = countByStatus(3);
        result.put("inRentCount", inRentCount);
        result.put("retiredCount", retiredCount);
        result.put("blacklistCount", blacklistCount);

        Long personalCount = countByType(1);
        Long enterpriseCount = countByType(2);
        result.put("personalCount", personalCount);
        result.put("enterpriseCount", enterpriseCount);

        Long entireRentCount = countByLeaseType(1);
        Long shareRentCount = countByLeaseType(2);
        result.put("entireRentCount", entireRentCount);
        result.put("shareRentCount", shareRentCount);

        result.put("leaseTypeDistribution", getLeaseTypeDistribution());
        result.put("tenantStatusDistribution", getTenantStatusDistribution());
        result.put("leaseTermDistribution", getLeaseTermDistribution());
        result.put("rentRangeDistribution", getRentRangeDistribution());
        result.put("contractExpiryWarning", getContractExpiryWarning());

        Map<String, Object> rentStats = getRentStatistics();
        result.putAll(rentStats);

        return result;
    }

    public ExcelWriter getTemplateWriter() {
        ExcelWriter writer = ExcelUtil.getWriter();

        writer.addHeaderAlias("tenantType", "租户类型");
        writer.addHeaderAlias("name", "姓名/企业名称");
        writer.addHeaderAlias("gender", "性别");
        writer.addHeaderAlias("idCard", "身份证号");
        writer.addHeaderAlias("birthDate", "出生日期(yyyy-MM-dd)");
        writer.addHeaderAlias("phone", "手机号");
        writer.addHeaderAlias("backupPhone", "备用手机");
        writer.addHeaderAlias("email", "电子邮箱");
        writer.addHeaderAlias("workUnit", "工作单位");
        writer.addHeaderAlias("occupation", "职业");
        writer.addHeaderAlias("incomeRange", "月收入范围");
        writer.addHeaderAlias("emergencyContact", "紧急联系人");
        writer.addHeaderAlias("emergencyRelation", "联系人关系");
        writer.addHeaderAlias("emergencyPhone", "联系人电话");
        writer.addHeaderAlias("remark", "备注");
        writer.addHeaderAlias("enterpriseName", "企业名称");
        writer.addHeaderAlias("enterpriseCreditCode", "统一社会信用代码");
        writer.addHeaderAlias("legalPersonName", "法定代表人");
        writer.addHeaderAlias("contactPerson", "联系人姓名");
        writer.addHeaderAlias("contactPosition", "联系人职务");
        writer.addHeaderAlias("contactPhone", "联系人手机");
        writer.addHeaderAlias("enterpriseAddress", "企业地址");

        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("tenantType", "个人租户");
        example.put("name", "张三");
        example.put("gender", "男");
        example.put("idCard", "110101199001011234");
        example.put("birthDate", "1990-01-01");
        example.put("phone", "13800138001");
        example.put("backupPhone", "13900139001");
        example.put("email", "zhangsan@example.com");
        example.put("workUnit", "某科技公司");
        example.put("occupation", "工程师");
        example.put("incomeRange", "10000-20000");
        example.put("emergencyContact", "李四");
        example.put("emergencyRelation", "配偶");
        example.put("emergencyPhone", "13800138002");
        example.put("remark", "示例数据");
        example.put("enterpriseName", "");
        example.put("enterpriseCreditCode", "");
        example.put("legalPersonName", "");
        example.put("contactPerson", "");
        example.put("contactPosition", "");
        example.put("contactPhone", "");
        example.put("enterpriseAddress", "");
        rows.add(example);

        writer.write(rows, true);
        return writer;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importTenants(MultipartFile file, Long operatorId, String operatorName) {
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
                    String tenantTypeStr = getCellString(row, 0);
                    String name = getCellString(row, 1);
                    String genderStr = getCellString(row, 2);
                    String idCard = getCellString(row, 3);
                    String birthDateStr = getCellString(row, 4);
                    String phone = getCellString(row, 5);
                    String backupPhone = getCellString(row, 6);
                    String email = getCellString(row, 7);
                    String workUnit = getCellString(row, 8);
                    String occupation = getCellString(row, 9);
                    String incomeRange = getCellString(row, 10);
                    String emergencyContact = getCellString(row, 11);
                    String emergencyRelation = getCellString(row, 12);
                    String emergencyPhone = getCellString(row, 13);
                    String remark = getCellString(row, 14);
                    String enterpriseName = getCellString(row, 15);
                    String enterpriseCreditCode = getCellString(row, 16);
                    String legalPersonName = getCellString(row, 17);
                    String contactPerson = getCellString(row, 18);
                    String contactPosition = getCellString(row, 19);
                    String contactPhone = getCellString(row, 20);
                    String enterpriseAddress = getCellString(row, 21);

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

                    ParkTenant existByIdCard = getByIdCard(idCard);
                    if (existByIdCard != null) {
                        throw new BusinessException("身份证号已存在");
                    }
                    QueryWrapper phoneQuery = QueryWrapper.create()
                            .from(ParkTenant.class)
                            .where(PARK_TENANT.PHONE.eq(phone))
                            .and(PARK_TENANT.DELETED.eq(0));
                    Long phoneCount = parkTenantMapper.selectCountByQuery(phoneQuery);
                    if (phoneCount != null && phoneCount > 0) {
                        throw new BusinessException("手机号已存在");
                    }

                    idCardSet.add(idCard);
                    phoneSet.add(phone);

                    Integer tenantType = 1;
                    if ("个人租户".equals(tenantTypeStr)) tenantType = 1;
                    else if ("企业租户".equals(tenantTypeStr)) tenantType = 2;

                    Integer gender = null;
                    if ("男".equals(genderStr)) gender = 1;
                    else if ("女".equals(genderStr)) gender = 2;

                    ParkTenant tenant = new ParkTenant();
                    tenant.setTenantType(tenantType);
                    tenant.setName(name);
                    tenant.setGender(gender);
                    tenant.setIdCard(idCard);
                    tenant.setPhone(phone);
                    tenant.setBackupPhone(backupPhone);
                    tenant.setEmail(email);
                    tenant.setWorkUnit(workUnit);
                    tenant.setOccupation(occupation);
                    tenant.setIncomeRange(incomeRange);
                    tenant.setEmergencyContact(emergencyContact);
                    tenant.setEmergencyRelation(emergencyRelation);
                    tenant.setEmergencyPhone(emergencyPhone);
                    tenant.setRemark(remark);
                    tenant.setEnterpriseName(enterpriseName);
                    tenant.setEnterpriseCreditCode(enterpriseCreditCode);
                    tenant.setLegalPersonName(legalPersonName);
                    tenant.setContactPerson(contactPerson);
                    tenant.setContactPosition(contactPosition);
                    tenant.setContactPhone(contactPhone);
                    tenant.setEnterpriseAddress(enterpriseAddress);
                    tenant.setTenantStatus(1);
                    tenant.setDeleted(0);

                    if (birthDateStr != null && !birthDateStr.isEmpty()) {
                        try {
                            tenant.setBirthDate(LocalDate.parse(birthDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        } catch (Exception e) {
                            throw new BusinessException("出生日期格式错误");
                        }
                    }

                    add(tenant, operatorId, operatorName);
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

    public List<ParkTenant> listAllForExport(String keyword, Integer tenantType, Integer tenantStatus) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkTenant.class)
                .where(PARK_TENANT.DELETED.eq(0));

        if (keyword != null && !keyword.isEmpty()) {
            query.and(PARK_TENANT.NAME.like(keyword)
                    .or(PARK_TENANT.PHONE.like(keyword))
                    .or(PARK_TENANT.ID_CARD.like(keyword)));
        }
        if (tenantType != null) {
            query.and(PARK_TENANT.TENANT_TYPE.eq(tenantType));
        }
        if (tenantStatus != null) {
            query.and(PARK_TENANT.TENANT_STATUS.eq(tenantStatus));
        }

        query.orderBy(PARK_TENANT.TENANT_CODE.asc());

        List<ParkTenant> list = parkTenantMapper.selectListByQuery(query);
        for (ParkTenant tenant : list) {
            enrichTenant(tenant);
            loadCurrentLeaseInfo(tenant);
        }
        return list;
    }

    private void validateTenant(ParkTenant tenant, Long excludeId) {
        if (tenant.getName() == null || tenant.getName().isEmpty()) {
            throw new BusinessException("姓名不能为空");
        }
        if (tenant.getPhone() == null || tenant.getPhone().isEmpty()) {
            throw new BusinessException("手机号不能为空");
        }
        if (!PHONE_PATTERN.matcher(tenant.getPhone()).matches()) {
            throw new BusinessException("手机号格式错误");
        }
        if (tenant.getTenantType() != null && tenant.getTenantType() == 1) {
            if (tenant.getIdCard() != null && !tenant.getIdCard().isEmpty()) {
                if (!ID_CARD_PATTERN.matcher(tenant.getIdCard()).matches()) {
                    throw new BusinessException("身份证号格式错误");
                }
                ParkTenant exist = getByIdCard(tenant.getIdCard());
                if (exist != null && (excludeId == null || !exist.getId().equals(excludeId))) {
                    throw new BusinessException("身份证号已存在");
                }
            }
        }

        if (tenant.getIdCard() != null && !tenant.getIdCard().isEmpty()) {
            if (!ID_CARD_PATTERN.matcher(tenant.getIdCard()).matches()) {
                throw new BusinessException("身份证号格式错误");
            }
            ParkTenant existByIdCard = getByIdCard(tenant.getIdCard());
            if (existByIdCard != null && (excludeId == null || !existByIdCard.getId().equals(excludeId))) {
                throw new BusinessException("身份证号已存在");
            }
        }

        QueryWrapper phoneQuery = QueryWrapper.create()
                .from(ParkTenant.class)
                .where(PARK_TENANT.PHONE.eq(tenant.getPhone()))
                .and(PARK_TENANT.DELETED.eq(0));
        if (excludeId != null) {
            phoneQuery.and(PARK_TENANT.ID.ne(excludeId));
        }
        Long phoneCount = parkTenantMapper.selectCountByQuery(phoneQuery);
        if (phoneCount != null && phoneCount > 0) {
            throw new BusinessException("手机号已存在");
        }
    }

    private String generateTenantCode() {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkTenant.class)
                .orderBy(PARK_TENANT.TENANT_CODE.desc())
                .limit(1);
        ParkTenant last = parkTenantMapper.selectOneByQuery(query);
        int seq = 1;
        if (last != null && last.getTenantCode() != null) {
            try {
                seq = Integer.parseInt(last.getTenantCode().replace("ZH", "")) + 1;
            } catch (Exception ignored) {}
        }
        return String.format("ZH%03d", seq);
    }

    private void enrichTenant(ParkTenant tenant) {
        tenant.setGenderText(tenant.getGender() != null ? (tenant.getGender() == 1 ? "男" : "女") : "");
        tenant.setTenantTypeText(tenant.getTenantType() != null ?
                (tenant.getTenantType() == 1 ? "个人租户" : "企业租户") : "");
        tenant.setTenantStatusText(tenant.getTenantStatus() != null ?
                (tenant.getTenantStatus() == 1 ? "在租" :
                        tenant.getTenantStatus() == 2 ? "已退租" : "黑名单") : "");
    }

    private void loadCurrentLeaseInfo(ParkTenant tenant) {
        QueryWrapper contractQuery = QueryWrapper.create()
                .from(ParkLeaseContract.class)
                .where(PARK_LEASE_CONTRACT.TENANT_ID.eq(tenant.getId()))
                .and(PARK_LEASE_CONTRACT.CONTRACT_STATUS.eq(2))
                .and(PARK_LEASE_CONTRACT.DELETED.eq(0))
                .orderBy(PARK_LEASE_CONTRACT.CREATE_TIME.desc())
                .limit(1);
        ParkLeaseContract currentContract = parkLeaseContractMapper.selectOneByQuery(contractQuery);
        if (currentContract != null) {
            tenant.setCurrentContract(currentContract);
            if (currentContract.getStartDate() != null && currentContract.getEndDate() != null) {
                tenant.setCurrentLeasePeriod(currentContract.getStartDate() + " 至 " + currentContract.getEndDate());
            }
            if (currentContract.getPropertyId() != null) {
                ParkProperty property = parkPropertyMapper.selectOneById(currentContract.getPropertyId());
                if (property != null) {
                    tenant.setCurrentPropertyCode(property.getPropertyCode());
                    if (property.getBuildingId() != null) {
                        ParkBuilding building = parkBuildingMapper.selectOneById(property.getBuildingId());
                        if (building != null) {
                            tenant.setCurrentBuildingName(building.getBuildingName());
                        }
                    }
                }
            }
        }
    }

    private List<ParkLeaseContract> getTenantContracts(Long tenantId) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkLeaseContract.class)
                .where(PARK_LEASE_CONTRACT.TENANT_ID.eq(tenantId))
                .and(PARK_LEASE_CONTRACT.DELETED.eq(0))
                .orderBy(PARK_LEASE_CONTRACT.CREATE_TIME.desc());
        List<ParkLeaseContract> list = parkLeaseContractMapper.selectListByQuery(query);
        for (ParkLeaseContract contract : list) {
            enrichContractBasic(contract);
        }
        return list;
    }

    private List<ParkTenantProperty> getTenantProperties(Long tenantId) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkTenantProperty.class)
                .leftJoin(ParkProperty.class).on(PARK_PROPERTY.ID.eq(PARK_TENANT_PROPERTY.PROPERTY_ID))
                .leftJoin(ParkBuilding.class).on(PARK_BUILDING.ID.eq(PARK_PROPERTY.BUILDING_ID))
                .leftJoin(ParkFloor.class).on(PARK_FLOOR.ID.eq(PARK_PROPERTY.FLOOR_ID))
                .where(PARK_TENANT_PROPERTY.TENANT_ID.eq(tenantId))
                .orderBy(PARK_TENANT_PROPERTY.CREATE_TIME.desc());

        List<ParkTenantProperty> list = parkTenantPropertyMapper.selectListByQuery(query);
        for (ParkTenantProperty tp : list) {
            enrichTenantProperty(tp);
        }
        return list;
    }

    private void enrichTenantProperty(ParkTenantProperty tp) {
        tp.setRelationStatusText(tp.getRelationStatus() != null ?
                (tp.getRelationStatus() == 1 ? "生效中" : "已结束") : "");

        if (tp.getPropertyId() != null) {
            ParkProperty property = parkPropertyMapper.selectOneById(tp.getPropertyId());
            if (property != null) {
                tp.setBuildingArea(property.getBuildingArea());
                tp.setHouseType(property.getHouseType());
                if (property.getBuildingId() != null) {
                    ParkBuilding building = parkBuildingMapper.selectOneById(property.getBuildingId());
                    if (building != null) {
                        tp.setBuildingName(building.getBuildingName());
                    }
                }
                if (property.getFloorId() != null) {
                    ParkFloor floor = parkFloorMapper.selectOneById(property.getFloorId());
                    if (floor != null) {
                        tp.setFloorNumber(floor.getFloorNumber());
                    }
                }
            }
        }
    }

    private void enrichContractBasic(ParkLeaseContract contract) {
        contract.setLeaseTypeText(contract.getLeaseType() != null ?
                (contract.getLeaseType() == 1 ? "整租" : "合租") : "");
        contract.setContractStatusText(contract.getContractStatus() != null ?
                (contract.getContractStatus() == 1 ? "待生效" :
                        contract.getContractStatus() == 2 ? "生效中" :
                                contract.getContractStatus() == 3 ? "已续签" :
                                        contract.getContractStatus() == 4 ? "已解除" :
                                                contract.getContractStatus() == 5 ? "已到期" : "已拒绝") : "");
        contract.setPaymentMethodText(contract.getPaymentMethod() != null ?
                (contract.getPaymentMethod() == 1 ? "月付" :
                        contract.getPaymentMethod() == 2 ? "季付" :
                                contract.getPaymentMethod() == 3 ? "半年付" : "年付") : "");
    }

    private Long countByStatus(Integer status) {
        return parkTenantMapper.selectCountByQuery(
                QueryWrapper.create().from(ParkTenant.class)
                        .where(PARK_TENANT.TENANT_STATUS.eq(status))
                        .and(PARK_TENANT.DELETED.eq(0))
        );
    }

    private Long countByType(Integer type) {
        return parkTenantMapper.selectCountByQuery(
                QueryWrapper.create().from(ParkTenant.class)
                        .where(PARK_TENANT.TENANT_TYPE.eq(type))
                        .and(PARK_TENANT.DELETED.eq(0))
        );
    }

    private Long countByLeaseType(Integer leaseType) {
        return parkLeaseContractMapper.selectCountByQuery(
                QueryWrapper.create().from(ParkLeaseContract.class)
                        .where(PARK_LEASE_CONTRACT.LEASE_TYPE.eq(leaseType))
                        .and(PARK_LEASE_CONTRACT.CONTRACT_STATUS.in(Arrays.asList(1, 2)))
                        .and(PARK_LEASE_CONTRACT.DELETED.eq(0))
        );
    }

    private Map<String, Long> getTenantStatusDistribution() {
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("在租", countByStatus(1));
        result.put("已退租", countByStatus(2));
        result.put("黑名单", countByStatus(3));
        return result;
    }

    private Map<String, Long> getLeaseTypeDistribution() {
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("整租", countByLeaseType(1));
        result.put("合租", countByLeaseType(2));
        return result;
    }

    private Map<String, Long> getLeaseTermDistribution() {
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("6个月及以下", countByLeaseTermRange(null, 6));
        result.put("12个月", countByLeaseTermRange(7, 12));
        result.put("24个月", countByLeaseTermRange(13, 24));
        result.put("24个月以上", countByLeaseTermRange(25, null));
        return result;
    }

    private Long countByLeaseTermRange(Integer min, Integer max) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkLeaseContract.class)
                .where(PARK_LEASE_CONTRACT.CONTRACT_STATUS.in(Arrays.asList(1, 2)))
                .and(PARK_LEASE_CONTRACT.DELETED.eq(0));
        if (min != null) {
            query.and(PARK_LEASE_CONTRACT.LEASE_MONTHS.ge(min));
        }
        if (max != null) {
            query.and(PARK_LEASE_CONTRACT.LEASE_MONTHS.le(max));
        }
        return parkLeaseContractMapper.selectCountByQuery(query);
    }

    private Map<String, Long> getRentRangeDistribution() {
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("2000以下", countByRentRange(null, 2000));
        result.put("2000-4000", countByRentRange(2000, 4000));
        result.put("4000-6000", countByRentRange(4000, 6000));
        result.put("6000-8000", countByRentRange(6000, 8000));
        result.put("8000以上", countByRentRange(8000, null));
        return result;
    }

    private Long countByRentRange(Integer min, Integer max) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkLeaseContract.class)
                .where(PARK_LEASE_CONTRACT.CONTRACT_STATUS.in(Arrays.asList(1, 2)))
                .and(PARK_LEASE_CONTRACT.DELETED.eq(0));
        if (min != null) {
            query.and(PARK_LEASE_CONTRACT.MONTHLY_RENT.ge(new BigDecimal(min)));
        }
        if (max != null) {
            query.and(PARK_LEASE_CONTRACT.MONTHLY_RENT.lt(new BigDecimal(max)));
        }
        return parkLeaseContractMapper.selectCountByQuery(query);
    }

    private List<Map<String, Object>> getContractExpiryWarning() {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate now = LocalDate.now();

        int[] daysList = {30, 60, 90};
        for (int days : daysList) {
            LocalDate endDate = now.plusDays(days);
            LocalDate startDate = days == 30 ? now : now.plusDays(days - 30);

            QueryWrapper query = QueryWrapper.create()
                    .from(ParkLeaseContract.class)
                    .where(PARK_LEASE_CONTRACT.CONTRACT_STATUS.eq(2))
                    .and(PARK_LEASE_CONTRACT.END_DATE.gt(startDate))
                    .and(PARK_LEASE_CONTRACT.END_DATE.le(endDate))
                    .and(PARK_LEASE_CONTRACT.DELETED.eq(0))
                    .orderBy(PARK_LEASE_CONTRACT.END_DATE.asc());

            List<ParkLeaseContract> contracts = parkLeaseContractMapper.selectListByQuery(query);
            for (ParkLeaseContract contract : contracts) {
                enrichContractBasic(contract);
                Map<String, Object> item = new HashMap<>();
                item.put("contractCode", contract.getContractCode());
                item.put("tenantName", contract.getTenantName());
                item.put("tenantPhone", getTenantPhoneById(contract.getTenantId()));
                item.put("propertyCode", contract.getPropertyCode());
                item.put("endDate", contract.getEndDate().toString());
                item.put("monthlyRent", contract.getMonthlyRent());
                item.put("warningDays", days);
                item.put("contractStatusText", contract.getContractStatusText());
                result.add(item);
            }
        }
        return result;
    }

    private String getTenantPhoneById(Long tenantId) {
        if (tenantId == null) return null;
        ParkTenant tenant = parkTenantMapper.selectOneById(tenantId);
        return tenant != null ? tenant.getPhone() : null;
    }

    private Map<String, Object> getRentStatistics() {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper query = QueryWrapper.create()
                .select("AVG(monthly_rent) as avg_rent")
                .from(ParkLeaseContract.class)
                .where(PARK_LEASE_CONTRACT.CONTRACT_STATUS.in(Arrays.asList(1, 2)))
                .and(PARK_LEASE_CONTRACT.DELETED.eq(0));

        Object avgRentObj = parkLeaseContractMapper.selectObjectByQuery(query);
        BigDecimal avgRent = avgRentObj != null ? new BigDecimal(avgRentObj.toString()) : BigDecimal.ZERO;
        result.put("avgRent", avgRent.setScale(2, BigDecimal.ROUND_HALF_UP));

        return result;
    }

    private String getCellString(List<Object> row, int index) {
        if (index >= row.size()) return null;
        Object val = row.get(index);
        return val != null ? val.toString().trim() : null;
    }
}
