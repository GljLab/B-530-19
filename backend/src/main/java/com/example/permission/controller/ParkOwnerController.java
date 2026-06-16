
package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.ParkOwner;
import com.example.permission.entity.ParkOwnerProperty;
import com.example.permission.entity.ParkOwnerAuditLog;
import com.example.permission.security.LoginUser;
import com.example.permission.service.ParkOwnerService;
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
@RequestMapping("/api/park/owner")
public class ParkOwnerController {

    @Autowired
    private ParkOwnerService parkOwnerService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('park:owner:list')")
    public Result<PageResult<ParkOwner>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String ownerName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String idCard,
            @RequestParam(required = false) Integer ownerType,
            @RequestParam(required = false) Integer occupancyStatus,
            @RequestParam(required = false) Integer authStatus,
            @RequestParam(required = false) String ownerTags,
            @RequestParam(required = false) Integer minPropertyCount,
            @RequestParam(required = false) Integer maxPropertyCount,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate moveInStartDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate moveInEndDate,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        String searchKeyword = keyword;
        if (searchKeyword == null || searchKeyword.isEmpty()) {
            if (ownerName != null && !ownerName.isEmpty()) {
                searchKeyword = ownerName;
            } else if (phone != null && !phone.isEmpty()) {
                searchKeyword = phone;
            } else if (idCard != null && !idCard.isEmpty()) {
                searchKeyword = idCard;
            }
        }
        PageResult<ParkOwner> result = parkOwnerService.pageList(
                pageNum, pageSize, searchKeyword, ownerType, occupancyStatus, authStatus,
                ownerTags, minPropertyCount, maxPropertyCount, moveInStartDate, moveInEndDate,
                sortField, sortOrder);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('park:owner:query')")
    public Result<ParkOwner> getInfo(@PathVariable Long id) {
        ParkOwner owner = parkOwnerService.getById(id);
        return Result.success(owner);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('park:owner:add')")
    public Result<Void> add(@RequestBody Map<String, Object> params) {
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

        Map<String, Object> ownerMap = (Map<String, Object>) params.get("owner");
        if (ownerMap == null) {
            ownerMap = params;
        }
        List<Map<String, Object>> propertyListMap = (List<Map<String, Object>>) params.get("propertyList");

        ParkOwner owner = convertMapToOwner(ownerMap);
        List<ParkOwnerProperty> propertyList = null;
        if (propertyListMap != null) {
            propertyList = propertyListMap.stream()
                    .map(this::convertMapToOwnerProperty)
                    .collect(java.util.stream.Collectors.toList());
        }

        parkOwnerService.add(owner, propertyList, operatorId, operatorName);
        return Result.success("新增成功", null);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('park:owner:edit')")
    public Result<Void> update(@RequestBody ParkOwner owner) {
        boolean canEditContactOnly = false;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof LoginUser) {
                LoginUser loginUser = (LoginUser) auth.getPrincipal();
                if (loginUser.getPermissions() != null
                        && !loginUser.getPermissions().contains("park:owner:edit")
                        && loginUser.getPermissions().contains("park:owner:query")) {
                    canEditContactOnly = true;
                }
            }
        } catch (Exception ignored) {}

        parkOwnerService.update(owner, canEditContactOnly);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('park:owner:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        parkOwnerService.delete(id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/audit")
    @PreAuthorize("hasAuthority('park:owner:audit')")
    public Result<Void> audit(@RequestBody Map<String, Object> params) {
        Long ownerId = Long.valueOf(params.get("ownerId").toString());
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

        parkOwnerService.audit(ownerId, auditResult, auditOpinion, rejectReason, operatorId, operatorName);
        return Result.success("审核完成", null);
    }

    @PostMapping("/bindProperty")
    @PreAuthorize("hasAuthority('park:owner:bindProperty')")
    public Result<Void> bindProperty(@RequestBody Map<String, Object> params) {
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

        ParkOwnerProperty propertyRelation = convertMapToOwnerProperty(params);
        if (propertyRelation.getOwnerId() == null) {
            return Result.error("业主ID不能为空");
        }
        if (propertyRelation.getPropertyId() == null) {
            return Result.error("房产ID不能为空");
        }

        ParkOwner owner = parkOwnerService.getById(propertyRelation.getOwnerId());
        if (owner == null) {
            return Result.error("业主不存在");
        }

        parkOwnerService.bindProperty(propertyRelation.getOwnerId(), owner.getOwnerCode(),
                propertyRelation, operatorId, operatorName);
        return Result.success("关联成功", null);
    }

    @PostMapping("/unbindProperty")
    @PreAuthorize("hasAuthority('park:owner:unbindProperty')")
    public Result<Void> unbindProperty(@RequestBody Map<String, Object> params) {
        Long relationId = null;
        if (params.get("relationId") != null) {
            relationId = Long.valueOf(params.get("relationId").toString());
        } else if (params.get("propertyId") != null) {
            relationId = Long.valueOf(params.get("propertyId").toString());
        } else if (params.get("id") != null) {
            relationId = Long.valueOf(params.get("id").toString());
        }
        if (relationId == null) {
            return Result.error("关联ID不能为空");
        }
        String unbindReason = params.get("unbindReason") != null ? params.get("unbindReason").toString() : null;

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

        parkOwnerService.unbindProperty(relationId, unbindReason, operatorId, operatorName);
        return Result.success("解除关联成功", null);
    }

    @GetMapping("/properties/{ownerId}")
    @PreAuthorize("hasAuthority('park:owner:query')")
    public Result<List<ParkOwnerProperty>> getOwnerProperties(@PathVariable Long ownerId) {
        List<ParkOwnerProperty> list = parkOwnerService.getOwnerProperties(ownerId);
        return Result.success(list);
    }

    @GetMapping("/auditLogs/{ownerId}")
    @PreAuthorize("hasAuthority('park:owner:query')")
    public Result<List<ParkOwnerAuditLog>> getAuditLogs(@PathVariable Long ownerId) {
        List<ParkOwnerAuditLog> list = parkOwnerService.getAuditLogs(ownerId);
        return Result.success(list);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('park:owner:stats')")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = parkOwnerService.getStatistics();
        return Result.success(stats);
    }

    @GetMapping("/template")
    @PreAuthorize("hasAuthority('park:owner:import')")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        ExcelWriter writer = parkOwnerService.getTemplateWriter();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("业主导入模板.xlsx", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        out.close();
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('park:owner:import')")
    public Result<Map<String, Object>> importOwners(@RequestParam("file") MultipartFile file) {
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

        Map<String, Object> result = parkOwnerService.importOwners(file, operatorId, operatorName);
        return Result.success(result);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('park:owner:export')")
    public Result<List<ParkOwner>> export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String ownerName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String idCard,
            @RequestParam(required = false) Integer ownerType,
            @RequestParam(required = false) Integer occupancyStatus,
            @RequestParam(required = false) Integer authStatus,
            @RequestParam(required = false) String ownerTags) {
        String searchKeyword = keyword;
        if (searchKeyword == null || searchKeyword.isEmpty()) {
            if (ownerName != null && !ownerName.isEmpty()) {
                searchKeyword = ownerName;
            } else if (phone != null && !phone.isEmpty()) {
                searchKeyword = phone;
            } else if (idCard != null && !idCard.isEmpty()) {
                searchKeyword = idCard;
            }
        }
        List<ParkOwner> list = parkOwnerService.listAllForExport(
                searchKeyword, ownerType, occupancyStatus, authStatus, ownerTags);
        return Result.success(list);
    }

    private ParkOwner convertMapToOwner(Map<String, Object> map) {
        ParkOwner owner = new ParkOwner();
        if (map.get("ownerType") != null) owner.setOwnerType(Integer.valueOf(map.get("ownerType").toString()));
        if (map.get("name") != null) owner.setName(map.get("name").toString());
        else if (map.get("ownerName") != null) owner.setName(map.get("ownerName").toString());
        if (map.get("gender") != null) owner.setGender(Integer.valueOf(map.get("gender").toString()));
        if (map.get("idCard") != null) owner.setIdCard(map.get("idCard").toString());
        if (map.get("birthDate") != null) owner.setBirthDate(LocalDate.parse(map.get("birthDate").toString()));
        if (map.get("nation") != null) owner.setNation(map.get("nation").toString());
        if (map.get("maritalStatus") != null) owner.setMaritalStatus(Integer.valueOf(map.get("maritalStatus").toString()));
        if (map.get("phone") != null) owner.setPhone(map.get("phone").toString());
        if (map.get("backupPhone") != null) owner.setBackupPhone(map.get("backupPhone").toString());
        if (map.get("email") != null) owner.setEmail(map.get("email").toString());
        if (map.get("wechat") != null) owner.setWechat(map.get("wechat").toString());
        if (map.get("householdAddress") != null) owner.setHouseholdAddress(map.get("householdAddress").toString());
        if (map.get("currentAddress") != null) owner.setCurrentAddress(map.get("currentAddress").toString());
        if (map.get("workUnit") != null) owner.setWorkUnit(map.get("workUnit").toString());
        if (map.get("occupation") != null) owner.setOccupation(map.get("occupation").toString());
        if (map.get("familyCount") != null) owner.setFamilyCount(Integer.valueOf(map.get("familyCount").toString()));
        if (map.get("emergencyContact") != null) owner.setEmergencyContact(map.get("emergencyContact").toString());
        if (map.get("emergencyRelation") != null) owner.setEmergencyRelation(map.get("emergencyRelation").toString());
        if (map.get("emergencyPhone") != null) owner.setEmergencyPhone(map.get("emergencyPhone").toString());
        if (map.get("occupancyStatus") != null) owner.setOccupancyStatus(Integer.valueOf(map.get("occupancyStatus").toString()));
        if (map.get("ownerTags") != null) owner.setOwnerTags(map.get("ownerTags").toString());
        if (map.get("remark") != null) owner.setRemark(map.get("remark").toString());
        if (map.get("enterpriseCreditCode") != null) owner.setEnterpriseCreditCode(map.get("enterpriseCreditCode").toString());
        if (map.get("enterpriseType") != null) owner.setEnterpriseType(map.get("enterpriseType").toString());
        if (map.get("legalPersonName") != null) owner.setLegalPersonName(map.get("legalPersonName").toString());
        if (map.get("legalPersonIdCard") != null) owner.setLegalPersonIdCard(map.get("legalPersonIdCard").toString());
        if (map.get("contactPerson") != null) owner.setContactPerson(map.get("contactPerson").toString());
        if (map.get("contactPosition") != null) owner.setContactPosition(map.get("contactPosition").toString());
        if (map.get("contactPhone") != null) owner.setContactPhone(map.get("contactPhone").toString());
        if (map.get("enterpriseAddress") != null) owner.setEnterpriseAddress(map.get("enterpriseAddress").toString());
        if (map.get("registeredCapital") != null) owner.setRegisteredCapital(new java.math.BigDecimal(map.get("registeredCapital").toString()));
        if (map.get("businessLicenseUrl") != null) owner.setBusinessLicenseUrl(map.get("businessLicenseUrl").toString());
        return owner;
    }

    private ParkOwnerProperty convertMapToOwnerProperty(Map<String, Object> map) {
        ParkOwnerProperty op = new ParkOwnerProperty();
        if (map.get("ownerId") != null) op.setOwnerId(Long.valueOf(map.get("ownerId").toString()));
        if (map.get("propertyId") != null) op.setPropertyId(Long.valueOf(map.get("propertyId").toString()));
        if (map.get("propertyRightType") != null) op.setPropertyRightType(Integer.valueOf(map.get("propertyRightType").toString()));
        else if (map.get("ownershipType") != null) op.setPropertyRightType(Integer.valueOf(map.get("ownershipType").toString()));
        if (map.get("propertyRightRatio") != null) op.setPropertyRightRatio(new java.math.BigDecimal(map.get("propertyRightRatio").toString()));
        else if (map.get("ownershipRatio") != null) op.setPropertyRightRatio(new java.math.BigDecimal(map.get("ownershipRatio").toString()));
        if (map.get("propertyCertNo") != null) op.setPropertyCertNo(map.get("propertyCertNo").toString());
        if (map.get("acquireType") != null) op.setAcquireType(Integer.valueOf(map.get("acquireType").toString()));
        if (map.get("acquireDate") != null) op.setAcquireDate(LocalDate.parse(map.get("acquireDate").toString()));
        if (map.get("purchasePrice") != null) op.setPurchasePrice(new java.math.BigDecimal(map.get("purchasePrice").toString()));
        if (map.get("moveInDate") != null) op.setMoveInDate(LocalDate.parse(map.get("moveInDate").toString()));
        if (map.get("isSelfOccupy") != null) op.setIsSelfOccupy(Integer.valueOf(map.get("isSelfOccupy").toString()));
        return op;
    }
}
