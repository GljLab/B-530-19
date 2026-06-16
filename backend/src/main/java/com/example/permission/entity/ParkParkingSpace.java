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
@Table("park_parking_space")
public class ParkParkingSpace {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String spaceCode;

    private String area;

    private String spaceType;

    private String locationDesc;

    private String orientation;

    private BigDecimal length;

    private BigDecimal width;

    private String propertyRight;

    private Long ownerId;

    private String propertyCertNo;

    private BigDecimal purchasePrice;

    private LocalDate purchaseDate;

    private Integer status;

    private BigDecimal monthlyRent;

    private BigDecimal hourlyFee;

    private BigDecimal deposit;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;

    @Column(ignore = true)
    private String ownerName;

    @Column(ignore = true)
    private List<ParkParkingSpaceStatusLog> statusLogs;
}
