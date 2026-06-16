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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import org.springframework.web.multipart.MultipartFile;

import com.example.permission.entity.ParkPropertyTag;
import com.example.permission.entity.ParkPropertyTransfer;
import com.example.permission.entity.ParkOwner;
import com.example.permission.service.ParkPropertyVacancyService;
import com.example.permission.service.ParkPropertyTagService;
import java.time.temporal.ChronoUnit;

import static com.example.permission.entity.table.ParkPropertyStatusLogTableDef.PARK_PROPERTY_STATUS_LOG;
import static com.example.permission.entity.table.ParkPropertyTableDef.PARK_PROPERTY;
import static com.example.permission.entity.table.ParkBuildingTableDef.PARK_BUILDING;
import static com.example.permission.entity.table.ParkFloorTableDef.PARK_FLOOR;
import static com.example.permission.entity.table.ParkOwnerPropertyTableDef.PARK_OWNER_PROPERTY;
import static com.example.permission.entity.table.ParkOwnerTableDef.PARK_OWNER;
import static com.example.permission.entity.table.ParkPropertyTransferTableDef.PARK_PROPERTY_TRANSFER;

/**
 * 房产服务
 */
@Service
public class ParkPropertyService {

    @Autowired
    private ParkPropertyMapper parkPropertyMapper;

    @Autowired
    private ParkPropertyStatusLogMapper parkPropertyStatusLogMapper;

    @Autowired
    private ParkBuildingMapper parkBuildingMapper;

    @Autowired
    private ParkFloorMapper parkFloorMapper;

    @Autowired
    private ParkPropertyVacancyService vacancyService;

    @Autowired
    private ParkPropertyTagService tagService;

    @Autowired
    private ParkOwnerPropertyMapper parkOwnerPropertyMapper;

    @Autowired
    private ParkOwnerMapper parkOwnerMapper;

    @Autowired
    private ParkPropertyTransferMapper parkPropertyTransferMapper;

