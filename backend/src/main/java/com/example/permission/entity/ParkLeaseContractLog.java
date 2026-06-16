
package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("park_lease_contract_log")
public class ParkLeaseContractLog {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long contractId;

    private String contractCode;

    private String operationType;

    private String operationDetail;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime createTime;
}
