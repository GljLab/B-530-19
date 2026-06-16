
package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.ParkTenant;
import com.example.permission.security.LoginUser;
import com.example.permission.service.ParkTenantService;
import cn.hutool.poi.excel.ExcelWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/park/tenant")
public class ParkTenantController {

    @Autowired
    private ParkTenantService parkTenantService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('park:tenant:list')")
    public Result<PageResult<ParkTenant>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer tenantType,
            @RequestParam(required = false) Integer tenantStatus,
            @RequestParam(required = false) Long propertyId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate moveInStartDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate moveInEndDate,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        PageResult<ParkTenant> result = parkTenantService.pageList(
                pageNum, pageSize, keyword, tenantType, tenantStatus,
                propertyId, moveInStartDate, moveInEndDate, sortField, sortOrder);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('park:tenant:query')")
    public Result<ParkTenant> getInfo(@PathVariable Long id) {
        ParkTenant tenant = parkTenantService.getById(id);
        return Result.success(tenant);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('park:tenant:add')")
    public Result<Void> add(@RequestBody ParkTenant tenant) {
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

        parkTenantService.add(tenant, operatorId, operatorName);
        return Result.success("新增成功", null);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('park:tenant:edit')")
    public Result<Void> update(@RequestBody ParkTenant tenant) {
        parkTenantService.update(tenant);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('park:tenant:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        parkTenantService.delete(id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/blacklist")
    @PreAuthorize("hasAuthority('park:tenant:edit')")
    public Result<Void> setBlacklist(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        boolean isBlacklist = Boolean.TRUE.equals(params.get("isBlacklist"));
        String reason = params.get("reason") != null ? params.get("reason").toString() : null;
        parkTenantService.setBlacklist(id, isBlacklist, reason);
        return Result.success(isBlacklist ? "已加入黑名单" : "已移出黑名单", null);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('park:tenant:stats')")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = parkTenantService.getStatistics();
        return Result.success(stats);
    }

    @GetMapping("/template")
    @PreAuthorize("hasAuthority('park:tenant:import')")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        ExcelWriter writer = parkTenantService.getTemplateWriter();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("租户导入模板.xlsx", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        out.close();
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('park:tenant:import')")
    public Result<Map<String, Object>> importTenants(@RequestParam("file") MultipartFile file) {
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

        Map<String, Object> result = parkTenantService.importTenants(file, operatorId, operatorName);
        return Result.success(result);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('park:tenant:export')")
    public Result<List<ParkTenant>> export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer tenantType,
            @RequestParam(required = false) Integer tenantStatus) {
        List<ParkTenant> list = parkTenantService.listAllForExport(keyword, tenantType, tenantStatus);
        return Result.success(list);
    }
}
