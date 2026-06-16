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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.ParkPropertyTableDef.PARK_PROPERTY;
import static com.example.permission.entity.table.ParkPropertyVacancyLogTableDef.PARK_PROPERTY_VACANCY_LOG;
import static com.example.permission.entity.table.ParkBuildingTableDef.PARK_BUILDING;

@Service
public class ParkPropertyVacancyService {

    @Autowired
    private ParkPropertyMapper parkPropertyMapper;

    @Autowired
    private ParkPropertyVacancyLogMapper parkPropertyVacancyLogMapper;

    @Autowired
    private ParkBuildingMapper parkBuildingMapper;

    @Autowired
    private ParkOwnerPropertyMapper parkOwnerPropertyMapper;

    public Map<String, Object> getVacancyStats() {
        Map<String, Object> result = new HashMap<>();

        Long vacantTotal = parkPropertyMapper.selectCountByQuery(
                QueryWrapper.create()
                        .from(ParkProperty.class)
                        .where(PARK_PROPERTY.STATUS.eq(5))
                        .and(PARK_PROPERTY.DELETED.eq(0))
        );
        result.put("vacantTotal", vacantTotal);

        Long totalPropertyCount = parkPropertyMapper.selectCountByQuery(
                QueryWrapper.create()
                        .from(ParkProperty.class)
                        .where(PARK_PROPERTY.DELETED.eq(0))
        );
        result.put("totalPropertyCount", totalPropertyCount);
        result.put("vacancyRate", totalPropertyCount > 0 ? vacantTotal * 100.0 / totalPropertyCount : 0);

        Long longTermVacantCount = parkPropertyMapper.selectCountByQuery(
                QueryWrapper.create()
                        .from(ParkProperty.class)
                        .where(PARK_PROPERTY.STATUS.eq(5))
                        .and(PARK_PROPERTY.IS_LONG_TERM_VACANT.eq(1))
                        .and(PARK_PROPERTY.DELETED.eq(0))
        );
        result.put("longTermVacantCount", longTermVacantCount);

        List<Map<String, Object>> buildingVacancyStats = new ArrayList<>();
        List<ParkBuilding> buildings = parkBuildingMapper.selectListByQuery(
                QueryWrapper.create()
                        .from(ParkBuilding.class)
                        .where(PARK_BUILDING.DELETED.eq(0))
        );
        for (ParkBuilding building : buildings) {
            Map<String, Object> item = new HashMap<>();
            item.put("buildingName", building.getBuildingName());

            Long buildingTotal = parkPropertyMapper.selectCountByQuery(
                    QueryWrapper.create()
                            .from(ParkProperty.class)
                            .where(PARK_PROPERTY.BUILDING_ID.eq(building.getId()))
                            .and(PARK_PROPERTY.DELETED.eq(0))
            );
            item.put("totalPropertyCount", buildingTotal);

            Long buildingVacant = parkPropertyMapper.selectCountByQuery(
                    QueryWrapper.create()
                            .from(ParkProperty.class)
                            .where(PARK_PROPERTY.BUILDING_ID.eq(building.getId()))
                            .and(PARK_PROPERTY.STATUS.eq(5))
                            .and(PARK_PROPERTY.DELETED.eq(0))
            );
            item.put("vacantCount", buildingVacant);
            item.put("vacancyRate", buildingTotal > 0 ? buildingVacant * 100.0 / buildingTotal : 0);

            buildingVacancyStats.add(item);
        }
        result.put("buildingVacancyStats", buildingVacancyStats);

        List<Map<String, Object>> houseTypeVacancyStats = new ArrayList<>();
        String[] houseTypes = {"一室", "两室", "三室", "四室及以上"};
        for (String ht : houseTypes) {
            Map<String, Object> item = new HashMap<>();
            item.put("houseType", ht);
            Long count = parkPropertyMapper.selectCountByQuery(
                    QueryWrapper.create()
                            .from(ParkProperty.class)
                            .where(PARK_PROPERTY.STATUS.eq(5))
                            .and(PARK_PROPERTY.HOUSE_TYPE.eq(ht))
                            .and(PARK_PROPERTY.DELETED.eq(0))
            );
            item.put("vacantCount", count);
            houseTypeVacancyStats.add(item);
        }
        result.put("houseTypeVacancyStats", houseTypeVacancyStats);

        List<ParkProperty> vacantProperties = parkPropertyMapper.selectListByQuery(
                QueryWrapper.create()
                        .from(ParkProperty.class)
                        .where(PARK_PROPERTY.STATUS.eq(5))
                        .and(PARK_PROPERTY.DELETED.eq(0))
        );
        LocalDate today = LocalDate.now();
        Map<String, Long> vacancyDurationDistribution = new LinkedHashMap<>();
        long within1 = 0, between1to3 = 0, between3to6 = 0, between6to12 = 0, over12 = 0;
        for (ParkProperty p : vacantProperties) {
            if (p.getVacantSince() == null) {
                continue;
            }
            long months = ChronoUnit.MONTHS.between(p.getVacantSince(), today);
            long days = ChronoUnit.DAYS.between(p.getVacantSince(), today);
            if (days < 30) {
                within1++;
            } else if (months < 3) {
                between1to3++;
            } else if (months < 6) {
                between3to6++;
            } else if (months < 12) {
                between6to12++;
            } else {
                over12++;
            }
        }
        vacancyDurationDistribution.put("1个月以内", within1);
        vacancyDurationDistribution.put("1-3个月", between1to3);
        vacancyDurationDistribution.put("3-6个月", between3to6);
        vacancyDurationDistribution.put("6-12个月", between6to12);
        vacancyDurationDistribution.put("12个月以上", over12);
        result.put("vacancyDurationDistribution", vacancyDurationDistribution);

        Map<String, Long> vacancyReasonDistribution = new HashMap<>();
        for (ParkProperty p : vacantProperties) {
            String reason = p.getVacancyReason();
            if (reason != null && !reason.isEmpty()) {
                vacancyReasonDistribution.merge(reason, 1L, Long::sum);
            }
        }
        result.put("vacancyReasonDistribution", vacancyReasonDistribution);

        return result;
    }