    /**
     * 分页查询房产列表
     */
    public PageResult<ParkProperty> pageList(Integer pageNum, Integer pageSize, String propertyCode,
                                             Long buildingId, Long floorId, Integer propertyType,
                                             Integer status, String houseType, String orientation,
                                             BigDecimal minArea, BigDecimal maxArea, String sortField,
                                             String sortOrder) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkProperty.class)
                .leftJoin(ParkBuilding.class).on(PARK_BUILDING.ID.eq(PARK_PROPERTY.BUILDING_ID))
                .where(PARK_PROPERTY.DELETED.eq(0));

        if (propertyCode != null && !propertyCode.isEmpty()) {
            query.and(PARK_PROPERTY.PROPERTY_CODE.like(propertyCode));
        }
        if (buildingId != null) {
            query.and(PARK_PROPERTY.BUILDING_ID.eq(buildingId));
        }
        if (floorId != null) {
            query.and(PARK_PROPERTY.FLOOR_ID.eq(floorId));
        }
        if (propertyType != null) {
            query.and(PARK_PROPERTY.PROPERTY_TYPE.eq(propertyType));
        }
        if (status != null) {
            query.and(PARK_PROPERTY.STATUS.eq(status));
        }
        if (houseType != null && !houseType.isEmpty()) {
            query.and(PARK_PROPERTY.HOUSE_TYPE.eq(houseType));
        }
        if (orientation != null && !orientation.isEmpty()) {
            query.and(PARK_PROPERTY.ORIENTATION.eq(orientation));
        }
        if (minArea != null) {
            query.and(PARK_PROPERTY.BUILDING_AREA.ge(minArea));
        }
        if (maxArea != null) {
            query.and(PARK_PROPERTY.BUILDING_AREA.le(maxArea));
        }

        if (sortField != null && !sortField.isEmpty()) {
            if ("propertyCode".equals(sortField)) {
                if ("desc".equals(sortOrder)) {
                    query.orderBy(PARK_PROPERTY.PROPERTY_CODE.desc());
                } else {
                    query.orderBy(PARK_PROPERTY.PROPERTY_CODE.asc());
                }
            } else if ("buildingArea".equals(sortField)) {
                if ("desc".equals(sortOrder)) {
                    query.orderBy(PARK_PROPERTY.BUILDING_AREA.desc());
                } else {
                    query.orderBy(PARK_PROPERTY.BUILDING_AREA.asc());
                }
            } else if ("floorNumber".equals(sortField)) {
                if ("desc".equals(sortOrder)) {
                    query.orderBy(PARK_PROPERTY.FLOOR_NUMBER.desc());
                } else {
                    query.orderBy(PARK_PROPERTY.FLOOR_NUMBER.asc());
                }
            } else {
                query.orderBy(PARK_PROPERTY.CREATE_TIME.desc());
            }
        } else {
            query.orderBy(PARK_PROPERTY.PROPERTY_CODE.asc());
        }

        Page<ParkProperty> page = parkPropertyMapper.paginate(Page.of(pageNum, pageSize), query);

        List<ParkProperty> list = page.getRecords();
        for (ParkProperty property : list) {
            if (property.getSpecialTags() != null && !property.getSpecialTags().isEmpty()) {
                property.setSpecialTagList(Arrays.asList(property.getSpecialTags().split(",")));
            }
        }

        return new PageResult<>(page.getTotalRow(), list,
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    /**
     * 获取房产详情
     */
    public ParkProperty getById(Long id) {
        ParkProperty property = parkPropertyMapper.selectOneById(id);
        if (property != null) {
            if (property.getSpecialTags() != null && !property.getSpecialTags().isEmpty()) {
                property.setSpecialTagList(Arrays.asList(property.getSpecialTags().split(",")));
            }
            if (property.getBuildingId() != null) {
                ParkBuilding building = parkBuildingMapper.selectOneById(property.getBuildingId());
                if (building != null) {
                    property.setBuildingName(building.getBuildingName());
                }
            }
            property.setStatusLogs(getStatusLogs(id));
            property.setTagList(tagService.getPropertyTags(id));
            if (property.getOwnerId() != null) {
                ParkOwner owner = parkOwnerMapper.selectOneById(property.getOwnerId());
                if (owner != null) {
                    property.setOwnerName(owner.getName());
                }
            }
            if (property.getTenantId() != null) {
                ParkOwner tenant = parkOwnerMapper.selectOneById(property.getTenantId());
                if (tenant != null) {
                    property.setTenantName(tenant.getName());
                }
            }
        }
        return property;
    }

    public List<ParkProperty> compareProperties(List<Long> ids) {
        if (ids == null || ids.size() < 2 || ids.size() > 4) {
            throw new BusinessException("请选择2-4个房产进行对比");
        }
        List<ParkProperty> list = new ArrayList<>();
        for (Long id : ids) {
            ParkProperty property = getById(id);
            if (property == null) {
                throw new BusinessException("房产不存在: " + id);
            }
            if (property.getStatus() != null && property.getStatus() == 5 && property.getVacantSince() != null) {
                property.setVacancyDays(ChronoUnit.DAYS.between(property.getVacantSince(), LocalDate.now()));
            }
            list.add(property);
        }
        return list;
    }

    public ParkProperty getDetailWithExtra(Long id) {
        ParkProperty property = getById(id);
        if (property != null) {
            property.setTagList(tagService.getPropertyTags(id));
            property.setTransferHistory(parkPropertyTransferMapper.selectListByQuery(
                    QueryWrapper.create()
                            .from(ParkPropertyTransfer.class)
                            .where(PARK_PROPERTY_TRANSFER.PROPERTY_ID.eq(id))
                            .orderBy(PARK_PROPERTY_TRANSFER.TRANSFER_DATE.desc())
            ));
            if (property.getOwnerId() != null) {
                ParkOwner owner = parkOwnerMapper.selectOneById(property.getOwnerId());
                if (owner != null) {
                    property.setOwnerName(owner.getName());
                }
            }
            if (property.getTenantId() != null) {
                ParkOwner tenant = parkOwnerMapper.selectOneById(property.getTenantId());
                if (tenant != null) {
                    property.setTenantName(tenant.getName());
                }
            }
            if (property.getStatus() != null && property.getStatus() == 5 && property.getVacantSince() != null) {
                property.setVacancyDays(ChronoUnit.DAYS.between(property.getVacantSince(), LocalDate.now()));
            }
        }
        return property;
    }

    /**
     * 根据房产编号查询
     */
    public ParkProperty getByCode(String propertyCode) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkProperty.class)
                .where(PARK_PROPERTY.PROPERTY_CODE.eq(propertyCode))
                .and(PARK_PROPERTY.DELETED.eq(0));
        return parkPropertyMapper.selectOneByQuery(query);
    }

    /**
     * 新增房产
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(ParkProperty property, Long operatorId, String operatorName) {
        if (property.getPropertyCode() == null || property.getPropertyCode().isEmpty()) {
            throw new BusinessException("房产编号不能为空");
        }
        if (property.getBuildingId() == null) {
            throw new BusinessException("请选择楼宇");
        }
        if (property.getFloorId() == null) {
            throw new BusinessException("请选择楼层");
        }

        ParkProperty existProperty = getByCode(property.getPropertyCode());
        if (existProperty != null) {
            throw new BusinessException("房产编号已存在");
        }

        ParkFloor floor = parkFloorMapper.selectOneById(property.getFloorId());
        if (floor != null) {
            property.setFloorNumber(floor.getFloorNumber());
            if (!floor.getBuildingId().equals(property.getBuildingId())) {
                throw new BusinessException("楼层不属于所选楼宇");
            }
        }

        if (property.getInsideArea() != null && property.getSharedArea() != null) {
            property.setBuildingArea(property.getInsideArea().add(property.getSharedArea()));
        }

        property.setDeleted(0);
        property.setStatus(property.getStatus() != null ? property.getStatus() : 1);
        property.setCreateTime(LocalDateTime.now());
        property.setUpdateTime(LocalDateTime.now());
        parkPropertyMapper.insert(property);

        addStatusLog(property.getId(), property.getPropertyCode(), null, property.getStatus(), "创建房产", operatorId, operatorName);

        updateFloorPropertyCount(property.getFloorId());
    }

    /**
     * 更新房产
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(ParkProperty property, boolean isManager) {
        ParkProperty existProperty = parkPropertyMapper.selectOneById(property.getId());
        if (existProperty == null) {
            throw new BusinessException("房产不存在");
        }

        if (property.getInsideArea() != null && property.getSharedArea() != null) {
            property.setBuildingArea(property.getInsideArea().add(property.getSharedArea()));
        }

        if (!isManager && property.getPropertyCertNo() != null) {
            property.setPropertyCertNo(existProperty.getPropertyCertNo());
        }

        property.setPropertyCode(null);
        property.setBuildingId(null);
        property.setFloorId(null);
        property.setPropertyType(null);
        property.setUpdateTime(LocalDateTime.now());
        parkPropertyMapper.update(property);
    }

    /**
     * 删除房产
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ParkProperty property = parkPropertyMapper.selectOneById(id);
        if (property == null) {
            throw new BusinessException("房产不存在");
        }

        property.setDeleted(1);
        property.setUpdateTime(LocalDateTime.now());
        parkPropertyMapper.update(property);

        updateFloorPropertyCount(property.getFloorId());
    }

    /**
     * 修改房产状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer newStatus, String changeReason, Long operatorId, String operatorName) {
        ParkProperty property = parkPropertyMapper.selectOneById(id);
        if (property == null) {
            throw new BusinessException("房产不存在");
        }

        Integer oldStatus = property.getStatus();
        if (Objects.equals(oldStatus, newStatus)) {
            throw new BusinessException("状态未变更");
        }

        property.setStatus(newStatus);
        property.setUpdateTime(LocalDateTime.now());
        parkPropertyMapper.update(property);

        addStatusLog(id, property.getPropertyCode(), oldStatus, newStatus, changeReason, operatorId, operatorName);
        vacancyService.onStatusChange(id, oldStatus, newStatus);
    }

    /**
     * 获取状态变更日志
     */
    public List<ParkPropertyStatusLog> getStatusLogs(Long propertyId) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkPropertyStatusLog.class)
                .where(PARK_PROPERTY_STATUS_LOG.PROPERTY_ID.eq(propertyId))
                .orderBy(PARK_PROPERTY_STATUS_LOG.CREATE_TIME.desc());
        return parkPropertyStatusLogMapper.selectListByQuery(query);
    }

    /**
     * 批量创建房产
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchCreate(Long buildingId, Long floorId, Integer propertyType, String propertySubType,
                                           String prefix, Integer startNum, Integer endNum, BigDecimal buildingArea,
                                           BigDecimal insideArea, BigDecimal sharedArea, String houseType,
                                           String orientation, String decorationStatus, String propertyRightType,
                                           Integer propertyRightYears, Long operatorId, String operatorName) {
        ParkFloor floor = parkFloorMapper.selectOneById(floorId);
        if (floor == null) {
            throw new BusinessException("楼层不存在");
        }
        if (!floor.getBuildingId().equals(buildingId)) {
            throw new BusinessException("楼层不属于所选楼宇");
        }

        int successCount = 0;
        int failCount = 0;
        List<Map<String, String>> failList = new ArrayList<>();

        for (int i = startNum; i <= endNum; i++) {
            String roomNum = String.format("%02d", i);
            String propertyCode = prefix + roomNum;

            try {
                ParkProperty existProperty = getByCode(propertyCode);
                if (existProperty != null) {
                    failCount++;
                    Map<String, String> failItem = new HashMap<>();
                    failItem.put("propertyCode", propertyCode);
                    failItem.put("reason", "房产编号已存在");
                    failList.add(failItem);
                    continue;
                }

                ParkProperty property = new ParkProperty();
                property.setPropertyCode(propertyCode);
                property.setBuildingId(buildingId);
                property.setFloorId(floorId);
                property.setFloorNumber(floor.getFloorNumber());
                property.setPropertyType(propertyType);
                property.setPropertySubType(propertySubType);
                property.setBuildingArea(buildingArea);
                property.setInsideArea(insideArea);
                property.setSharedArea(sharedArea);
                property.setHouseType(houseType);
                property.setOrientation(orientation);
                property.setDecorationStatus(decorationStatus);
                property.setPropertyRightType(propertyRightType);
                property.setPropertyRightYears(propertyRightYears);
                property.setStatus(1);
                property.setDeleted(0);
                property.setCreateTime(LocalDateTime.now());
                property.setUpdateTime(LocalDateTime.now());
                parkPropertyMapper.insert(property);

                addStatusLog(property.getId(), propertyCode, null, 1, "批量创建", operatorId, operatorName);

                successCount++;
            } catch (Exception e) {
                failCount++;
                Map<String, String> failItem = new HashMap<>();
                failItem.put("propertyCode", propertyCode);
                failItem.put("reason", e.getMessage());
                failList.add(failItem);
            }
        }

        updateFloorPropertyCount(floorId);

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failList", failList);
        return result;
    }

    /**
     * 预览批量创建的房产
     */
    public List<ParkProperty> previewBatchCreate(Long buildingId, Long floorId, Integer propertyType, String propertySubType,
                                                 String prefix, Integer startNum, Integer endNum, BigDecimal buildingArea,
                                                 BigDecimal insideArea, BigDecimal sharedArea, String houseType,
                                                 String orientation, String decorationStatus) {
        List<ParkProperty> list = new ArrayList<>();
        ParkFloor floor = parkFloorMapper.selectOneById(floorId);
        String floorNumber = floor != null ? floor.getFloorNumber() : "";
        ParkBuilding building = parkBuildingMapper.selectOneById(buildingId);
        String buildingName = building != null ? building.getBuildingName() : "";

        for (int i = startNum; i <= endNum; i++) {
            String roomNum = String.format("%02d", i);
            String propertyCode = prefix + roomNum;

            ParkProperty property = new ParkProperty();
            property.setPropertyCode(propertyCode);
            property.setBuildingId(buildingId);
            property.setBuildingName(buildingName);
            property.setFloorId(floorId);
            property.setFloorNumber(floorNumber);
            property.setPropertyType(propertyType);
            property.setPropertySubType(propertySubType);
            property.setBuildingArea(buildingArea);
            property.setInsideArea(insideArea);
            property.setSharedArea(sharedArea);
            property.setHouseType(houseType);
            property.setOrientation(orientation);
            property.setDecorationStatus(decorationStatus);
            property.setStatus(1);

            list.add(property);
        }
        return list;
    }

    /**
     * 统计分析
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper totalQuery = QueryWrapper.create()
                .from(ParkProperty.class)
                .where(PARK_PROPERTY.DELETED.eq(0));
        Long totalCount = parkPropertyMapper.selectCountByQuery(totalQuery);
        result.put("totalCount", totalCount);

        Object totalBuildingAreaObj = parkPropertyMapper.selectObjectByQuery(
                QueryWrapper.create()
                        .select("SUM(building_area)")
                        .from(ParkProperty.class)
                        .where(PARK_PROPERTY.DELETED.eq(0))
        );
        BigDecimal totalBuildingArea = totalBuildingAreaObj != null ? new BigDecimal(totalBuildingAreaObj.toString()) : BigDecimal.ZERO;
        result.put("totalBuildingArea", totalBuildingArea);

        Object totalInsideAreaObj = parkPropertyMapper.selectObjectByQuery(
                QueryWrapper.create()
                        .select("SUM(inside_area)")
                        .from(ParkProperty.class)
                        .where(PARK_PROPERTY.DELETED.eq(0))
        );
        BigDecimal totalInsideArea = totalInsideAreaObj != null ? new BigDecimal(totalInsideAreaObj.toString()) : BigDecimal.ZERO;
        result.put("totalInsideArea", totalInsideArea != null ? totalInsideArea : BigDecimal.ZERO);

        Map<String, Long> typeDistribution = new HashMap<>();
        Long residentialCount = countByPropertyType(1);
        Long commercialCount = countByPropertyType(2);
        Long otherCount = countByPropertyType(3);
        typeDistribution.put("residential", residentialCount);
        typeDistribution.put("commercial", commercialCount);
        typeDistribution.put("other", otherCount);
        result.put("typeDistribution", typeDistribution);

        Map<String, Long> statusDistribution = new HashMap<>();
        for (int i = 1; i <= 7; i++) {
            statusDistribution.put(String.valueOf(i), countByStatus(i));
        }
        result.put("statusDistribution", statusDistribution);

        Long occupiedCount = countByStatus(3) + countByStatus(4);
        Long vacantCount = countByStatus(5);
        Long rentedCount = countByStatus(4);
        result.put("occupancyRate", totalCount > 0 ? occupiedCount * 100.0 / totalCount : 0);
        result.put("vacancyRate", totalCount > 0 ? vacantCount * 100.0 / totalCount : 0);
        result.put("rentalRate", totalCount > 0 ? rentedCount * 100.0 / totalCount : 0);

        Map<String, Long> houseTypeDistribution = new HashMap<>();
        String[] houseTypes = {"一室", "两室", "三室", "四室及以上"};
        for (String ht : houseTypes) {
            houseTypeDistribution.put(ht, countByHouseType(ht));
        }
        result.put("houseTypeDistribution", houseTypeDistribution);

        Map<String, Long> areaDistribution = new LinkedHashMap<>();
        areaDistribution.put("50以下", countByAreaRange(null, new BigDecimal(50)));
        areaDistribution.put("50-80", countByAreaRange(new BigDecimal(50), new BigDecimal(80)));
        areaDistribution.put("80-120", countByAreaRange(new BigDecimal(80), new BigDecimal(120)));
        areaDistribution.put("120-150", countByAreaRange(new BigDecimal(120), new BigDecimal(150)));
        areaDistribution.put("150以上", countByAreaRange(new BigDecimal(150), null));
        result.put("areaDistribution", areaDistribution);

        BigDecimal avgArea = totalCount > 0 && totalBuildingArea != null
                ? totalBuildingArea.divide(new BigDecimal(totalCount), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;
        result.put("avgArea", avgArea);

        List<Map<String, Object>> buildingStats = getBuildingStats();
        result.put("buildingStats", buildingStats);

        Map<String, Long> orientationDistribution = new HashMap<>();
        String[] orientations = {"东", "南", "西", "北", "东南", "西南", "东北", "西北"};
        for (String o : orientations) {
            orientationDistribution.put(o, countByOrientation(o));
        }
        result.put("orientationDistribution", orientationDistribution);

        return result;
    }

    private Long countByPropertyType(Integer type) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkProperty.class)
                .where(PARK_PROPERTY.PROPERTY_TYPE.eq(type))
                .and(PARK_PROPERTY.DELETED.eq(0));
        return parkPropertyMapper.selectCountByQuery(query);
    }

    private Long countByStatus(Integer status) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkProperty.class)
                .where(PARK_PROPERTY.STATUS.eq(status))
                .and(PARK_PROPERTY.DELETED.eq(0));
        return parkPropertyMapper.selectCountByQuery(query);
    }

    private Long countByHouseType(String houseType) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkProperty.class)
                .where(PARK_PROPERTY.HOUSE_TYPE.eq(houseType))
                .and(PARK_PROPERTY.DELETED.eq(0));
        return parkPropertyMapper.selectCountByQuery(query);
    }

    private Long countByAreaRange(BigDecimal min, BigDecimal max) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkProperty.class)
                .where(PARK_PROPERTY.DELETED.eq(0));
        if (min != null) {
            query.and(PARK_PROPERTY.BUILDING_AREA.ge(min));
        }
        if (max != null) {
            query.and(PARK_PROPERTY.BUILDING_AREA.lt(max));
        }
        return parkPropertyMapper.selectCountByQuery(query);
    }

    private Long countByOrientation(String orientation) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkProperty.class)
                .where(PARK_PROPERTY.ORIENTATION.eq(orientation))
                .and(PARK_PROPERTY.DELETED.eq(0));
        return parkPropertyMapper.selectCountByQuery(query);
    }

    private List<Map<String, Object>> getBuildingStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<ParkBuilding> buildings = parkBuildingMapper.selectListByQuery(
                QueryWrapper.create()
                        .from(ParkBuilding.class)
                        .where(PARK_BUILDING.DELETED.eq(0))
                        .orderBy(PARK_BUILDING.BUILDING_CODE.asc())
        );

        for (ParkBuilding building : buildings) {
            Map<String, Object> item = new HashMap<>();
            item.put("buildingId", building.getId());
            item.put("buildingName", building.getBuildingName());

            Long total = parkPropertyMapper.selectCountByQuery(
                    QueryWrapper.create()
                            .from(ParkProperty.class)
                            .where(PARK_PROPERTY.BUILDING_ID.eq(building.getId()))
                            .and(PARK_PROPERTY.DELETED.eq(0))
            );

            Long occupied = parkPropertyMapper.selectCountByQuery(
                    QueryWrapper.create()
                            .from(ParkProperty.class)
                            .where(PARK_PROPERTY.BUILDING_ID.eq(building.getId()))
                            .and(PARK_PROPERTY.STATUS.in(3, 4))
                            .and(PARK_PROPERTY.DELETED.eq(0))
            );

            item.put("totalCount", total);
            item.put("occupancyRate", total > 0 ? occupied * 100.0 / total : 0);
            result.add(item);
        }
        return result;
    }

    /**
     * 批量删除房产
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        Set<Long> floorIds = new HashSet<>();
        for (Long id : ids) {
            ParkProperty property = parkPropertyMapper.selectOneById(id);
            if (property != null) {
                floorIds.add(property.getFloorId());
                property.setDeleted(1);
                property.setUpdateTime(LocalDateTime.now());
                parkPropertyMapper.update(property);
            }
        }
        for (Long floorId : floorIds) {
            updateFloorPropertyCount(floorId);
        }
    }

    /**
     * 批量修改状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchChangeStatus(List<Long> ids, Integer newStatus, String changeReason, Long operatorId, String operatorName) {
        for (Long id : ids) {
            changeStatus(id, newStatus, changeReason, operatorId, operatorName);
        }
    }

    private void addStatusLog(Long propertyId, String propertyCode, Integer oldStatus, Integer newStatus,
                              String changeReason, Long operatorId, String operatorName) {
        ParkPropertyStatusLog log = new ParkPropertyStatusLog();
        log.setPropertyId(propertyId);
        log.setPropertyCode(propertyCode);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setChangeReason(changeReason);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setCreateTime(LocalDateTime.now());
        parkPropertyStatusLogMapper.insert(log);
    }

    private void updateFloorPropertyCount(Long floorId) {
        if (floorId == null) return;
        Long count = parkPropertyMapper.selectCountByQuery(
                QueryWrapper.create()
                        .from(ParkProperty.class)
                        .where(PARK_PROPERTY.FLOOR_ID.eq(floorId))
                        .and(PARK_PROPERTY.DELETED.eq(0))
        );
        ParkFloor floor = new ParkFloor();
        floor.setId(floorId);
        floor.setPropertyCount(count != null ? count.intValue() : 0);
        floor.setUpdateTime(LocalDateTime.now());
        parkFloorMapper.update(floor);
    }

    /**
     * 获取所有房产列表（导出用）
     */
    public List<ParkProperty> listAllForExport(String propertyCode, Long buildingId, Long floorId,
                                               Integer propertyType, Integer status, String houseType,
                                               String orientation, BigDecimal minArea, BigDecimal maxArea) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkProperty.class)
                .leftJoin(ParkBuilding.class).on(PARK_BUILDING.ID.eq(PARK_PROPERTY.BUILDING_ID))
                .where(PARK_PROPERTY.DELETED.eq(0));

        if (propertyCode != null && !propertyCode.isEmpty()) {
            query.and(PARK_PROPERTY.PROPERTY_CODE.like(propertyCode));
        }
        if (buildingId != null) {
            query.and(PARK_PROPERTY.BUILDING_ID.eq(buildingId));
        }
        if (floorId != null) {
            query.and(PARK_PROPERTY.FLOOR_ID.eq(floorId));
        }
        if (propertyType != null) {
            query.and(PARK_PROPERTY.PROPERTY_TYPE.eq(propertyType));
        }
        if (status != null) {
            query.and(PARK_PROPERTY.STATUS.eq(status));
        }
        if (houseType != null && !houseType.isEmpty()) {
            query.and(PARK_PROPERTY.HOUSE_TYPE.eq(houseType));
        }
        if (orientation != null && !orientation.isEmpty()) {
            query.and(PARK_PROPERTY.ORIENTATION.eq(orientation));
        }
        if (minArea != null) {
            query.and(PARK_PROPERTY.BUILDING_AREA.ge(minArea));
        }
        if (maxArea != null) {
            query.and(PARK_PROPERTY.BUILDING_AREA.le(maxArea));
        }

        query.orderBy(PARK_PROPERTY.PROPERTY_CODE.asc());

        List<ParkProperty> list = parkPropertyMapper.selectListByQuery(query);
        for (ParkProperty property : list) {
            if (property.getBuildingId() != null) {
                ParkBuilding building = parkBuildingMapper.selectOneById(property.getBuildingId());
                if (building != null) {
                    property.setBuildingName(building.getBuildingName());
                }
            }
        }
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importProperties(MultipartFile file, Long operatorId, String operatorName) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<Map<String, String>> failList = new ArrayList<>();
        Set<Long> floorIds = new HashSet<>();

        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
            List<List<Object>> rows = reader.read();

            if (rows.size() <= 1) {
                throw new BusinessException("Excel文件为空或只有表头");
            }

            Map<String, Long> buildingNameIdMap = new HashMap<>();
            List<ParkBuilding> buildings = parkBuildingMapper.selectListByQuery(
                    QueryWrapper.create().from(ParkBuilding.class).where(PARK_BUILDING.DELETED.eq(0))
            );
            for (ParkBuilding b : buildings) {
                buildingNameIdMap.put(b.getBuildingName(), b.getId());
            }

            Map<String, Map<String, Long>> buildingFloorMap = new HashMap<>();
            List<ParkFloor> floors = parkFloorMapper.selectListByQuery(
                    QueryWrapper.create().from(ParkFloor.class)
            );
            for (ParkFloor f : floors) {
                String buildingName = "";
                ParkBuilding b = parkBuildingMapper.selectOneById(f.getBuildingId());
                if (b != null) {
                    buildingName = b.getBuildingName();
                }
                if (!buildingFloorMap.containsKey(buildingName)) {
                    buildingFloorMap.put(buildingName, new HashMap<>());
                }
                buildingFloorMap.get(buildingName).put(f.getFloorNumber(), f.getId());
            }

            for (int i = 1; i < rows.size(); i++) {
                List<Object> row = rows.get(i);
                int rowNum = i + 1;
                try {
                    String propertyCode = getCellString(row, 0);
                    String buildingName = getCellString(row, 1);
                    String floorNumber = getCellString(row, 2);
                    String typeStr = getCellString(row, 3);
                    String subType = getCellString(row, 4);
                    String buildingAreaStr = getCellString(row, 5);
                    String insideAreaStr = getCellString(row, 6);
                    String sharedAreaStr = getCellString(row, 7);
                    String houseType = getCellString(row, 8);
                    String orientation = getCellString(row, 9);
                    String decorationStatus = getCellString(row, 10);
                    String rightType = getCellString(row, 11);
                    String rightYearsStr = getCellString(row, 12);
                    String certNo = getCellString(row, 13);
                    String deliveryDateStr = getCellString(row, 14);
                    String statusStr = getCellString(row, 15);
                    String remark = getCellString(row, 16);

                    if (propertyCode == null || propertyCode.isEmpty()) {
                        throw new BusinessException("房产编号不能为空");
                    }

                    ParkProperty exist = getByCode(propertyCode);
                    if (exist != null) {
                        throw new BusinessException("房产编号已存在");
                    }

                    Long buildingId = buildingNameIdMap.get(buildingName);
                    if (buildingId == null) {
                        throw new BusinessException("楼宇不存在: " + buildingName);
                    }

                    Long floorId = null;
                    Map<String, Long> floorMap = buildingFloorMap.get(buildingName);
                    if (floorMap != null) {
                        floorId = floorMap.get(floorNumber);
                    }
                    if (floorId == null) {
                        throw new BusinessException("楼层不存在: " + floorNumber);
                    }

                    Integer propertyType = 1;
                    if ("住宅".equals(typeStr)) propertyType = 1;
                    else if ("商业".equals(typeStr)) propertyType = 2;
                    else if ("其他".equals(typeStr)) propertyType = 3;

                    Integer status = 1;
                    if ("待售".equals(statusStr)) status = 1;
                    else if ("已售未交付".equals(statusStr)) status = 2;
                    else if ("业主自住".equals(statusStr)) status = 3;
                    else if ("业主出租".equals(statusStr)) status = 4;
                    else if ("空置".equals(statusStr)) status = 5;
                    else if ("装修中".equals(statusStr)) status = 6;
                    else if ("查封".equals(statusStr)) status = 7;

                    ParkProperty property = new ParkProperty();
                    property.setPropertyCode(propertyCode);
                    property.setBuildingId(buildingId);
                    property.setFloorId(floorId);
                    property.setFloorNumber(floorNumber);
                    property.setPropertyType(propertyType);
                    property.setPropertySubType(subType);
                    property.setHouseType(houseType);
                    property.setOrientation(orientation);
                    property.setDecorationStatus(decorationStatus);
                    property.setPropertyRightType(rightType);
                    property.setPropertyCertNo(certNo);
                    property.setStatus(status);
                    property.setRemark(remark);
                    property.setDeleted(0);
                    property.setCreateTime(LocalDateTime.now());
                    property.setUpdateTime(LocalDateTime.now());

                    if (buildingAreaStr != null && !buildingAreaStr.isEmpty()) {
                        property.setBuildingArea(new BigDecimal(buildingAreaStr));
                    }
                    if (insideAreaStr != null && !insideAreaStr.isEmpty()) {
                        property.setInsideArea(new BigDecimal(insideAreaStr));
                    }
                    if (sharedAreaStr != null && !sharedAreaStr.isEmpty()) {
                        property.setSharedArea(new BigDecimal(sharedAreaStr));
                    }
                    if (property.getInsideArea() != null && property.getSharedArea() != null) {
                        property.setBuildingArea(property.getInsideArea().add(property.getSharedArea()));
                    }
                    if (rightYearsStr != null && !rightYearsStr.isEmpty()) {
                        property.setPropertyRightYears(Integer.parseInt(rightYearsStr.replace("年", "")));
                    }
                    if (deliveryDateStr != null && !deliveryDateStr.isEmpty()) {
                        try {
                            property.setDeliveryDate(LocalDate.parse(deliveryDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        } catch (Exception e) {
                            try {
                                property.setDeliveryDate(LocalDate.parse(deliveryDateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd")));
                            } catch (Exception e2) {
                                throw new BusinessException("交付日期格式错误");
                            }
                        }
                    }

                    parkPropertyMapper.insert(property);
                    addStatusLog(property.getId(), propertyCode, null, status, "导入创建", operatorId, operatorName);
                    floorIds.add(floorId);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    Map<String, String> failItem = new HashMap<>();
                    failItem.put("rowNum", String.valueOf(rowNum));
                    failItem.put("propertyCode", getCellString(row, 0));
                    failItem.put("reason", e.getMessage());
                    failList.add(failItem);
                }
            }

            for (Long floorId : floorIds) {
                updateFloorPropertyCount(floorId);
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

        writer.addHeaderAlias("propertyCode", "房产编号");
        writer.addHeaderAlias("buildingName", "楼宇名称");
        writer.addHeaderAlias("floorNumber", "楼层号");
        writer.addHeaderAlias("propertyType", "房产类型");
        writer.addHeaderAlias("propertySubType", "子类型");
        writer.addHeaderAlias("buildingArea", "建筑面积(㎡)");
        writer.addHeaderAlias("insideArea", "套内面积(㎡)");
        writer.addHeaderAlias("sharedArea", "公摊面积(㎡)");
        writer.addHeaderAlias("houseType", "户型");
        writer.addHeaderAlias("orientation", "朝向");
        writer.addHeaderAlias("decorationStatus", "装修状况");
        writer.addHeaderAlias("propertyRightType", "产权性质");
        writer.addHeaderAlias("propertyRightYears", "产权年限");
        writer.addHeaderAlias("propertyCertNo", "产权证号");
        writer.addHeaderAlias("deliveryDate", "交付日期(yyyy-MM-dd)");
        writer.addHeaderAlias("status", "状态");
        writer.addHeaderAlias("remark", "备注");

        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("propertyCode", "1-101");
        example.put("buildingName", "1号楼");
        example.put("floorNumber", "1层");
        example.put("propertyType", "住宅");
        example.put("propertySubType", "普通住宅");
        example.put("buildingArea", 100.5);
        example.put("insideArea", 80.0);
        example.put("sharedArea", 20.5);
        example.put("houseType", "两室");
        example.put("orientation", "南");
        example.put("decorationStatus", "毛坯");
        example.put("propertyRightType", "商品房");
        example.put("propertyRightYears", "70年");
        example.put("propertyCertNo", "");
        example.put("deliveryDate", "2024-01-01");
        example.put("status", "待售");
        example.put("remark", "示例数据");
        rows.add(example);

        writer.write(rows, true);
        return writer;
    }
}
