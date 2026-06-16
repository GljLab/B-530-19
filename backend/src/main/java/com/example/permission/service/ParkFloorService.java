package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.ParkBuilding;
import com.example.permission.entity.ParkFloor;
import com.example.permission.mapper.ParkBuildingMapper;
import com.example.permission.mapper.ParkFloorMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.permission.entity.table.ParkBuildingTableDef.PARK_BUILDING;
import static com.example.permission.entity.table.ParkFloorTableDef.PARK_FLOOR;

/**
 * 楼层服务
 */
@Service
public class ParkFloorService {

    @Autowired
    private ParkFloorMapper parkFloorMapper;

    @Autowired
    private ParkBuildingMapper parkBuildingMapper;

    /**
     * 根据楼宇ID获取楼层列表
     */
    public List<ParkFloor> listByBuildingId(Long buildingId) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkFloor.class)
                .where(PARK_FLOOR.BUILDING_ID.eq(buildingId))
                .and(PARK_FLOOR.DELETED.eq(0))
                .orderBy(PARK_FLOOR.SORT_ORDER.asc());
        return parkFloorMapper.selectListByQuery(query);
    }

    /**
     * 获取楼层详情
     */
    public ParkFloor getById(Long id) {
        return parkFloorMapper.selectOneById(id);
    }

    /**
     * 新增楼层（检查同楼宇下楼层号不重复）
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(ParkFloor floor) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkFloor.class)
                .where(PARK_FLOOR.BUILDING_ID.eq(floor.getBuildingId()))
                .and(PARK_FLOOR.FLOOR_NUMBER.eq(floor.getFloorNumber()))
                .and(PARK_FLOOR.DELETED.eq(0));
        Long count = parkFloorMapper.selectCountByQuery(query);
        if (count != null && count > 0) {
            throw new BusinessException("同楼宇下楼层号已存在");
        }

        floor.setDeleted(0);
        floor.setStatus(floor.getStatus() != null ? floor.getStatus() : 1);
        floor.setCreateTime(LocalDateTime.now());
        floor.setUpdateTime(LocalDateTime.now());
        parkFloorMapper.insert(floor);
    }

    /**
     * 更新楼层
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(ParkFloor floor) {
        ParkFloor existFloor = parkFloorMapper.selectOneById(floor.getId());
        if (existFloor == null) {
            throw new BusinessException("楼层不存在");
        }

        if (floor.getFloorNumber() != null && 
            !floor.getFloorNumber().equals(existFloor.getFloorNumber())) {
            QueryWrapper query = QueryWrapper.create()
                    .from(ParkFloor.class)
                    .where(PARK_FLOOR.BUILDING_ID.eq(existFloor.getBuildingId()))
                    .and(PARK_FLOOR.FLOOR_NUMBER.eq(floor.getFloorNumber()))
                    .and(PARK_FLOOR.DELETED.eq(0));
            Long count = parkFloorMapper.selectCountByQuery(query);
            if (count != null && count > 0) {
                throw new BusinessException("同楼宇下楼层号已存在");
            }
        }

        floor.setUpdateTime(LocalDateTime.now());
        parkFloorMapper.update(floor);
    }

    /**
     * 删除楼层
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ParkFloor floor = parkFloorMapper.selectOneById(id);
        if (floor == null) {
            throw new BusinessException("楼层不存在");
        }

        floor.setDeleted(1);
        floor.setUpdateTime(LocalDateTime.now());
        parkFloorMapper.update(floor);
    }

    /**
     * 统计楼宇楼层数量
     */
    public Long countByBuildingId(Long buildingId) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkFloor.class)
                .where(PARK_FLOOR.BUILDING_ID.eq(buildingId))
                .and(PARK_FLOOR.DELETED.eq(0));
        return parkFloorMapper.selectCountByQuery(query);
    }

    /**
     * 统计园区总楼层数
     */
    public Long countByParkId(Long parkId) {
        QueryWrapper buildingQuery = QueryWrapper.create()
                .from(ParkBuilding.class)
                .where(PARK_BUILDING.PARK_ID.eq(parkId))
                .and(PARK_BUILDING.DELETED.eq(0));
        List<ParkBuilding> buildings = parkBuildingMapper.selectListByQuery(buildingQuery);

        long totalCount = 0;
        for (ParkBuilding building : buildings) {
            Long count = countByBuildingId(building.getId());
            if (count != null) {
                totalCount += count;
            }
        }
        return totalCount;
    }
}
