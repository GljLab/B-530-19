
package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Table("park_lease_contract")
public class ParkLeaseContract {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String contractCode;

    private Long tenantId;

    private String tenantCode;

    private String tenantName;

    private Long propertyId;

    private String propertyCode;

    private Long ownerId;

    private String ownerCode;

    private String ownerName;

    private Long trusteeId;

    private String trusteeName;

    private Integer leaseType;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer leaseMonths;

    private BigDecimal monthlyRent;

    private BigDecimal depositAmount;

    private Integer paymentMethod;

    private Integer includePropertyFee;

    private BigDecimal propertyFeeMonthly;

    private Integer waterFeeBearer;

    private Integer electricityFeeBearer;

    private Integer gasFeeBearer;

    private BigDecimal earlyTerminationPenalty;

    private BigDecimal latePaymentPenaltyRate;

    private String contractTerms;

    private String contractFileUrl;

    private String idCardCopyUrl;

    private String otherAttachments;

    private Integer contractStatus;

    private String auditRejectReason;

    private LocalDateTime auditTime;

    private Long auditorId;

    private String auditorName;

    private String terminationReason;

    private LocalDate terminationDate;

    private BigDecimal terminationPenalty;

    private Integer terminationAuditStatus;

    private LocalDateTime terminationAuditTime;

    private Long terminationAuditorId;

    private String terminationAuditorName;

    private Long originalContractId;

    private Integer renewalReminderSent;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;

    @Column(ignore = true)
    private ParkTenant tenant;

    @Column(ignore = true)
    private ParkProperty property;

    @Column(ignore = true)
    private ParkOwner owner;

    @Column(ignore = true)
    private ParkBuilding building;

    @Column(ignore = true)
    private ParkFloor floor;

    @Column(ignore = true)
    private List<ParkLeaseContractLog> operationLogs;

    @Column(ignore = true)
    private List<String> otherAttachmentList;

    @Column(ignore = true)
    private String leaseTypeText;

    @Column(ignore = true)
    private String contractStatusText;

    @Column(ignore = true)
    private String paymentMethodText;

    @Column(ignore = true)
    private String waterFeeBearerText;

    @Column(ignore = true)
    private String electricityFeeBearerText;

    @Column(ignore = true)
    private String gasFeeBearerText;

    @Column(ignore = true)
    private String terminationAuditStatusText;
}
