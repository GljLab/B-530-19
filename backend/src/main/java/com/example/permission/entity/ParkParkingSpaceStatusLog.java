package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("park_parking_space_status_log")
public class ParkParkingSpaceStatusLog {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long spaceId;

    private String spaceCode;

    private Integer oldStatus;

    private Integer newStatus;

    private String changeReason;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime createTime;
}
