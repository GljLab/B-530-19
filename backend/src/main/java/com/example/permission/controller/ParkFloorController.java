package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.ParkFloor;
import com.example.permission.service.ParkFloorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 楼层控制器
 */
@RestController
@RequestMapping("/api/park/floor")
public class ParkFloorController {

    @Autowired
    private ParkFloorService parkFloorService;

    /**
     * 获取楼宇的楼层列表
     */
    @GetMapping("/list/{buildingId}")
    @PreAuthorize("hasAuthority('park:floor:list')")
    public Result<List<ParkFloor>> list(@PathVariable Long buildingId) {
        List<ParkFloor> list = parkFloorService.listByBuildingId(buildingId);
        return Result.success(list);
    }

    /**
     * 获取楼层详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('park:floor:query')")
    public Result<ParkFloor> getInfo(@PathVariable Long id) {
        ParkFloor floor = parkFloorService.getById(id);
        return Result.success(floor);
    }

    /**
     * 新增楼层
     */
    @PostMapping
    @PreAuthorize("hasAuthority('park:floor:add')")
    public Result<Void> add(@RequestBody ParkFloor floor) {
        parkFloorService.add(floor);
        return Result.success("新增成功", null);
    }

    /**
     * 更新楼层
     */
    @PutMapping
    @PreAuthorize("hasAuthority('park:floor:edit')")
    public Result<Void> update(@RequestBody ParkFloor floor) {
        parkFloorService.update(floor);
        return Result.success("修改成功", null);
    }

    /**
     * 删除楼层
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('park:floor:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        parkFloorService.delete(id);
        return Result.success("删除成功", null);
    }
}
