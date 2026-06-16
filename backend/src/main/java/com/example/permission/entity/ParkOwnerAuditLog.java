
package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("park_owner_audit_log")
public class ParkOwnerAuditLog {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long ownerId;

    private String ownerCode;

    private String ownerName;

    private Integer auditResult;

    private String auditOpinion;

    private String rejectReason;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime createTime;

    @Column(ignore = true)
    private String auditResultText;
}
