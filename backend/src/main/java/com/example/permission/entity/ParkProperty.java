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
 * 房产实体
 */
@Data
@Table("park_property")
public class ParkProperty {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 房产编号（楼栋-房号，全园区唯一）
     */
    private String propertyCode;

    /**
     * 楼宇ID
     */
    private Long buildingId;

    /**
     * 楼层ID
     */
    private Long floorId;

    /**
     * 楼层号（冗余字段，方便查询）
     */
    private String floorNumber;

    /**
     * 房产类型：1-住宅，2-商业，3-其他
     */
    private Integer propertyType;

    /**
     * 房产子类型：普通住宅/公寓/别墅/复式/loft/写字间/商铺/底商/仓库/设备间
     */
    private String propertySubType;

    /**
     * 建筑面积（㎡）
     */
    private BigDecimal buildingArea;

    /**
     * 套内面积（㎡）
     */
    private BigDecimal insideArea;

    /**
     * 公摊面积（㎡）
     */
    private BigDecimal sharedArea;

    /**
     * 户型：一室/两室/三室/四室及以上
     */
    private String houseType;

    /**
     * 朝向：东/南/西/北/东南/西南/东北/西北
     */
    private String orientation;

    /**
     * 装修状况：毛坯/简装/精装/豪装
     */
    private String decorationStatus;

    /**
     * 产权性质：商品房/经济适用房/公租房/商铺产权
     */
    private String propertyRightType;

    /**
     * 产权年限：40/50/70
     */
    private Integer propertyRightYears;

    /**
     * 产权证号
     */
    private String propertyCertNo;

    /**
     * 交付日期
     */
    private LocalDate deliveryDate;

    /**
     * 房产状态：1-待售，2-已售未交付，3-业主自住，4-业主出租，5-空置，6-装修中，7-查封
     */
    private Integer status;

    private LocalDate vacantSince;

    private String vacancyReason;

    private String vacancyReasonRemark;

    private Integer isLongTermVacant;

    /**
     * 特殊标识（逗号分隔）：学区房/人才房/拆迁房
     */
    private String specialTags;

    /**
     * 备注
     */
    private String remark;

    /**
     * 产权人ID（后续关联业主）
     */
    private Long ownerId;

    /**
     * 租户ID（后续关联租户）
     */
    private Long tenantId;

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

    /**
     * 特殊标识列表（非数据库字段）
     */
    @Column(ignore = true)
    private List<String> specialTagList;

    /**
     * 状态变更日志列表（非数据库字段）
     */
    @Column(ignore = true)
    private List<ParkPropertyStatusLog> statusLogs;

    @Column(ignore = true)
    private List<ParkPropertyTag> tagList;

    @Column(ignore = true)
    private List<ParkPropertyTransfer> transferHistory;

    @Column(ignore = true)
    private String ownerName;

    @Column(ignore = true)
    private String tenantName;

    @Column(ignore = true)
    private Long vacancyDays;
}
