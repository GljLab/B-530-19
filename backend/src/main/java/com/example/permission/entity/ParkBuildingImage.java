package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 楼宇图片实体
 */
@Data
@Table("park_building_image")
public class ParkBuildingImage {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 楼宇ID
     */
    private Long buildingId;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 图片名称
     */
    private String imageName;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 是否主图
     */
    private Integer isMain;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
