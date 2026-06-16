package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("park_property_transfer")
public class ParkPropertyTransfer {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String transferNo;

    private Long propertyId;

    private String propertyCode;

    private Long oldOwnerId;

    private String oldOwnerName;

    private Long newOwnerId;

    private String newOwnerName;

    private Integer transferType;

    private BigDecimal transferPrice;

    private LocalDate transferDate;

    private String newPropertyCertNo;

    private String transferAgreementUrl;

    private String newCertUrl;

    private Integer auditStatus;

    private String auditOpinion;

    private Long auditorId;

    private String auditorName;

    private LocalDateTime auditTime;

    private Long applyOperatorId;

    private String applyOperatorName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(ignore = true)
    private String buildingName;

    @Column(ignore = true)
    private String floorNumber;

    @Column(ignore = true)
    private String transferTypeText;

    @Column(ignore = true)
    private String auditStatusText;
}
