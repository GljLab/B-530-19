
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
@Table("park_owner_property")
public class ParkOwnerProperty {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long ownerId;

    private String ownerCode;

    private Long propertyId;

    private String propertyCode;

    private Integer propertyRightType;

    private BigDecimal propertyRightRatio;

    private String propertyCertNo;

    private Integer acquireType;

    private LocalDate acquireDate;

    private BigDecimal purchasePrice;

    private LocalDate moveInDate;

    private Integer isSelfOccupy;

    private Integer relationStatus;

    private LocalDateTime unbindTime;

    private Long unbindOperatorId;

    private String unbindOperatorName;

    private String unbindReason;

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
    private String propertyRightTypeText;

    @Column(ignore = true)
    private String acquireTypeText;

    @Column(ignore = true)
    private String relationStatusText;

    @Column(ignore = true)
    private String isSelfOccupyText;
}
