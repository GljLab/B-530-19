package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.ParkParkingSpace;
import com.example.permission.entity.ParkParkingSpaceStatusLog;
import com.example.permission.security.LoginUser;
import com.example.permission.service.ParkParkingSpaceService;
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

@RestController
@RequestMapping("/api/park/parking")
public class ParkParkingSpaceController {

    @Autowired
    private ParkParkingSpaceService parkParkingSpaceService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('park:parking:list')")
    public Result<PageResult<ParkParkingSpace>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String spaceCode,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String spaceType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String propertyRight,
            @RequestParam(required = false) BigDecimal minRent,
            @RequestParam(required = false) BigDecimal maxRent,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        PageResult<ParkParkingSpace> result = parkParkingSpaceService.pageList(
                pageNum, pageSize, spaceCode, area, spaceType, status,
                propertyRight, minRent, maxRent, sortField, sortOrder);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('park:parking:query')")
    public Result<ParkParkingSpace> getInfo(@PathVariable Long id) {
        ParkParkingSpace space = parkParkingSpaceService.getById(id);
        return Result.success(space);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('park:parking:add')")
    public Result<Void> add(@RequestBody ParkParkingSpace space) {
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

        parkParkingSpaceService.add(space, operatorId, operatorName);
        return Result.success("新增成功", null);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('park:parking:edit')")
    public Result<Void> update(@RequestBody ParkParkingSpace space) {
        boolean isManager = false;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof LoginUser) {
                LoginUser loginUser = (LoginUser) auth.getPrincipal();
                if (loginUser.getPermissions() != null) {
                    isManager = loginUser.getPermissions().contains("park:parking:delete");
                }
            }
        } catch (Exception ignored) {}

        parkParkingSpaceService.update(space, isManager);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('park:parking:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        parkParkingSpaceService.delete(id);
        return Result.success("删除成功", null);
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('park:parking:delete')")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        parkParkingSpaceService.batchDelete(ids);
        return Result.success("批量删除成功", null);
    }

    @PutMapping("/status")
    @PreAuthorize("hasAuthority('park:parking:changeStatus')")
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

        parkParkingSpaceService.changeStatus(id, status, reason, operatorId, operatorName);
        return Result.success("状态修改成功", null);
    }

    @PutMapping("/status/batch")
    @PreAuthorize("hasAuthority('park:parking:changeStatus')")
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

        parkParkingSpaceService.batchChangeStatus(ids, status, reason, operatorId, operatorName);
        return Result.success("批量修改状态成功", null);
    }

    @GetMapping("/statusLog/{spaceId}")
    @PreAuthorize("hasAuthority('park:parking:query')")
    public Result<List<ParkParkingSpaceStatusLog>> getStatusLogs(@PathVariable Long spaceId) {
        List<ParkParkingSpaceStatusLog> logs = parkParkingSpaceService.getStatusLogs(spaceId);
        return Result.success(logs);
    }

    @PostMapping("/batch/preview")
    @PreAuthorize("hasAuthority('park:parking:batchAdd')")
    public Result<List<ParkParkingSpace>> previewBatchCreate(@RequestBody Map<String, Object> params) {
        String area = params.get("area") != null ? params.get("area").toString() : null;
        String spaceType = params.get("spaceType") != null ? params.get("spaceType").toString() : null;
        String prefix = params.get("prefix") != null ? params.get("prefix").toString() : "";
        Integer startNum = Integer.valueOf(params.get("startNum").toString());
        Integer endNum = Integer.valueOf(params.get("endNum").toString());
        BigDecimal length = params.get("length") != null ? new BigDecimal(params.get("length").toString()) : null;
        BigDecimal width = params.get("width") != null ? new BigDecimal(params.get("width").toString()) : null;
        String propertyRight = params.get("propertyRight") != null ? params.get("propertyRight").toString() : null;
        BigDecimal monthlyRent = params.get("monthlyRent") != null ? new BigDecimal(params.get("monthlyRent").toString()) : null;
        BigDecimal hourlyFee = params.get("hourlyFee") != null ? new BigDecimal(params.get("hourlyFee").toString()) : null;

        List<ParkParkingSpace> list = parkParkingSpaceService.previewBatchCreate(
                area, spaceType, prefix, startNum, endNum, length, width,
                propertyRight, monthlyRent, hourlyFee);
        return Result.success(list);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('park:parking:batchAdd')")
    public Result<Map<String, Object>> batchCreate(@RequestBody Map<String, Object> params) {
        String area = params.get("area") != null ? params.get("area").toString() : null;
        String spaceType = params.get("spaceType") != null ? params.get("spaceType").toString() : null;
        String prefix = params.get("prefix") != null ? params.get("prefix").toString() : "";
        Integer startNum = Integer.valueOf(params.get("startNum").toString());
        Integer endNum = Integer.valueOf(params.get("endNum").toString());
        BigDecimal length = params.get("length") != null ? new BigDecimal(params.get("length").toString()) : null;
        BigDecimal width = params.get("width") != null ? new BigDecimal(params.get("width").toString()) : null;
        String propertyRight = params.get("propertyRight") != null ? params.get("propertyRight").toString() : null;
        BigDecimal monthlyRent = params.get("monthlyRent") != null ? new BigDecimal(params.get("monthlyRent").toString()) : null;
        BigDecimal hourlyFee = params.get("hourlyFee") != null ? new BigDecimal(params.get("hourlyFee").toString()) : null;
        BigDecimal deposit = params.get("deposit") != null ? new BigDecimal(params.get("deposit").toString()) : null;

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

        Map<String, Object> result = parkParkingSpaceService.batchCreate(
                area, spaceType, prefix, startNum, endNum, length, width,
                propertyRight, monthlyRent, hourlyFee, deposit, operatorId, operatorName);
        return Result.success(result);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('park:parking:export')")
    public Result<List<ParkParkingSpace>> export(
            @RequestParam(required = false) String spaceCode,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String spaceType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String propertyRight,
            @RequestParam(required = false) BigDecimal minRent,
            @RequestParam(required = false) BigDecimal maxRent) {
        List<ParkParkingSpace> list = parkParkingSpaceService.listAllForExport(
                spaceCode, area, spaceType, status, propertyRight, minRent, maxRent);
        return Result.success(list);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('park:parking:stats')")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = parkParkingSpaceService.getStatistics();
        return Result.success(stats);
    }

    @GetMapping("/template")
    @PreAuthorize("hasAuthority('park:parking:import')")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        ExcelWriter writer = parkParkingSpaceService.getTemplateWriter();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("车位导入模板.xlsx", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        out.close();
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('park:parking:import')")
    public Result<Map<String, Object>> importSpaces(@RequestParam("file") MultipartFile file) {
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

        Map<String, Object> result = parkParkingSpaceService.importSpaces(file, operatorId, operatorName);
        return Result.success(result);
    }
}