    public PageResult<ParkProperty> getLongTermVacantList(Integer pageNum, Integer pageSize) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkProperty.class)
                .where(PARK_PROPERTY.STATUS.eq(5))
                .and(PARK_PROPERTY.IS_LONG_TERM_VACANT.eq(1))
                .and(PARK_PROPERTY.DELETED.eq(0))
                .orderBy(PARK_PROPERTY.VACANT_SINCE.asc());

        Page<ParkProperty> page = parkPropertyMapper.paginate(Page.of(pageNum, pageSize), query);
        List<ParkProperty> list = page.getRecords();

        for (ParkProperty property : list) {
            if (property.getBuildingId() != null) {
                ParkBuilding building = parkBuildingMapper.selectOneById(property.getBuildingId());
                if (building != null) {
                    property.setBuildingName(building.getBuildingName());
                }
            }
            if (property.getVacantSince() != null) {
                property.setVacancyDays(ChronoUnit.DAYS.between(property.getVacantSince(), LocalDate.now()));
            }
        }

        return new PageResult<>(page.getTotalRow(), list,
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    @Transactional(rollbackFor = Exception.class)
    public void recordVacancyReason(Long propertyId, String vacancyReason, String vacancyReasonRemark) {
        ParkProperty property = parkPropertyMapper.selectOneById(propertyId);
        if (property == null) {
            throw new BusinessException("房产不存在");
        }
        if (!Integer.valueOf(5).equals(property.getStatus())) {
            throw new BusinessException("房产状态不是空置");
        }

        property.setVacancyReason(vacancyReason);
        property.setVacancyReasonRemark(vacancyReasonRemark);
        property.setUpdateTime(LocalDateTime.now());
        parkPropertyMapper.update(property);

        ParkPropertyVacancyLog existingLog = parkPropertyVacancyLogMapper.selectOneByQuery(
                QueryWrapper.create()
                        .from(ParkPropertyVacancyLog.class)
                        .where(PARK_PROPERTY_VACANCY_LOG.PROPERTY_ID.eq(propertyId))
                        .and(PARK_PROPERTY_VACANCY_LOG.END_DATE.isNull())
        );

        if (existingLog != null) {
            existingLog.setVacancyReason(vacancyReason);
            existingLog.setVacancyReasonRemark(vacancyReasonRemark);
            existingLog.setUpdateTime(LocalDateTime.now());
            parkPropertyVacancyLogMapper.update(existingLog);
        } else {
            ParkPropertyVacancyLog log = new ParkPropertyVacancyLog();
            log.setPropertyId(propertyId);
            log.setPropertyCode(property.getPropertyCode());
            log.setStartDate(LocalDate.now());
            log.setVacancyReason(vacancyReason);
            log.setVacancyReasonRemark(vacancyReasonRemark);
            log.setCreateTime(LocalDateTime.now());
            log.setUpdateTime(LocalDateTime.now());
            parkPropertyVacancyLogMapper.insert(log);
        }
    }

    public int markLongTermVacant() {
        List<ParkProperty> properties = parkPropertyMapper.selectListByQuery(
                QueryWrapper.create()
                        .from(ParkProperty.class)
                        .where(PARK_PROPERTY.STATUS.eq(5))
                        .and(PARK_PROPERTY.IS_LONG_TERM_VACANT.eq(0))
                        .and(PARK_PROPERTY.DELETED.eq(0))
                        .and(PARK_PROPERTY.VACANT_SINCE.isNotNull())
        );

        LocalDate today = LocalDate.now();
        int count = 0;
        for (ParkProperty property : properties) {
            if (ChronoUnit.MONTHS.between(property.getVacantSince(), today) >= 6) {
                property.setIsLongTermVacant(1);
                property.setUpdateTime(LocalDateTime.now());
                parkPropertyMapper.update(property);
                count++;
            }
        }
        return count;
    }

    @Transactional(rollbackFor = Exception.class)
    public void onStatusChange(Long propertyId, Integer oldStatus, Integer newStatus) {
        ParkProperty property = parkPropertyMapper.selectOneById(propertyId);
        if (property == null) {
            return;
        }

        if (Integer.valueOf(5).equals(newStatus)) {
            property.setVacantSince(LocalDate.now());
            property.setIsLongTermVacant(0);
            property.setUpdateTime(LocalDateTime.now());
            parkPropertyMapper.update(property);

            ParkPropertyVacancyLog log = new ParkPropertyVacancyLog();
            log.setPropertyId(propertyId);
            log.setPropertyCode(property.getPropertyCode());
            log.setStartDate(LocalDate.now());
            log.setCreateTime(LocalDateTime.now());
            log.setUpdateTime(LocalDateTime.now());
            parkPropertyVacancyLogMapper.insert(log);
        }

        if (Integer.valueOf(5).equals(oldStatus) && !Integer.valueOf(5).equals(newStatus)) {
            property.setVacantSince(null);
            property.setVacancyReason(null);
            property.setVacancyReasonRemark(null);
            property.setIsLongTermVacant(0);
            property.setUpdateTime(LocalDateTime.now());
            parkPropertyMapper.update(property);

            ParkPropertyVacancyLog openLog = parkPropertyVacancyLogMapper.selectOneByQuery(
                    QueryWrapper.create()
                            .from(ParkPropertyVacancyLog.class)
                            .where(PARK_PROPERTY_VACANCY_LOG.PROPERTY_ID.eq(propertyId))
                            .and(PARK_PROPERTY_VACANCY_LOG.END_DATE.isNull())
            );

            if (openLog != null) {
                LocalDate today = LocalDate.now();
                openLog.setEndDate(today);
                openLog.setVacancyDays((int) ChronoUnit.DAYS.between(openLog.getStartDate(), today));
                openLog.setVacancyReason(property.getVacancyReason());
                openLog.setVacancyReasonRemark(property.getVacancyReasonRemark());
                openLog.setUpdateTime(LocalDateTime.now());
                parkPropertyVacancyLogMapper.update(openLog);
            }
        }
    }

    public List<ParkProperty> exportLongTermVacantList() {
        List<ParkProperty> list = parkPropertyMapper.selectListByQuery(
                QueryWrapper.create()
                        .from(ParkProperty.class)
                        .where(PARK_PROPERTY.STATUS.eq(5))
                        .and(PARK_PROPERTY.IS_LONG_TERM_VACANT.eq(1))
                        .and(PARK_PROPERTY.DELETED.eq(0))
        );

        for (ParkProperty property : list) {
            if (property.getBuildingId() != null) {
                ParkBuilding building = parkBuildingMapper.selectOneById(property.getBuildingId());
                if (building != null) {
                    property.setBuildingName(building.getBuildingName());
                }
            }
            if (property.getVacantSince() != null) {
                property.setVacancyDays(ChronoUnit.DAYS.between(property.getVacantSince(), LocalDate.now()));
            }
        }
        return list;
    }
}
