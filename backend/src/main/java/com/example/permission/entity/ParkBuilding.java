package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 楼宇实体
 */
@Data
@Table("park_building")
public class ParkBuilding {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 园区ID
     */
    private Long parkId;

    /**
     * 楼宇名称
     */
    private String buildingName;

    /**
     * 楼宇编号
     */
    private String buildingCode;

    /**
     * 楼宇类型：1-住宅楼，2-商业楼，3-综合楼
     */
    private Integer buildingType;

    /**
     * 楼宇子类型
     */
    private String buildingSubType;

    /**
     * 地上楼层数
     */
    private Integer totalFloors;

    /**
     * 地下楼层数
     */
    private Integer undergroundFloors;

    /**
     * 建筑结构
     */
    private String structureType;

    /**
     * 建成年份
     */
    private Integer builtYear;

    /**
     * 客梯数量
     */
    private Integer passengerElevators;

    /**
     * 货梯数量
     */
    private Integer freightElevators;

    /**
     * 楼宇状态：1-正常使用，2-装修中，3-待交付
     */
    private Integer status;

    /**
     * 楼宇简介
     */
    private String description;

    /**
     * 主图URL
     */
    private String mainImage;

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
     * 楼宇图片列表（非数据库字段）
     */
    @Column(ignore = true)
    private List<ParkBuildingImage> images;

    /**
     * 楼层列表（非数据库字段）
     */
    @Column(ignore = true)
    private List<ParkFloor> floors;
}
