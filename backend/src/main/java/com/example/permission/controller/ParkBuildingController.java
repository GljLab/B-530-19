package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.ParkBuilding;
import com.example.permission.entity.ParkBuildingImage;
import com.example.permission.service.ParkBuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 楼宇控制器
 */
@RestController
@RequestMapping("/api/park/building")
public class ParkBuildingController {

    @Autowired
    private ParkBuildingService parkBuildingService;

    /**
     * 分页查询楼宇列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('park:building:list')")
    public Result<PageResult<ParkBuilding>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long parkId,
            @RequestParam(required = false) String buildingName,
            @RequestParam(required = false) Integer buildingType,
            @RequestParam(required = false) Integer status) {
        PageResult<ParkBuilding> result = parkBuildingService.pageList(pageNum, pageSize, parkId, buildingName, buildingType, status);
        return Result.success(result);
    }

    /**
     * 获取所有楼宇列表
     */
    @GetMapping("/listAll")
    @PreAuthorize("hasAuthority('park:building:query')")
    public Result<List<ParkBuilding>> listAll(@RequestParam(required = false) Long parkId) {
        List<ParkBuilding> list = parkBuildingService.listAll(parkId);
        return Result.success(list);
    }

    /**
     * 获取楼宇详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('park:building:query')")
    public Result<ParkBuilding> getInfo(@PathVariable Long id) {
        ParkBuilding building = parkBuildingService.getById(id);
        return Result.success(building);
    }

    /**
     * 获取楼宇完整详情（含图片楼层）
     */
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAuthority('park:building:query')")
    public Result<ParkBuilding> getDetail(@PathVariable Long id) {
        ParkBuilding building = parkBuildingService.getDetail(id);
        return Result.success(building);
    }

    /**
     * 新增楼宇
     */
    @PostMapping
    @PreAuthorize("hasAuthority('park:building:add')")
    public Result<Void> add(@RequestBody ParkBuilding building) {
        parkBuildingService.add(building);
        return Result.success("新增成功", null);
    }

    /**
     * 更新楼宇
     */
    @PutMapping
    @PreAuthorize("hasAuthority('park:building:edit')")
    public Result<Void> update(@RequestBody ParkBuilding building) {
        parkBuildingService.update(building);
        return Result.success("修改成功", null);
    }

    /**
     * 删除楼宇
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('park:building:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        parkBuildingService.delete(id);
        return Result.success("删除成功", null);
    }

    /**
     * 获取楼宇图片
     */
    @GetMapping("/image/{buildingId}")
    @PreAuthorize("hasAuthority('park:building:query')")
    public Result<List<ParkBuildingImage>> getImages(@PathVariable Long buildingId) {
        List<ParkBuildingImage> images = parkBuildingService.getImages(buildingId);
        return Result.success(images);
    }

    /**
     * 保存楼宇图片
     */
    @PutMapping("/image/{buildingId}")
    @PreAuthorize("hasAuthority('park:building:edit')")
    public Result<Void> saveImages(@PathVariable Long buildingId, @RequestBody List<ParkBuildingImage> images) {
        parkBuildingService.saveImages(buildingId, images);
        return Result.success("保存成功", null);
    }
}
