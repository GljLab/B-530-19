package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 配套设施实体
 */
@Data
@Table("park_facility")
public class ParkFacility {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 园区ID
     */
    private Long parkId;

    /**
     * 设施名称
     */
    private String facilityName;

    /**
     * 设施类型
     */
    private String facilityType;

    /**
     * 位置
     */
    private String location;

    /**
     * 开放时间
     */
    private String openTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    private Integer deleted;
}
