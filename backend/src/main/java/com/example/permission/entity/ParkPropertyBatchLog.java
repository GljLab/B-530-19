package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Table("park_property_batch_log")
public class ParkPropertyBatchLog {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String batchNo;

    private Integer batchType;

    private Integer targetStatus;

    private String targetDecorationStatus;

    private String targetSpecialTags;

    private String operateReason;

    private Integer totalCount;

    private Integer successCount;

    private Integer failCount;

    private Integer skipCount;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime createTime;

    @Column(ignore = true)
    private List<ParkPropertyBatchItem> items;
}
