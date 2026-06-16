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
import java.util.stream.Collectors;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;

import static com.example.permission.entity.table.ParkParkingSpaceTableDef.PARK_PARKING_SPACE;
import static com.example.permission.entity.table.ParkParkingSpaceStatusLogTableDef.PARK_PARKING_SPACE_STATUS_LOG;
import static com.example.permission.entity.table.ParkOwnerTableDef.PARK_OWNER;

@Service
public class ParkParkingSpaceService {

    @Autowired
    private ParkParkingSpaceMapper parkParkingSpaceMapper;

    @Autowired
    private ParkParkingSpaceStatusLogMapper parkParkingSpaceStatusLogMapper;

    @Autowired
    private ParkOwnerMapper parkOwnerMapper;

    public PageResult<ParkParkingSpace> pageList(Integer pageNum, Integer pageSize, String spaceCode,
                                                  String area, String spaceType, Integer status,
                                                  String propertyRight, BigDecimal minRent,
                                                  BigDecimal maxRent, String sortField, String sortOrder) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkParkingSpace.class)
                .where(PARK_PARKING_SPACE.DELETED.eq(0));

        if (spaceCode != null && !spaceCode.isEmpty()) {
            query.and(PARK_PARKING_SPACE.SPACE_CODE.like(spaceCode));
        }
        if (area != null && !area.isEmpty()) {
            query.and(PARK_PARKING_SPACE.AREA.eq(area));
        }
        if (spaceType != null && !spaceType.isEmpty()) {
            query.and(PARK_PARKING_SPACE.SPACE_TYPE.eq(spaceType));
        }
        if (status != null) {
            query.and(PARK_PARKING_SPACE.STATUS.eq(status));
        }
        if (propertyRight != null && !propertyRight.isEmpty()) {
            query.and(PARK_PARKING_SPACE.PROPERTY_RIGHT.eq(propertyRight));
        }
        if (minRent != null) {
            query.and(PARK_PARKING_SPACE.MONTHLY_RENT.ge(minRent));
        }
        if (maxRent != null) {
            query.and(PARK_PARKING_SPACE.MONTHLY_RENT.le(maxRent));
        }

        if (sortField != null && !sortField.isEmpty()) {
            if ("spaceCode".equals(sortField)) {
                if ("desc".equals(sortOrder)) {
                    query.orderBy(PARK_PARKING_SPACE.SPACE_CODE.desc());
                } else {
                    query.orderBy(PARK_PARKING_SPACE.SPACE_CODE.asc());
                }
            } else if ("monthlyRent".equals(sortField)) {
                if ("desc".equals(sortOrder)) {
                    query.orderBy(PARK_PARKING_SPACE.MONTHLY_RENT.desc());
                } else {
                    query.orderBy(PARK_PARKING_SPACE.MONTHLY_RENT.asc());
                }
            } else if ("area".equals(sortField)) {
                if ("desc".equals(sortOrder)) {
                    query.orderBy(PARK_PARKING_SPACE.AREA.desc());
                } else {
                    query.orderBy(PARK_PARKING_SPACE.AREA.asc());
                }
            } else {
                query.orderBy(PARK_PARKING_SPACE.CREATE_TIME.desc());
            }
        } else {
            query.orderBy(PARK_PARKING_SPACE.SPACE_CODE.asc());
        }

        Page<ParkParkingSpace> page = parkParkingSpaceMapper.paginate(Page.of(pageNum, pageSize), query);

        List<ParkParkingSpace> list = page.getRecords();
        for (ParkParkingSpace space : list) {
            fillOwnerName(space);
        }

        return new PageResult<>(page.getTotalRow(), list,
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public ParkParkingSpace getById(Long id) {
        ParkParkingSpace space = parkParkingSpaceMapper.selectOneById(id);
        if (space != null) {
            fillOwnerName(space);
            space.setStatusLogs(getStatusLogs(id));
        }
        return space;
    }

    public ParkParkingSpace getByCode(String spaceCode) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkParkingSpace.class)
                .where(PARK_PARKING_SPACE.SPACE_CODE.eq(spaceCode))
                .and(PARK_PARKING_SPACE.DELETED.eq(0));
        return parkParkingSpaceMapper.selectOneByQuery(query);
    }

    private void fillOwnerName(ParkParkingSpace space) {
        if (space.getOwnerId() != null) {
            ParkOwner owner = parkOwnerMapper.selectOneById(space.getOwnerId());
            if (owner != null) {
                space.setOwnerName(owner.getName());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(ParkParkingSpace space, Long operatorId, String operatorName) {
        if (space.getSpaceCode() == null || space.getSpaceCode().isEmpty()) {
            throw new BusinessException("车位编号不能为空");
        }
        if (space.getArea() == null || space.getArea().isEmpty()) {
            throw new BusinessException("请选择所属区域");
        }
        if (space.getSpaceType() == null || space.getSpaceType().isEmpty()) {
            throw new BusinessException("请选择车位类型");
        }

        ParkParkingSpace existSpace = getByCode(space.getSpaceCode());
        if (existSpace != null) {
            throw new BusinessException("车位编号已存在");
        }

        if ("业主".equals(space.getPropertyRight())) {
            if (space.getPurchasePrice() == null) {
                throw new BusinessException("产权归属为业主时，购买价格必填");
            }
            if (space.getPurchaseDate() == null) {
                throw new BusinessException("产权归属为业主时，购买日期必填");
            }
        }

        if (space.getMonthlyRent() != null && space.getMonthlyRent().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("月租金不能为负数");
        }
        if (space.getHourlyFee() != null && space.getHourlyFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("临时收费不能为负数");
        }

        space.setDeleted(0);
        space.setStatus(space.getStatus() != null ? space.getStatus() : 1);
        space.setCreateTime(LocalDateTime.now());
        space.setUpdateTime(LocalDateTime.now());
        parkParkingSpaceMapper.insert(space);

        addStatusLog(space.getId(), space.getSpaceCode(), null, space.getStatus(), "创建车位", operatorId, operatorName);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(ParkParkingSpace space, boolean isManager) {
        ParkParkingSpace existSpace = parkParkingSpaceMapper.selectOneById(space.getId());
        if (existSpace == null) {
            throw new BusinessException("车位不存在");
        }

        if (space.getMonthlyRent() != null && space.getMonthlyRent().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("月租金不能为负数");
        }
        if (space.getHourlyFee() != null && space.getHourlyFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("临时收费不能为负数");
        }

        if (!isManager) {
            space.setPropertyCertNo(existSpace.getPropertyCertNo());
            space.setPurchasePrice(existSpace.getPurchasePrice());
            space.setPurchaseDate(existSpace.getPurchaseDate());
        }

        space.setSpaceCode(null);
        space.setArea(null);
        space.setSpaceType(null);
        space.setUpdateTime(LocalDateTime.now());
        parkParkingSpaceMapper.update(space);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ParkParkingSpace space = parkParkingSpaceMapper.selectOneById(id);
        if (space == null) {
            throw new BusinessException("车位不存在");
        }

        space.setDeleted(1);
        space.setUpdateTime(LocalDateTime.now());
        parkParkingSpaceMapper.update(space);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        for (Long id : ids) {
            delete(id);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer newStatus, String changeReason, Long operatorId, String operatorName) {
        ParkParkingSpace space = parkParkingSpaceMapper.selectOneById(id);
        if (space == null) {
            throw new BusinessException("车位不存在");
        }

        Integer oldStatus = space.getStatus();
        if (Objects.equals(oldStatus, newStatus)) {
            throw new BusinessException("状态未变更");
        }

        space.setStatus(newStatus);
        space.setUpdateTime(LocalDateTime.now());
        parkParkingSpaceMapper.update(space);

        addStatusLog(id, space.getSpaceCode(), oldStatus, newStatus, changeReason, operatorId, operatorName);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchChangeStatus(List<Long> ids, Integer newStatus, String changeReason, Long operatorId, String operatorName) {
        for (Long id : ids) {
            changeStatus(id, newStatus, changeReason, operatorId, operatorName);
        }
    }

    public List<ParkParkingSpaceStatusLog> getStatusLogs(Long spaceId) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkParkingSpaceStatusLog.class)
                .where(PARK_PARKING_SPACE_STATUS_LOG.SPACE_ID.eq(spaceId))
                .orderBy(PARK_PARKING_SPACE_STATUS_LOG.CREATE_TIME.desc());
        return parkParkingSpaceStatusLogMapper.selectListByQuery(query);
    }

    private void addStatusLog(Long spaceId, String spaceCode, Integer oldStatus, Integer newStatus,
                              String changeReason, Long operatorId, String operatorName) {
        ParkParkingSpaceStatusLog log = new ParkParkingSpaceStatusLog();
        log.setSpaceId(spaceId);
        log.setSpaceCode(spaceCode);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setChangeReason(changeReason);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setCreateTime(LocalDateTime.now());
        parkParkingSpaceStatusLogMapper.insert(log);
    }

    public List<ParkParkingSpace> previewBatchCreate(String area, String spaceType, String prefix,
                                                      Integer startNum, Integer endNum,
                                                      BigDecimal length, BigDecimal width,
                                                      String propertyRight, BigDecimal monthlyRent,
                                                      BigDecimal hourlyFee) {
        List<ParkParkingSpace> list = new ArrayList<>();
        int digitCount = String.valueOf(endNum).length();

        for (int i = startNum; i <= endNum; i++) {
            String seq = String.format("%0" + digitCount + "d", i);
            String spaceCode = prefix + seq;

            ParkParkingSpace space = new ParkParkingSpace();
            space.setSpaceCode(spaceCode);
            space.setArea(area);
            space.setSpaceType(spaceType);
            space.setLength(length);
            space.setWidth(width);
            space.setPropertyRight(propertyRight);
            space.setMonthlyRent(monthlyRent);
            space.setHourlyFee(hourlyFee);
            space.setStatus(1);

            list.add(space);
        }
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchCreate(String area, String spaceType, String prefix,
                                            Integer startNum, Integer endNum,
                                            BigDecimal length, BigDecimal width,
                                            String propertyRight, BigDecimal monthlyRent,
                                            BigDecimal hourlyFee, BigDecimal deposit,
                                            Long operatorId, String operatorName) {
        int successCount = 0;
        int failCount = 0;
        List<Map<String, String>> failList = new ArrayList<>();
        int digitCount = String.valueOf(endNum).length();

        for (int i = startNum; i <= endNum; i++) {
            String seq = String.format("%0" + digitCount + "d", i);
            String spaceCode = prefix + seq;

            try {
                ParkParkingSpace existSpace = getByCode(spaceCode);
                if (existSpace != null) {
                    failCount++;
                    Map<String, String> failItem = new HashMap<>();
                    failItem.put("spaceCode", spaceCode);
                    failItem.put("reason", "车位编号已存在");
                    failList.add(failItem);
                    continue;
                }

                ParkParkingSpace space = new ParkParkingSpace();
                space.setSpaceCode(spaceCode);
                space.setArea(area);
                space.setSpaceType(spaceType);
                space.setLength(length);
                space.setWidth(width);
                space.setPropertyRight(propertyRight);
                space.setMonthlyRent(monthlyRent);
                space.setHourlyFee(hourlyFee);
                space.setDeposit(deposit);
                space.setStatus(1);
                space.setDeleted(0);
                space.setCreateTime(LocalDateTime.now());
                space.setUpdateTime(LocalDateTime.now());
                parkParkingSpaceMapper.insert(space);

                addStatusLog(space.getId(), spaceCode, null, 1, "批量创建", operatorId, operatorName);

                successCount++;
            } catch (Exception e) {
                failCount++;
                Map<String, String> failItem = new HashMap<>();
                failItem.put("spaceCode", spaceCode);
                failItem.put("reason", e.getMessage());
                failList.add(failItem);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failList", failList);
        return result;
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper totalQuery = QueryWrapper.create()
                .from(ParkParkingSpace.class)
                .where(PARK_PARKING_SPACE.DELETED.eq(0));
        Long totalCount = parkParkingSpaceMapper.selectCountByQuery(totalQuery);
        result.put("totalCount", totalCount);

        Map<String, Long> typeDistribution = new HashMap<>();
        String[] types = {"标准车位", "大型车位", "微型车位", "充电车位", "无障碍车位"};
        for (String type : types) {
            typeDistribution.put(type, countBySpaceType(type));
        }
        result.put("typeDistribution", typeDistribution);

        Map<String, Long> statusDistribution = new LinkedHashMap<>();
        String[] statusLabels = {"空闲", "业主自用", "已出租", "临时占用", "维护中", "禁用"};
        for (int i = 1; i <= 6; i++) {
            statusDistribution.put(statusLabels[i - 1], countByStatus(i));
        }
        result.put("statusDistribution", statusDistribution);

        Long freeCount = countByStatus(1);
        Long selfUseCount = countByStatus(2);
        Long rentedCount = countByStatus(3);
        Long tempUseCount = countByStatus(4);
        Long maintenanceCount = countByStatus(5);
        Long disabledCount = countByStatus(6);

        Long usedCount = selfUseCount + rentedCount + tempUseCount;
        result.put("freeCount", freeCount);
        result.put("rentedCount", rentedCount);
        result.put("usageRate", totalCount > 0 ? usedCount * 100.0 / totalCount : 0);
        result.put("vacancyRate", totalCount > 0 ? freeCount * 100.0 / totalCount : 0);
        result.put("rentalRate", totalCount > 0 ? rentedCount * 100.0 / totalCount : 0);

        Map<String, Long> propertyRightDistribution = new HashMap<>();
        String[] rights = {"开发商", "业主", "公共"};
        for (String right : rights) {
            propertyRightDistribution.put(right, countByPropertyRight(right));
        }
        result.put("propertyRightDistribution", propertyRightDistribution);
        Long ownerCount = propertyRightDistribution.getOrDefault("业主", 0L);
        result.put("propertySalesRate", totalCount > 0 ? ownerCount * 100.0 / totalCount : 0);

        Object totalMonthlyRentObj = parkParkingSpaceMapper.selectObjectByQuery(
                QueryWrapper.create()
                        .select("SUM(monthly_rent)")
                        .from(ParkParkingSpace.class)
                        .where(PARK_PARKING_SPACE.DELETED.eq(0))
                        .and(PARK_PARKING_SPACE.STATUS.in(2, 3))
        );
        BigDecimal totalMonthlyRent = totalMonthlyRentObj != null ? new BigDecimal(totalMonthlyRentObj.toString()) : BigDecimal.ZERO;
        result.put("estimatedMonthlyIncome", totalMonthlyRent);

        Object avgMonthlyRentObj = parkParkingSpaceMapper.selectObjectByQuery(
                QueryWrapper.create()
                        .select("AVG(monthly_rent)")
                        .from(ParkParkingSpace.class)
                        .where(PARK_PARKING_SPACE.DELETED.eq(0))
                        .and(PARK_PARKING_SPACE.MONTHLY_RENT.isNotNull())
        );
        BigDecimal avgMonthlyRent = avgMonthlyRentObj != null ? new BigDecimal(avgMonthlyRentObj.toString()) : BigDecimal.ZERO;
        result.put("avgMonthlyRent", avgMonthlyRent);

        Map<String, Long> rentDistribution = new LinkedHashMap<>();
        rentDistribution.put("300以下", countByRentRange(null, new BigDecimal(300)));
        rentDistribution.put("300-500", countByRentRange(new BigDecimal(300), new BigDecimal(500)));
        rentDistribution.put("500-800", countByRentRange(new BigDecimal(500), new BigDecimal(800)));
        rentDistribution.put("800以上", countByRentRange(new BigDecimal(800), null));
        result.put("rentDistribution", rentDistribution);

        List<Map<String, Object>> areaStats = getAreaStats();
        result.put("areaStats", areaStats);

        return result;
    }

    private Long countBySpaceType(String spaceType) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkParkingSpace.class)
                .where(PARK_PARKING_SPACE.SPACE_TYPE.eq(spaceType))
                .and(PARK_PARKING_SPACE.DELETED.eq(0));
        return parkParkingSpaceMapper.selectCountByQuery(query);
    }

    private Long countByStatus(Integer status) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkParkingSpace.class)
                .where(PARK_PARKING_SPACE.STATUS.eq(status))
                .and(PARK_PARKING_SPACE.DELETED.eq(0));
        return parkParkingSpaceMapper.selectCountByQuery(query);
    }

    private Long countByPropertyRight(String propertyRight) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkParkingSpace.class)
                .where(PARK_PARKING_SPACE.PROPERTY_RIGHT.eq(propertyRight))
                .and(PARK_PARKING_SPACE.DELETED.eq(0));
        return parkParkingSpaceMapper.selectCountByQuery(query);
    }

    private Long countByRentRange(BigDecimal min, BigDecimal max) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkParkingSpace.class)
                .where(PARK_PARKING_SPACE.DELETED.eq(0));
        if (min != null) {
            query.and(PARK_PARKING_SPACE.MONTHLY_RENT.ge(min));
        }
        if (max != null) {
            query.and(PARK_PARKING_SPACE.MONTHLY_RENT.lt(max));
        }
        return parkParkingSpaceMapper.selectCountByQuery(query);
    }

    private List<Map<String, Object>> getAreaStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        String[] areas = {"地上露天A区", "地上访客区", "地下B1层", "地下B2层", "独立车库"};

        for (String area : areas) {
            Map<String, Object> item = new HashMap<>();
            item.put("area", area);

            Long total = parkParkingSpaceMapper.selectCountByQuery(
                    QueryWrapper.create()
                            .from(ParkParkingSpace.class)
                            .where(PARK_PARKING_SPACE.AREA.eq(area))
                            .and(PARK_PARKING_SPACE.DELETED.eq(0))
            );

            Long used = parkParkingSpaceMapper.selectCountByQuery(
                    QueryWrapper.create()
                            .from(ParkParkingSpace.class)
                            .where(PARK_PARKING_SPACE.AREA.eq(area))
                            .and(PARK_PARKING_SPACE.STATUS.in(2, 3, 4))
                            .and(PARK_PARKING_SPACE.DELETED.eq(0))
            );

            Object avgRentObj = parkParkingSpaceMapper.selectObjectByQuery(
                    QueryWrapper.create()
                            .select("AVG(monthly_rent)")
                            .from(ParkParkingSpace.class)
                            .where(PARK_PARKING_SPACE.AREA.eq(area))
                            .and(PARK_PARKING_SPACE.MONTHLY_RENT.isNotNull())
                            .and(PARK_PARKING_SPACE.DELETED.eq(0))
            );
            BigDecimal avgRent = avgRentObj != null ? new BigDecimal(avgRentObj.toString()) : BigDecimal.ZERO;

            item.put("totalCount", total);
            item.put("usedCount", used);
            item.put("usageRate", total > 0 ? used * 100.0 / total : 0);
            item.put("avgRent", avgRent);
            result.add(item);
        }
        return result;
    }

    public List<ParkParkingSpace> listAllForExport(String spaceCode, String area, String spaceType,
                                                    Integer status, String propertyRight,
                                                    BigDecimal minRent, BigDecimal maxRent) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkParkingSpace.class)
                .where(PARK_PARKING_SPACE.DELETED.eq(0));

        if (spaceCode != null && !spaceCode.isEmpty()) {
            query.and(PARK_PARKING_SPACE.SPACE_CODE.like(spaceCode));
        }
        if (area != null && !area.isEmpty()) {
            query.and(PARK_PARKING_SPACE.AREA.eq(area));
        }
        if (spaceType != null && !spaceType.isEmpty()) {
            query.and(PARK_PARKING_SPACE.SPACE_TYPE.eq(spaceType));
        }
        if (status != null) {
            query.and(PARK_PARKING_SPACE.STATUS.eq(status));
        }
        if (propertyRight != null && !propertyRight.isEmpty()) {
            query.and(PARK_PARKING_SPACE.PROPERTY_RIGHT.eq(propertyRight));
        }
        if (minRent != null) {
            query.and(PARK_PARKING_SPACE.MONTHLY_RENT.ge(minRent));
        }
        if (maxRent != null) {
            query.and(PARK_PARKING_SPACE.MONTHLY_RENT.le(maxRent));
        }

        query.orderBy(PARK_PARKING_SPACE.SPACE_CODE.asc());

        List<ParkParkingSpace> list = parkParkingSpaceMapper.selectListByQuery(query);
        for (ParkParkingSpace space : list) {
            fillOwnerName(space);
        }
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importSpaces(MultipartFile file, Long operatorId, String operatorName) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<Map<String, String>> failList = new ArrayList<>();

        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
            List<List<Object>> rows = reader.read();

            if (rows.size() <= 1) {
                throw new BusinessException("Excel文件为空或只有表头");
            }

            String[] statusLabels = {"空闲", "业主自用", "已出租", "临时占用", "维护中", "禁用"};

            for (int i = 1; i < rows.size(); i++) {
                List<Object> row = rows.get(i);
                int rowNum = i + 1;
                try {
                    String spaceCode = getCellString(row, 0);
                    String area = getCellString(row, 1);
                    String spaceType = getCellString(row, 2);
                    String locationDesc = getCellString(row, 3);
                    String orientation = getCellString(row, 4);
                    String lengthStr = getCellString(row, 5);
                    String widthStr = getCellString(row, 6);
                    String propertyRight = getCellString(row, 7);
                    String propertyCertNo = getCellString(row, 8);
                    String purchasePriceStr = getCellString(row, 9);
                    String purchaseDateStr = getCellString(row, 10);
                    String statusStr = getCellString(row, 11);
                    String monthlyRentStr = getCellString(row, 12);
                    String hourlyFeeStr = getCellString(row, 13);
                    String depositStr = getCellString(row, 14);
                    String remark = getCellString(row, 15);

                    if (spaceCode == null || spaceCode.isEmpty()) {
                        throw new BusinessException("车位编号不能为空");
                    }
                    if (area == null || area.isEmpty()) {
                        throw new BusinessException("所属区域不能为空");
                    }
                    if (spaceType == null || spaceType.isEmpty()) {
                        throw new BusinessException("车位类型不能为空");
                    }

                    ParkParkingSpace exist = getByCode(spaceCode);
                    if (exist != null) {
                        throw new BusinessException("车位编号已存在");
                    }

                    Integer status = 1;
                    for (int j = 0; j < statusLabels.length; j++) {
                        if (statusLabels[j].equals(statusStr)) {
                            status = j + 1;
                            break;
                        }
                    }

                    ParkParkingSpace space = new ParkParkingSpace();
                    space.setSpaceCode(spaceCode);
                    space.setArea(area);
                    space.setSpaceType(spaceType);
                    space.setLocationDesc(locationDesc);
                    space.setOrientation(orientation);
                    space.setPropertyRight(propertyRight);
                    space.setPropertyCertNo(propertyCertNo);
                    space.setStatus(status);
                    space.setRemark(remark);
                    space.setDeleted(0);
                    space.setCreateTime(LocalDateTime.now());
                    space.setUpdateTime(LocalDateTime.now());

                    if (lengthStr != null && !lengthStr.isEmpty()) {
                        space.setLength(new BigDecimal(lengthStr));
                    }
                    if (widthStr != null && !widthStr.isEmpty()) {
                        space.setWidth(new BigDecimal(widthStr));
                    }
                    if (purchasePriceStr != null && !purchasePriceStr.isEmpty()) {
                        space.setPurchasePrice(new BigDecimal(purchasePriceStr));
                    }
                    if (purchaseDateStr != null && !purchaseDateStr.isEmpty()) {
                        try {
                            space.setPurchaseDate(LocalDate.parse(purchaseDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        } catch (Exception e) {
                            try {
                                space.setPurchaseDate(LocalDate.parse(purchaseDateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd")));
                            } catch (Exception e2) {
                                throw new BusinessException("购买日期格式错误");
                            }
                        }
                    }
                    if (monthlyRentStr != null && !monthlyRentStr.isEmpty()) {
                        space.setMonthlyRent(new BigDecimal(monthlyRentStr));
                    }
                    if (hourlyFeeStr != null && !hourlyFeeStr.isEmpty()) {
                        space.setHourlyFee(new BigDecimal(hourlyFeeStr));
                    }
                    if (depositStr != null && !depositStr.isEmpty()) {
                        space.setDeposit(new BigDecimal(depositStr));
                    }

                    parkParkingSpaceMapper.insert(space);
                    addStatusLog(space.getId(), spaceCode, null, status, "导入创建", operatorId, operatorName);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    Map<String, String> failItem = new HashMap<>();
                    failItem.put("rowNum", String.valueOf(rowNum));
                    failItem.put("spaceCode", getCellString(row, 0));
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

    private String getCellString(List<Object> row, int index) {
        if (index >= row.size()) return null;
        Object val = row.get(index);
        return val != null ? val.toString().trim() : null;
    }

    public ExcelWriter getTemplateWriter() {
        ExcelWriter writer = ExcelUtil.getWriter();

        writer.addHeaderAlias("spaceCode", "车位编号*");
        writer.addHeaderAlias("area", "所属区域*");
        writer.addHeaderAlias("spaceType", "车位类型*");
        writer.addHeaderAlias("locationDesc", "位置描述");
        writer.addHeaderAlias("orientation", "朝向");
        writer.addHeaderAlias("length", "长度(米)");
        writer.addHeaderAlias("width", "宽度(米)");
        writer.addHeaderAlias("propertyRight", "产权归属");
        writer.addHeaderAlias("propertyCertNo", "产权证号");
        writer.addHeaderAlias("purchasePrice", "购买价格(元)");
        writer.addHeaderAlias("purchaseDate", "购买日期(yyyy-MM-dd)");
        writer.addHeaderAlias("status", "状态");
        writer.addHeaderAlias("monthlyRent", "月租金(元)");
        writer.addHeaderAlias("hourlyFee", "临时收费(元/小时)");
        writer.addHeaderAlias("deposit", "押金(元)");
        writer.addHeaderAlias("remark", "备注");

        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("spaceCode", "A-001");
        example.put("area", "地上露天A区");
        example.put("spaceType", "标准车位");
        example.put("locationDesc", "A区入口第1位");
        example.put("orientation", "室外");
        example.put("length", 5.5);
        example.put("width", 2.5);
        example.put("propertyRight", "开发商");
        example.put("propertyCertNo", "");
        example.put("purchasePrice", "");
        example.put("purchaseDate", "");
        example.put("status", "空闲");
        example.put("monthlyRent", 300);
        example.put("hourlyFee", 5);
        example.put("deposit", 500);
        example.put("remark", "示例数据");
        rows.add(example);

        Map<String, Object> example2 = new LinkedHashMap<>();
        example2.put("spaceCode", "B1-001");
        example2.put("area", "地下B1层");
        example2.put("spaceType", "充电车位");
        example2.put("locationDesc", "B1层充电区");
        example2.put("orientation", "室内");
        example2.put("length", 5.5);
        example2.put("width", 2.5);
        example2.put("propertyRight", "公共");
        example2.put("propertyCertNo", "");
        example2.put("purchasePrice", "");
        example2.put("purchaseDate", "");
        example2.put("status", "空闲");
        example2.put("monthlyRent", 600);
        example2.put("hourlyFee", 10);
        example2.put("deposit", 800);
        example2.put("remark", "配备充电桩");
        rows.add(example2);

        writer.write(rows, true);
        return writer;
    }
}
