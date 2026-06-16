package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("park_property_tag_rel")
public class ParkPropertyTagRel {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long propertyId;

    private Long tagId;

    private LocalDateTime createTime;
}
