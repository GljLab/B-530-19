package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 楼层实体
 */
@Data
@Table("park_floor")
public class ParkFloor {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 楼宇ID
     */
    private Long buildingId;

    /**
     * 楼层号
     */
    private String floorNumber;

    /**
     * 楼层类型：1-住宅楼层，2-商业楼层，3-停车楼层，4-设备楼层
     */
    private Integer floorType;

    /**
     * 楼层用途
     */
    private String floorPurpose;

    /**
     * 房产数量
     */
    private Integer propertyCount;

    /**
     * 楼层平面图URL
     */
    private String floorPlan;

    /**
     * 楼层状态：1-正常，2-改造中，3-封闭
     */
    private Integer status;

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

    /**
     * 楼宇名称（非数据库字段）
     */
    @Column(ignore = true)
    private String buildingName;
}
