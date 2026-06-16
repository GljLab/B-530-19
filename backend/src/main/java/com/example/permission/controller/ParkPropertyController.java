package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.ParkProperty;
import com.example.permission.entity.ParkPropertyStatusLog;
import com.example.permission.security.LoginUser;
import com.example.permission.service.ParkPropertyService;
import cn.hutool.poi.excel.ExcelWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * 房产控制器
 */
@RestController
@RequestMapping("/api/park/property")
public class ParkPropertyController {

    @Autowired
    private ParkPropertyService parkPropertyService;

    /**
     * 分页查询房产列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('park:property:list')")
    public Result<PageResult<ParkProperty>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String propertyCode,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Long floorId,
            @RequestParam(required = false) Integer propertyType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String houseType,
            @RequestParam(required = false) String orientation,
            @RequestParam(required = false) BigDecimal minArea,
            @RequestParam(required = false) BigDecimal maxArea,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        PageResult<ParkProperty> result = parkPropertyService.pageList(
                pageNum, pageSize, propertyCode, buildingId, floorId, propertyType,
                status, houseType, orientation, minArea, maxArea, sortField, sortOrder);
        return Result.success(result);
    }

    /**
     * 获取房产详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('park:property:query')")
    public Result<ParkProperty> getInfo(@PathVariable Long id) {
        ParkProperty property = parkPropertyService.getById(id);
        return Result.success(property);
    }

    /**
     * 新增房产
     */
    @PostMapping
    @PreAuthorize("hasAuthority('park:property:add')")
    public Result<Void> add(@RequestBody ParkProperty property) {
        Long operatorId = null;
        String operatorName = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof LoginUser) {
                LoginUser loginUser = (LoginUser) auth.getPrincipal();
                operatorId = loginUser.getUserId();
                operatorName = loginUser.getUser() != null ? loginUser.getUser().getNickname() : loginUser.getUsername();
            }
        } catch (Exception ignored) {}

        parkPropertyService.add(property, operatorId, operatorName);
        return Result.success("新增成功", null);
    }

    /**
     * 更新房产
     */
    @PutMapping
    @PreAuthorize("hasAuthority('park:property:edit')")
    public Result<Void> update(@RequestBody ParkProperty property) {
        boolean isManager = false;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof LoginUser) {
                LoginUser loginUser = (LoginUser) auth.getPrincipal();
                if (loginUser.getPermissions() != null) {
                    isManager = loginUser.getPermissions().contains("park:property:delete");
                }
            }
        } catch (Exception ignored) {}

        parkPropertyService.update(property, isManager);
        return Result.success("修改成功", null);
    }

    /**
     * 删除房产
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('park:property:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        parkPropertyService.delete(id);
        return Result.success("删除成功", null);
    }

    /**
     * 批量删除房产
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('park:property:delete')")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        parkPropertyService.batchDelete(ids);
        return Result.success("批量删除成功", null);
    }

    /**
     * 修改房产状态
     */
    @PutMapping("/status")
    @PreAuthorize("hasAuthority('park:property:changeStatus')")
    public Result<Void> changeStatus(@RequestParam Long id,
                                      @RequestParam Integer status,
                                      @RequestParam(required = false) String reason) {
        Long operatorId = null;
        String operatorName = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof LoginUser) {
                LoginUser loginUser = (LoginUser) auth.getPrincipal();
                operatorId = loginUser.getUserId();
                operatorName = loginUser.getUser() != null ? loginUser.getUser().getNickname() : loginUser.getUsername();
            }
        } catch (Exception ignored) {}

        parkPropertyService.changeStatus(id, status, reason, operatorId, operatorName);
        return Result.success("状态修改成功", null);
    }

    /**
     * 批量修改状态
     */
    @PutMapping("/status/batch")
    @PreAuthorize("hasAuthority('park:property:changeStatus')")
    public Result<Void> batchChangeStatus(@RequestParam Integer status,
                                           @RequestParam(required = false) String reason,
                                           @RequestBody List<Long> ids) {
        Long operatorId = null;
        String operatorName = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof LoginUser) {
                LoginUser loginUser = (LoginUser) auth.getPrincipal();
                operatorId = loginUser.getUserId();
                operatorName = loginUser.getUser() != null ? loginUser.getUser().getNickname() : loginUser.getUsername();
            }
        } catch (Exception ignored) {}

        parkPropertyService.batchChangeStatus(ids, status, reason, operatorId, operatorName);
        return Result.success("批量修改状态成功", null);
    }

    /**
     * 获取状态变更日志
     */
    @GetMapping("/statusLog/{propertyId}")
    @PreAuthorize("hasAuthority('park:property:query')")
    public Result<List<ParkPropertyStatusLog>> getStatusLogs(@PathVariable Long propertyId) {
        List<ParkPropertyStatusLog> logs = parkPropertyService.getStatusLogs(propertyId);
        return Result.success(logs);
    }

    /**
     * 预览批量创建
     */
    @PostMapping("/batch/preview")
    @PreAuthorize("hasAuthority('park:property:batchAdd')")
    public Result<List<ParkProperty>> previewBatchCreate(@RequestBody Map<String, Object> params) {
        Long buildingId = Long.valueOf(params.get("buildingId").toString());
        Long floorId = Long.valueOf(params.get("floorId").toString());
        Integer propertyType = Integer.valueOf(params.get("propertyType").toString());
        String propertySubType = params.get("propertySubType") != null ? params.get("propertySubType").toString() : null;
        String prefix = params.get("prefix") != null ? params.get("prefix").toString() : "";
        Integer startNum = Integer.valueOf(params.get("startNum").toString());
        Integer endNum = Integer.valueOf(params.get("endNum").toString());
        BigDecimal buildingArea = params.get("buildingArea") != null ? new BigDecimal(params.get("buildingArea").toString()) : null;
        BigDecimal insideArea = params.get("insideArea") != null ? new BigDecimal(params.get("insideArea").toString()) : null;
        BigDecimal sharedArea = params.get("sharedArea") != null ? new BigDecimal(params.get("sharedArea").toString()) : null;
        String houseType = params.get("houseType") != null ? params.get("houseType").toString() : null;
        String orientation = params.get("orientation") != null ? params.get("orientation").toString() : null;
        String decorationStatus = params.get("decorationStatus") != null ? params.get("decorationStatus").toString() : null;

        List<ParkProperty> list = parkPropertyService.previewBatchCreate(
                buildingId, floorId, propertyType, propertySubType, prefix,
                startNum, endNum, buildingArea, insideArea, sharedArea,
                houseType, orientation, decorationStatus);
        return Result.success(list);
    }

    /**
     * 批量创建房产
     */
    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('park:property:batchAdd')")
    public Result<Map<String, Object>> batchCreate(@RequestBody Map<String, Object> params) {
        Long buildingId = Long.valueOf(params.get("buildingId").toString());
        Long floorId = Long.valueOf(params.get("floorId").toString());
        Integer propertyType = Integer.valueOf(params.get("propertyType").toString());
        String propertySubType = params.get("propertySubType") != null ? params.get("propertySubType").toString() : null;
        String prefix = params.get("prefix") != null ? params.get("prefix").toString() : "";
        Integer startNum = Integer.valueOf(params.get("startNum").toString());
        Integer endNum = Integer.valueOf(params.get("endNum").toString());
        BigDecimal buildingArea = params.get("buildingArea") != null ? new BigDecimal(params.get("buildingArea").toString()) : null;
        BigDecimal insideArea = params.get("insideArea") != null ? new BigDecimal(params.get("insideArea").toString()) : null;
        BigDecimal sharedArea = params.get("sharedArea") != null ? new BigDecimal(params.get("sharedArea").toString()) : null;
        String houseType = params.get("houseType") != null ? params.get("houseType").toString() : null;
        String orientation = params.get("orientation") != null ? params.get("orientation").toString() : null;
        String decorationStatus = params.get("decorationStatus") != null ? params.get("decorationStatus").toString() : null;
        String propertyRightType = params.get("propertyRightType") != null ? params.get("propertyRightType").toString() : null;
        Integer propertyRightYears = params.get("propertyRightYears") != null ? Integer.valueOf(params.get("propertyRightYears").toString()) : null;

        Long operatorId = null;
        String operatorName = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof LoginUser) {
                LoginUser loginUser = (LoginUser) auth.getPrincipal();
                operatorId = loginUser.getUserId();
                operatorName = loginUser.getUser() != null ? loginUser.getUser().getNickname() : loginUser.getUsername();
            }
        } catch (Exception ignored) {}

        Map<String, Object> result = parkPropertyService.batchCreate(
                buildingId, floorId, propertyType, propertySubType, prefix,
                startNum, endNum, buildingArea, insideArea, sharedArea,
                houseType, orientation, decorationStatus, propertyRightType,
                propertyRightYears, operatorId, operatorName);
        return Result.success(result);
    }

    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAuthority('park:property:query')")
    public Result<ParkProperty> getDetailWithExtra(@PathVariable Long id) {
        ParkProperty property = parkPropertyService.getDetailWithExtra(id);
        return Result.success(property);
    }

    /**
     * 导出房产数据
     */
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('park:property:export')")
    public Result<List<ParkProperty>> export(
            @RequestParam(required = false) String propertyCode,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Long floorId,
            @RequestParam(required = false) Integer propertyType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String houseType,
            @RequestParam(required = false) String orientation,
            @RequestParam(required = false) BigDecimal minArea,
            @RequestParam(required = false) BigDecimal maxArea) {
        List<ParkProperty> list = parkPropertyService.listAllForExport(
                propertyCode, buildingId, floorId, propertyType, status,
                houseType, orientation, minArea, maxArea);
        return Result.success(list);
    }

    /**
     * 统计分析
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('park:property:stats')")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = parkPropertyService.getStatistics();
        return Result.success(stats);
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/template")
    @PreAuthorize("hasAuthority('park:property:import')")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        ExcelWriter writer = parkPropertyService.getTemplateWriter();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("房产导入模板.xlsx", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        out.close();
    }

    /**
     * 导入房产数据
     */
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('park:property:import')")
    public Result<Map<String, Object>> importProperties(@RequestParam("file") MultipartFile file) {
        Long operatorId = null;
        String operatorName = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof LoginUser) {
                LoginUser loginUser = (LoginUser) auth.getPrincipal();
                operatorId = loginUser.getUserId();
                operatorName = loginUser.getUser() != null ? loginUser.getUser().getNickname() : loginUser.getUsername();
            }
        } catch (Exception ignored) {}

        Map<String, Object> result = parkPropertyService.importProperties(file, operatorId, operatorName);
        return Result.success(result);
    }
}
