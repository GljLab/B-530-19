package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.ParkFacility;
import com.example.permission.entity.ParkImage;
import com.example.permission.entity.ParkInfo;
import com.example.permission.service.ParkInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 园区信息控制器
 */
@RestController
@RequestMapping("/api/park/info")
public class ParkInfoController {

    @Autowired
    private ParkInfoService parkInfoService;

    /**
     * 获取园区信息
     */
    @GetMapping
    @PreAuthorize("hasAuthority('park:overview:query')")
    public Result<ParkInfo> getInfo() {
        ParkInfo parkInfo = parkInfoService.getParkInfo();
        return Result.success(parkInfo);
    }

    /**
     * 获取园区详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('park:overview:query')")
    public Result<ParkInfo> getDetail(@PathVariable Long id) {
        ParkInfo parkInfo = parkInfoService.getParkDetail(id);
        return Result.success(parkInfo);
    }

    /**
     * 保存/更新园区信息
     */
    @PutMapping
    @PreAuthorize("hasAuthority('park:overview:edit')")
    public Result<Void> saveOrUpdate(@RequestBody ParkInfo parkInfo) {
        parkInfoService.saveOrUpdate(parkInfo);
        return Result.success("保存成功", null);
    }

    /**
     * 获取配套设施列表
     */
    @GetMapping("/facility/{parkId}")
    @PreAuthorize("hasAuthority('park:overview:query')")
    public Result<List<ParkFacility>> getFacilities(@PathVariable Long parkId) {
        List<ParkFacility> facilities = parkInfoService.getFacilities(parkId);
        return Result.success(facilities);
    }

    /**
     * 保存配套设施
     */
    @PutMapping("/facility/{parkId}")
    @PreAuthorize("hasAuthority('park:overview:edit')")
    public Result<Void> saveFacilities(@PathVariable Long parkId, @RequestBody List<ParkFacility> facilities) {
        parkInfoService.saveFacilities(parkId, facilities);
        return Result.success("保存成功", null);
    }

    /**
     * 获取园区图片列表
     */
    @GetMapping("/image/{parkId}")
    @PreAuthorize("hasAuthority('park:overview:query')")
    public Result<List<ParkImage>> getImages(@PathVariable Long parkId) {
        List<ParkImage> images = parkInfoService.getImages(parkId);
        return Result.success(images);
    }

    /**
     * 保存园区图片
     */
    @PutMapping("/image/{parkId}")
    @PreAuthorize("hasAuthority('park:overview:edit')")
    public Result<Void> saveImages(@PathVariable Long parkId, @RequestBody List<ParkImage> images) {
        parkInfoService.saveImages(parkId, images);
        return Result.success("保存成功", null);
    }
}
