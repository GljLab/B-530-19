package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.ParkPropertyBatchItem;
import com.example.permission.entity.ParkPropertyBatchLog;
import com.example.permission.security.LoginUser;
import com.example.permission.service.ParkPropertyBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/park/property/batch")
public class ParkPropertyBatchController {

    @Autowired
    private ParkPropertyBatchService parkPropertyBatchService;

    @PutMapping("/status")
    @PreAuthorize("hasAuthority('park:property:batchStatus')")
    public Result<Map<String, Object>> batchChangeStatus(@RequestBody Map<String, Object> params) {
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

        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) params.get("ids");
        Integer targetStatus = Integer.valueOf(params.get("targetStatus").toString());
        String operateReason = params.get("operateReason") != null ? params.get("operateReason").toString() : null;

        Map<String, Object> result = parkPropertyBatchService.batchChangeStatus(ids, targetStatus, operateReason, operatorId, operatorName);
        return Result.success(result);
    }

    @PutMapping("/attribute")
    @PreAuthorize("hasAuthority('park:property:batchAttr')")
    public Result<Map<String, Object>> batchChangeAttribute(@RequestBody Map<String, Object> params) {
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

        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) params.get("ids");
        String targetDecorationStatus = params.get("targetDecorationStatus") != null ? params.get("targetDecorationStatus").toString() : null;
        String targetSpecialTags = params.get("targetSpecialTags") != null ? params.get("targetSpecialTags").toString() : null;
        String operateReason = params.get("operateReason") != null ? params.get("operateReason").toString() : null;

        Map<String, Object> result = parkPropertyBatchService.batchChangeAttribute(ids, targetDecorationStatus, targetSpecialTags, operateReason, operatorId, operatorName);
        return Result.success(result);
    }

    @GetMapping("/log")
    @PreAuthorize("hasAuthority('park:property:batchLog')")
    public Result<PageResult<ParkPropertyBatchLog>> batchLogList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String batchNo) {
        PageResult<ParkPropertyBatchLog> result = parkPropertyBatchService.getBatchLogList(pageNum, pageSize, batchNo);
        return Result.success(result);
    }

    @GetMapping("/log/{id}")
    @PreAuthorize("hasAuthority('park:property:batchLog')")
    public Result<ParkPropertyBatchLog> batchDetail(@PathVariable Long id) {
        ParkPropertyBatchLog result = parkPropertyBatchService.getBatchDetail(id);
        return Result.success(result);
    }

    @GetMapping("/log/items/{batchNo}")
    @PreAuthorize("hasAuthority('park:property:batchLog')")
    public Result<List<ParkPropertyBatchItem>> batchItemsByBatchNo(@PathVariable String batchNo) {
        List<ParkPropertyBatchItem> result = parkPropertyBatchService.getBatchItemsByBatchNo(batchNo);
        return Result.success(result);
    }
}
