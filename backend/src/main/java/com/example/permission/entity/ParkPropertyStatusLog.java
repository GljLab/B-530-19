package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 房产状态变更日志实体
 */
@Data
@Table("park_property_status_log")
public class ParkPropertyStatusLog {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 房产ID
     */
    private Long propertyId;

    /**
     * 房产编号（冗余）
     */
    private String propertyCode;

    /**
     * 原状态
     */
    private Integer oldStatus;

    /**
     * 新状态
     */
    private Integer newStatus;

    /**
     * 变更原因
     */
    private String changeReason;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
