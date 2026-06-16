package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.ParkPropertyTransfer;
import com.example.permission.security.LoginUser;
import com.example.permission.service.ParkPropertyTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/park/property/transfer")
public class ParkPropertyTransferController {

    @Autowired
    private ParkPropertyTransferService parkPropertyTransferService;

    @PostMapping("/apply")
    @PreAuthorize("hasAuthority('park:transfer:apply')")
    public Result<ParkPropertyTransfer> applyTransfer(@RequestBody Map<String, Object> params) {
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

        Long propertyId = Long.valueOf(params.get("propertyId").toString());
        Long newOwnerId = Long.valueOf(params.get("newOwnerId").toString());
        Integer transferType = Integer.valueOf(params.get("transferType").toString());
        BigDecimal transferPrice = params.get("transferPrice") != null ? new BigDecimal(params.get("transferPrice").toString()) : null;
        LocalDate transferDate = params.get("transferDate") != null ? LocalDate.parse(params.get("transferDate").toString()) : null;
        String newPropertyCertNo = params.get("newPropertyCertNo") != null ? params.get("newPropertyCertNo").toString() : null;
        String transferAgreementUrl = params.get("transferAgreementUrl") != null ? params.get("transferAgreementUrl").toString() : null;
        String newCertUrl = params.get("newCertUrl") != null ? params.get("newCertUrl").toString() : null;

        ParkPropertyTransfer result = parkPropertyTransferService.applyTransfer(
                propertyId, newOwnerId, transferType, transferPrice, transferDate,
                newPropertyCertNo, transferAgreementUrl, newCertUrl, operatorId, operatorName);
        return Result.success(result);
    }

    @PostMapping("/audit")
    @PreAuthorize("hasAuthority('park:transfer:auditPass')")
    public Result<Void> auditTransfer(@RequestBody Map<String, Object> params) {
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

        Long transferId = Long.valueOf(params.get("transferId").toString());
        Integer auditResult = Integer.valueOf(params.get("auditResult").toString());
        String auditOpinion = params.get("auditOpinion") != null ? params.get("auditOpinion").toString() : null;

        parkPropertyTransferService.auditTransfer(transferId, auditResult, auditOpinion, operatorId, operatorName);
        return Result.success(null);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('park:transfer:query')")
    public Result<PageResult<ParkPropertyTransfer>> transferList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) Long propertyId) {
        PageResult<ParkPropertyTransfer> result = parkPropertyTransferService.getTransferList(pageNum, pageSize, auditStatus, propertyId);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('park:transfer:query')")
    public Result<ParkPropertyTransfer> transferDetail(@PathVariable Long id) {
        ParkPropertyTransfer result = parkPropertyTransferService.getTransferById(id);
        return Result.success(result);
    }

    @GetMapping("/history/{propertyId}")
    @PreAuthorize("hasAuthority('park:property:query')")
    public Result<List<ParkPropertyTransfer>> transferHistory(@PathVariable Long propertyId) {
        List<ParkPropertyTransfer> result = parkPropertyTransferService.getTransferHistory(propertyId);
        return Result.success(result);
    }
}
