package com.example.permission.service;

import com.example.permission.entity.ParkBuilding;
import com.example.permission.entity.ParkFloor;
import com.example.permission.mapper.ParkBuildingMapper;
import com.example.permission.mapper.ParkFloorMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.permission.entity.table.ParkBuildingTableDef.PARK_BUILDING;
import static com.example.permission.entity.table.ParkFloorTableDef.PARK_FLOOR;

/**
 * 统计服务
 */
@Service
public class ParkStatsService {

    @Autowired
    private ParkBuildingMapper parkBuildingMapper;

    @Autowired
    private ParkFloorMapper parkFloorMapper;

    /**
     * 获取概览统计（总楼宇数、总楼层数）
     */
    public Map<String, Object> getOverviewStats() {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper buildingQuery = QueryWrapper.create()
                .from(ParkBuilding.class)
                .where(PARK_BUILDING.DELETED.eq(0));
        Long buildingCount = parkBuildingMapper.selectCountByQuery(buildingQuery);
        result.put("totalBuildings", buildingCount != null ? buildingCount : 0);

        QueryWrapper floorQuery = QueryWrapper.create()
                .from(ParkFloor.class)
                .where(PARK_FLOOR.DELETED.eq(0));
        Long floorCount = parkFloorMapper.selectCountByQuery(floorQuery);
        result.put("totalFloors", floorCount != null ? floorCount : 0);

        return result;
    }

    /**
     * 楼宇类型分布统计
     */
    public List<Map<String, Object>> getBuildingTypeStats() {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkBuilding.class)
                .where(PARK_BUILDING.DELETED.eq(0));
        List<ParkBuilding> buildings = parkBuildingMapper.selectListByQuery(query);

        Map<Integer, Long> typeCountMap = new HashMap<>();
        for (ParkBuilding building : buildings) {
            Integer type = building.getBuildingType();
            if (type != null) {
                typeCountMap.put(type, typeCountMap.getOrDefault(type, 0L) + 1);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : typeCountMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("buildingType", entry.getKey());
            item.put("count", entry.getValue());
            result.add(item);
        }
        return result;
    }

    /**
     * 楼层用途分布统计
     */
    public List<Map<String, Object>> getFloorPurposeStats() {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkFloor.class)
                .where(PARK_FLOOR.DELETED.eq(0));
        List<ParkFloor> floors = parkFloorMapper.selectListByQuery(query);

        Map<String, Long> purposeCountMap = new HashMap<>();
        for (ParkFloor floor : floors) {
            String purpose = floor.getFloorPurpose();
            if (purpose != null && !purpose.isEmpty()) {
                purposeCountMap.put(purpose, purposeCountMap.getOrDefault(purpose, 0L) + 1);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : purposeCountMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("floorPurpose", entry.getKey());
            item.put("count", entry.getValue());
            result.add(item);
        }
        return result;
    }

    /**
     * 楼宇状态统计
     */
    public List<Map<String, Object>> getBuildingStatusStats() {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkBuilding.class)
                .where(PARK_BUILDING.DELETED.eq(0));
        List<ParkBuilding> buildings = parkBuildingMapper.selectListByQuery(query);

        Map<Integer, Long> statusCountMap = new HashMap<>();
        for (ParkBuilding building : buildings) {
            Integer status = building.getStatus();
            if (status != null) {
                statusCountMap.put(status, statusCountMap.getOrDefault(status, 0L) + 1);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : statusCountMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("status", entry.getKey());
            item.put("count", entry.getValue());
            result.add(item);
        }
        return result;
    }

    /**
     * 获取楼宇列表（用于统计表格）
     */
    public List<ParkBuilding> getBuildingList() {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkBuilding.class)
                .where(PARK_BUILDING.DELETED.eq(0))
                .orderBy(PARK_BUILDING.BUILDING_CODE.asc());
        return parkBuildingMapper.selectListByQuery(query);
    }
}
