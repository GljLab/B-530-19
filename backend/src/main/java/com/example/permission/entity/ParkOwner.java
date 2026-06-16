
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
@Table("park_owner")
public class ParkOwner {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String ownerCode;

    private Integer ownerType;

    private String name;

    private Integer gender;

    private String idCard;

    private LocalDate birthDate;

    private Integer age;

    private String nation;

    private Integer maritalStatus;

    private String phone;

    private String backupPhone;

    private String email;

    private String wechat;

    private String householdAddress;

    private String currentAddress;

    private String workUnit;

    private String occupation;

    private Integer familyCount;

    private String emergencyContact;

    private String emergencyRelation;

    private String emergencyPhone;

    private Integer occupancyStatus;

    private Integer authStatus;

    private String authRejectReason;

    private String ownerTags;

    private String remark;

    private String enterpriseCreditCode;

    private String enterpriseType;

    private String legalPersonName;

    private String legalPersonIdCard;

    private String contactPerson;

    private String contactPosition;

    private String contactPhone;

    private String enterpriseAddress;

    private BigDecimal registeredCapital;

    private String businessLicenseUrl;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;

    @Column(ignore = true)
    private List<String> ownerTagList;

    @Column(ignore = true)
    private List<ParkOwnerProperty> propertyList;

    @Column(ignore = true)
    private List<ParkOwnerAuditLog> auditLogs;

    @Column(ignore = true)
    private Integer propertyCount;

    @Column(ignore = true)
    private String genderText;

    @Column(ignore = true)
    private String ownerTypeText;

    @Column(ignore = true)
    private String occupancyStatusText;

    @Column(ignore = true)
    private String authStatusText;

    @Column(ignore = true)
    private String maritalStatusText;
}
