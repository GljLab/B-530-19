package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.ParkProperty;
import com.example.permission.security.LoginUser;
import com.example.permission.service.ParkPropertyVacancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/park/property/vacancy")
public class ParkPropertyVacancyController {

    @Autowired
    private ParkPropertyVacancyService parkPropertyVacancyService;

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('park:property:vacancyStats')")
    public Result<Map<String, Object>> vacancyStats() {
        Map<String, Object> result = parkPropertyVacancyService.getVacancyStats();
        return Result.success(result);
    }

    @GetMapping("/longTerm")
    @PreAuthorize("hasAuthority('park:property:vacancyStats')")
    public Result<PageResult<ParkProperty>> longTermVacantList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ParkProperty> result = parkPropertyVacancyService.getLongTermVacantList(pageNum, pageSize);
        return Result.success(result);
    }

    @PostMapping("/reason")
    @PreAuthorize("hasAuthority('park:property:setVacancy')")
    public Result<Void> recordVacancyReason(@RequestBody Map<String, Object> params) {
        Long propertyId = Long.valueOf(params.get("propertyId").toString());
        String vacancyReason = params.get("vacancyReason") != null ? params.get("vacancyReason").toString() : null;
        String vacancyReasonRemark = params.get("vacancyReasonRemark") != null ? params.get("vacancyReasonRemark").toString() : null;
        parkPropertyVacancyService.recordVacancyReason(propertyId, vacancyReason, vacancyReasonRemark);
        return Result.success(null);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('park:property:vacancyExport')")
    public Result<List<ParkProperty>> exportLongTermVacant() {
        List<ParkProperty> result = parkPropertyVacancyService.exportLongTermVacantList();
        return Result.success(result);
    }
}
