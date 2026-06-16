
package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Table("park_tenant")
public class ParkTenant {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String tenantCode;

    private Integer tenantType;

    private String name;

    private Integer gender;

    private String idCard;

    private LocalDate birthDate;

    private String phone;

    private String backupPhone;

    private String email;

    private String workUnit;

    private String occupation;

    private String incomeRange;

    private String emergencyContact;

    private String emergencyRelation;

    private String emergencyPhone;

    private Integer tenantStatus;

    private String remark;

    private String enterpriseName;

    private String enterpriseCreditCode;

    private String legalPersonName;

    private String contactPerson;

    private String contactPosition;

    private String contactPhone;

    private String enterpriseAddress;

    private String businessLicenseUrl;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;

    @Column(ignore = true)
    private List<ParkLeaseContract> contractList;

    @Column(ignore = true)
    private ParkLeaseContract currentContract;

    @Column(ignore = true)
    private List<ParkTenantProperty> propertyList;

    @Column(ignore = true)
    private String tenantTypeText;

    @Column(ignore = true)
    private String tenantStatusText;

    @Column(ignore = true)
    private String genderText;

    @Column(ignore = true)
    private String currentPropertyCode;

    @Column(ignore = true)
    private String currentBuildingName;

    @Column(ignore = true)
    private String currentLeasePeriod;
}
