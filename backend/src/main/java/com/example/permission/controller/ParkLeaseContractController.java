
package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.ParkLeaseContract;
import com.example.permission.security.LoginUser;
import com.example.permission.service.ParkLeaseContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/park/lease-contract")
public class ParkLeaseContractController {

    @Autowired
    private ParkLeaseContractService parkLeaseContractService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('park:contract:list')")
    public Result<PageResult<ParkLeaseContract>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer contractStatus,
            @RequestParam(required = false) Integer leaseType,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long propertyId,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDateStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDateEnd,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDateStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDateEnd,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        PageResult<ParkLeaseContract> result = parkLeaseContractService.pageList(
                pageNum, pageSize, keyword, contractStatus, leaseType, tenantId, propertyId, ownerId,
                startDateStart, startDateEnd, endDateStart, endDateEnd, sortField, sortOrder);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('park:contract:query')")
    public Result<ParkLeaseContract> getInfo(@PathVariable Long id) {
        ParkLeaseContract contract = parkLeaseContractService.getById(id);
        return Result.success(contract);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('park:contract:add')")
    public Result<Void> add(@RequestBody ParkLeaseContract contract) {
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

        parkLeaseContractService.add(contract, operatorId, operatorName);
        return Result.success("新增成功", null);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('park:contract:edit')")
    public Result<Void> update(@RequestBody ParkLeaseContract contract) {
        parkLeaseContractService.update(contract);
        return Result.success("修改成功", null);
    }

    @PostMapping("/audit")
    @PreAuthorize("hasAuthority('park:contract:audit')")
    public Result<Void> audit(@RequestBody Map<String, Object> params) {
        Long contractId = Long.valueOf(params.get("contractId").toString());
        Integer auditResult = Integer.valueOf(params.get("auditResult").toString());
        String auditOpinion = params.get("auditOpinion") != null ? params.get("auditOpinion").toString() : null;
        String rejectReason = params.get("rejectReason") != null ? params.get("rejectReason").toString() : null;

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

        parkLeaseContractService.audit(contractId, auditResult, auditOpinion, rejectReason, operatorId, operatorName);
        return Result.success("审核完成", null);
    }

    @PostMapping("/renew")
    @PreAuthorize("hasAuthority('park:contract:renew')")
    public Result<ParkLeaseContract> renew(@RequestBody Map<String, Object> params) {
        Long contractId = Long.valueOf(params.get("contractId").toString());
        LocalDate newStartDate = params.get("newStartDate") != null ?
                LocalDate.parse(params.get("newStartDate").toString()) : null;
        LocalDate newEndDate = params.get("newEndDate") != null ?
                LocalDate.parse(params.get("newEndDate").toString()) : null;
        BigDecimal newMonthlyRent = params.get("newMonthlyRent") != null ?
                new BigDecimal(params.get("newMonthlyRent").toString()) : null;
        BigDecimal newDepositAmount = params.get("newDepositAmount") != null ?
                new BigDecimal(params.get("newDepositAmount").toString()) : null;

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

        ParkLeaseContract newContract = parkLeaseContractService.renew(
                contractId, newStartDate, newEndDate, newMonthlyRent, newDepositAmount, operatorId, operatorName);
        return Result.success("续签成功", newContract);
    }

    @PostMapping("/terminate")
    @PreAuthorize("hasAuthority('park:contract:terminate')")
    public Result<Void> requestTermination(@RequestBody Map<String, Object> params) {
        Long contractId = Long.valueOf(params.get("contractId").toString());
        String terminationReason = params.get("terminationReason") != null ?
                params.get("terminationReason").toString() : null;
        LocalDate terminationDate = params.get("terminationDate") != null ?
                LocalDate.parse(params.get("terminationDate").toString()) : null;
        BigDecimal terminationPenalty = params.get("terminationPenalty") != null ?
                new BigDecimal(params.get("terminationPenalty").toString()) : null;

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

        parkLeaseContractService.requestTermination(
                contractId, terminationReason, terminationDate, terminationPenalty, operatorId, operatorName);
        return Result.success("解除申请已提交", null);
    }

    @PostMapping("/audit-termination")
    @PreAuthorize("hasAuthority('park:leaseContract:audit')")
    public Result<Void> auditTermination(@RequestBody Map<String, Object> params) {
        Long contractId = Long.valueOf(params.get("contractId").toString());
        Integer auditResult = Integer.valueOf(params.get("auditResult").toString());
        String auditOpinion = params.get("auditOpinion") != null ? params.get("auditOpinion").toString() : null;

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

        parkLeaseContractService.auditTermination(contractId, auditResult, auditOpinion, operatorId, operatorName);
        return Result.success("审核完成", null);
    }

    @PostMapping("/check-expired")
    @PreAuthorize("hasAuthority('park:contract:edit')")
    public Result<Void> checkAndUpdateExpiredContracts() {
        parkLeaseContractService.checkAndUpdateExpiredContracts();
        return Result.success("已更新到期合约状态", null);
    }
}
