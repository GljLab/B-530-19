package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("park_property_vacancy_log")
public class ParkPropertyVacancyLog {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long propertyId;

    private String propertyCode;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer vacancyDays;

    private String vacancyReason;

    private String vacancyReasonRemark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
