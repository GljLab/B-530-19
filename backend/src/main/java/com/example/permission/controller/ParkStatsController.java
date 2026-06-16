package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.ParkBuilding;
import com.example.permission.service.ParkStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 统计控制器
 */
@RestController
@RequestMapping("/api/park/stats")
public class ParkStatsController {

    @Autowired
    private ParkStatsService parkStatsService;

    /**
     * 概览统计
     */
    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('park:dashboard:query')")
    public Result<Map<String, Object>> getOverviewStats() {
        Map<String, Object> result = parkStatsService.getOverviewStats();
        return Result.success(result);
    }

    /**
     * 楼宇类型分布
     */
    @GetMapping("/buildingType")
    @PreAuthorize("hasAuthority('park:dashboard:query')")
    public Result<List<Map<String, Object>>> getBuildingTypeStats() {
        List<Map<String, Object>> result = parkStatsService.getBuildingTypeStats();
        return Result.success(result);
    }

    /**
     * 楼层用途分布
     */
    @GetMapping("/floorPurpose")
    @PreAuthorize("hasAuthority('park:dashboard:query')")
    public Result<List<Map<String, Object>>> getFloorPurposeStats() {
        List<Map<String, Object>> result = parkStatsService.getFloorPurposeStats();
        return Result.success(result);
    }

    /**
     * 楼宇状态统计
     */
    @GetMapping("/buildingStatus")
    @PreAuthorize("hasAuthority('park:dashboard:query')")
    public Result<List<Map<String, Object>>> getBuildingStatusStats() {
        List<Map<String, Object>> result = parkStatsService.getBuildingStatusStats();
        return Result.success(result);
    }

    /**
     * 楼宇列表数据
     */
    @GetMapping("/buildingList")
    @PreAuthorize("hasAuthority('park:dashboard:query')")
    public Result<List<ParkBuilding>> getBuildingList() {
        List<ParkBuilding> result = parkStatsService.getBuildingList();
        return Result.success(result);
    }
}
