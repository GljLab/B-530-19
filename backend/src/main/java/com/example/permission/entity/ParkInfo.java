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

/**
 * 园区信息实体
 */
@Data
@Table("park_info")
public class ParkInfo {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 园区名称
     */
    private String parkName;

    /**
     * 园区类型：1-住宅小区，2-商业写字楼，3-产业园区，4-综合体
     */
    private Integer parkType;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 物业公司名称
     */
    private String propertyCompany;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 客服热线
     */
    private String serviceHotline;

    /**
     * 建成时间
     */
    private LocalDate builtTime;

    /**
     * 占地面积
     */
    private BigDecimal landArea;

    /**
     * 建筑面积
     */
    private BigDecimal buildingArea;

    /**
     * 绿化率
     */
    private BigDecimal greenRate;

    /**
     * 园区简介
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
     * 配套设施列表（非数据库字段）
     */
    @Column(ignore = true)
    private List<ParkFacility> facilities;

    /**
     * 园区图片列表（非数据库字段）
     */
    @Column(ignore = true)
    private List<ParkImage> images;
}
