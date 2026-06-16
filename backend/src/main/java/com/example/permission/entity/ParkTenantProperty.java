
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
@Table("park_tenant_property")
public class ParkTenantProperty {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long tenantId;

    private String tenantCode;

    private Long propertyId;

    private String propertyCode;

    private Long contractId;

    private String contractCode;

    private Integer relationStatus;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(ignore = true)
    private String buildingName;

    @Column(ignore = true)
    private String floorNumber;

    @Column(ignore = true)
    private BigDecimal buildingArea;

    @Column(ignore = true)
    private String houseType;

    @Column(ignore = true)
    private String relationStatusText;
}
