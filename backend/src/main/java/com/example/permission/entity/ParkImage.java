package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 园区图片实体
 */
@Data
@Table("park_image")
public class ParkImage {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 园区ID
     */
    private Long parkId;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 图片类型
     */
    private String imageType;

    /**
     * 图片名称
     */
    private String imageName;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 是否主图：0-否，1-是
     */
    private Integer isMain;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
