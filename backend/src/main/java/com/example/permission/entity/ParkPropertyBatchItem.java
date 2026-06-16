package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("park_property_batch_item")
public class ParkPropertyBatchItem {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long batchId;

    private String batchNo;

    private Long propertyId;

    private String propertyCode;

    private Integer oldStatus;

    private Integer newStatus;

    private String oldDecorationStatus;

    private String newDecorationStatus;

    private String oldSpecialTags;

    private String newSpecialTags;

    private Integer result;

    private String failReason;

    private LocalDateTime createTime;

    @Column(ignore = true)
    private String buildingName;

    @Column(ignore = true)
    private String floorNumber;

    @Column(ignore = true)
    private Integer currentStatus;
}
