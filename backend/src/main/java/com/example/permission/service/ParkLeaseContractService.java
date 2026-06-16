
package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.*;
import com.example.permission.mapper.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.ParkLeaseContractTableDef.PARK_LEASE_CONTRACT;
import static com.example.permission.entity.table.ParkLeaseContractLogTableDef.PARK_LEASE_CONTRACT_LOG;
import static com.example.permission.entity.table.ParkTenantPropertyTableDef.PARK_TENANT_PROPERTY;
import static com.example.permission.entity.table.ParkPropertyTableDef.PARK_PROPERTY;
import static com.example.permission.entity.table.ParkTenantTableDef.PARK_TENANT;
import static com.example.permission.entity.table.ParkOwnerTableDef.PARK_OWNER;
import static com.example.permission.entity.table.ParkBuildingTableDef.PARK_BUILDING;
import static com.example.permission.entity.table.ParkFloorTableDef.PARK_FLOOR;
import static com.example.permission.entity.table.ParkPropertyStatusLogTableDef.PARK_PROPERTY_STATUS_LOG;

@Service
public class ParkLeaseContractService {

    @Autowired
    private ParkLeaseContractMapper parkLeaseContractMapper;

    @Autowired
    private ParkLeaseContractLogMapper parkLeaseContractLogMapper;

    @Autowired
    private ParkTenantPropertyMapper parkTenantPropertyMapper;

    @Autowired
    private ParkPropertyMapper parkPropertyMapper;

    @Autowired
    private ParkTenantMapper parkTenantMapper;

    @Autowired
    private ParkOwnerMapper parkOwnerMapper;

    @Autowired
    private ParkBuildingMapper parkBuildingMapper;

    @Autowired
    private ParkFloorMapper parkFloorMapper;

    @Autowired
    private ParkPropertyStatusLogMapper parkPropertyStatusLogMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PageResult<ParkLeaseContract> pageList(Integer pageNum, Integer pageSize, String keyword,
                                                  Integer contractStatus, Integer leaseType,
                                                  Long tenantId, Long propertyId, Long ownerId,
                                                  LocalDate startDateStart, LocalDate startDateEnd,
                                                  LocalDate endDateStart, LocalDate endDateEnd,
                                                  String sortField, String sortOrder) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkLeaseContract.class)
                .where(PARK_LEASE_CONTRACT.DELETED.eq(0));

        if (keyword != null && !keyword.isEmpty()) {
            query.and(PARK_LEASE_CONTRACT.CONTRACT_CODE.like(keyword)
                    .or(PARK_LEASE_CONTRACT.TENANT_NAME.like(keyword))
                    .or(PARK_LEASE_CONTRACT.PROPERTY_CODE.like(keyword)));
        }
        if (contractStatus != null) {
            query.and(PARK_LEASE_CONTRACT.CONTRACT_STATUS.eq(contractStatus));
        }
        if (leaseType != null) {
            query.and(PARK_LEASE_CONTRACT.LEASE_TYPE.eq(leaseType));
        }
        if (tenantId != null) {
            query.and(PARK_LEASE_CONTRACT.TENANT_ID.eq(tenantId));
        }
        if (propertyId != null) {
            query.and(PARK_LEASE_CONTRACT.PROPERTY_ID.eq(propertyId));
        }
        if (ownerId != null) {
            query.and(PARK_LEASE_CONTRACT.OWNER_ID.eq(ownerId));
        }
        if (startDateStart != null) {
            query.and(PARK_LEASE_CONTRACT.START_DATE.ge(startDateStart));
        }
        if (startDateEnd != null) {
            query.and(PARK_LEASE_CONTRACT.START_DATE.le(startDateEnd));
        }
        if (endDateStart != null) {
            query.and(PARK_LEASE_CONTRACT.END_DATE.ge(endDateStart));
        }
        if (endDateEnd != null) {
            query.and(PARK_LEASE_CONTRACT.END_DATE.le(endDateEnd));
        }

        if (sortField != null && !sortField.isEmpty()) {
            switch (sortField) {
                case "contractCode":
                    query.orderBy("asc".equals(sortOrder) ? PARK_LEASE_CONTRACT.CONTRACT_CODE.asc() : PARK_LEASE_CONTRACT.CONTRACT_CODE.desc());
                    break;
                case "startDate":
                    query.orderBy("asc".equals(sortOrder) ? PARK_LEASE_CONTRACT.START_DATE.asc() : PARK_LEASE_CONTRACT.START_DATE.desc());
                    break;
                case "endDate":
                    query.orderBy("asc".equals(sortOrder) ? PARK_LEASE_CONTRACT.END_DATE.asc() : PARK_LEASE_CONTRACT.END_DATE.desc());
                    break;
                case "monthlyRent":
                    query.orderBy("asc".equals(sortOrder) ? PARK_LEASE_CONTRACT.MONTHLY_RENT.asc() : PARK_LEASE_CONTRACT.MONTHLY_RENT.desc());
                    break;
                default:
                    query.orderBy(PARK_LEASE_CONTRACT.CREATE_TIME.desc());
            }
        } else {
            query.orderBy(PARK_LEASE_CONTRACT.CREATE_TIME.desc());
        }

        Page<ParkLeaseContract> page = parkLeaseContractMapper.paginate(Page.of(pageNum, pageSize), query);

        List<ParkLeaseContract> list = page.getRecords();
        for (ParkLeaseContract contract : list) {
            enrichContract(contract);
        }

        return new PageResult<>(page.getTotalRow(), list,
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public ParkLeaseContract getById(Long id) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkLeaseContract.class)
                .where(PARK_LEASE_CONTRACT.ID.eq(id))
                .and(PARK_LEASE_CONTRACT.DELETED.eq(0));
        ParkLeaseContract contract = parkLeaseContractMapper.selectOneByQuery(query);
        if (contract != null) {
            enrichContract(contract);
            contract.setOperationLogs(getContractLogs(id));
            if (contract.getOtherAttachments() != null && !contract.getOtherAttachments().isEmpty()) {
                try {
                    List<String> attachments = objectMapper.readValue(contract.getOtherAttachments(), new TypeReference<List<String>>() {});
                    contract.setOtherAttachmentList(attachments);
                } catch (Exception ignored) {}
            }
        }
        return contract;
    }

    public ParkLeaseContract getByCode(String contractCode) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkLeaseContract.class)
                .where(PARK_LEASE_CONTRACT.CONTRACT_CODE.eq(contractCode))
                .and(PARK_LEASE_CONTRACT.DELETED.eq(0));
        return parkLeaseContractMapper.selectOneByQuery(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(ParkLeaseContract contract, Long operatorId, String operatorName) {
        ParkProperty property = parkPropertyMapper.selectOneById(contract.getPropertyId());
        if (property == null) {
            throw new BusinessException("房产不存在");
        }

        if (property.getStatus() != 4 && property.getStatus() != 5) {
            throw new BusinessException("房产状态必须为\"业主出租\"或\"空置\"才能创建租赁合约");
        }

        if (property.getOwnerId() == null) {
            throw new BusinessException("该房产未关联业主，无法创建合约");
        }
        contract.setOwnerId(property.getOwnerId());

        ParkTenant tenant = parkTenantMapper.selectOneById(contract.getTenantId());
        if (tenant == null) {
            throw new BusinessException("租户不存在");
        }

        ParkOwner owner = parkOwnerMapper.selectOneById(contract.getOwnerId());
        if (owner == null) {
            throw new BusinessException("业主不存在");
        }

        validateContract(contract);

        contract.setContractCode(generateContractCode());
        contract.setTenantCode(tenant.getTenantCode());
        contract.setTenantName(tenant.getName());
        contract.setPropertyCode(property.getPropertyCode());
        contract.setOwnerCode(owner.getOwnerCode());
        contract.setOwnerName(owner.getName());
        contract.setContractStatus(1);
        contract.setDeleted(0);

        if (contract.getStartDate() != null && contract.getEndDate() != null) {
            Period period = Period.between(contract.getStartDate(), contract.getEndDate());
            int months = period.getYears() * 12 + period.getMonths();
            contract.setLeaseMonths(months > 0 ? months : 1);
        }

        contract.setCreateTime(LocalDateTime.now());
        contract.setUpdateTime(LocalDateTime.now());
        parkLeaseContractMapper.insert(contract);

        addContractLog(contract.getId(), contract.getContractCode(), "创建", "创建租赁合约", operatorId, operatorName);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(ParkLeaseContract contract) {
        ParkLeaseContract existContract = parkLeaseContractMapper.selectOneById(contract.getId());
        if (existContract == null) {
            throw new BusinessException("合约不存在");
        }

        if (existContract.getContractStatus() != 1) {
            throw new BusinessException("只有\"待生效\"状态的合约可以修改");
        }

        contract.setContractCode(null);
        contract.setContractStatus(null);

        if (contract.getStartDate() != null && contract.getEndDate() != null) {
            Period period = Period.between(contract.getStartDate(), contract.getEndDate());
            int months = period.getYears() * 12 + period.getMonths();
            contract.setLeaseMonths(months > 0 ? months : 1);
        }

        contract.setUpdateTime(LocalDateTime.now());
        parkLeaseContractMapper.update(contract);
    }

    @Transactional(rollbackFor = Exception.class)
    public void audit(Long contractId, Integer auditResult, String auditOpinion, String rejectReason,
                      Long operatorId, String operatorName) {
        ParkLeaseContract contract = parkLeaseContractMapper.selectOneById(contractId);
        if (contract == null) {
            throw new BusinessException("合约不存在");
        }

        if (contract.getContractStatus() != 1) {
            throw new BusinessException("只有\"待生效\"状态的合约可以审核");
        }

        if (auditResult != 2 && auditResult != 6) {
            throw new BusinessException("无效的审核结果");
        }

        if (auditResult == 6 && (rejectReason == null || rejectReason.isEmpty())) {
            throw new BusinessException("审核拒绝时必须填写拒绝原因");
        }

        if (auditResult == 2) {
            contract.setContractStatus(2);
            contract.setAuditTime(LocalDateTime.now());
            contract.setAuditorId(operatorId);
            contract.setAuditorName(operatorName);

            ParkProperty property = parkPropertyMapper.selectOneById(contract.getPropertyId());
            if (property != null) {
                Integer oldStatus = property.getStatus();
                property.setStatus(6);
                property.setTenantId(contract.getTenantId());
                property.setUpdateTime(LocalDateTime.now());
                parkPropertyMapper.update(property);

                addPropertyStatusLog(property.getId(), property.getPropertyCode(), oldStatus, 6,
                        "租赁合约审核通过，房产状态更新为已出租", operatorId, operatorName);
            }

            createTenantPropertyRelation(contract);
            updateTenantStatus(contract.getTenantId(), 1);

            addContractLog(contractId, contract.getContractCode(), "审核通过", auditOpinion, operatorId, operatorName);
        } else {
            contract.setContractStatus(6);
            contract.setAuditRejectReason(rejectReason);
            contract.setAuditTime(LocalDateTime.now());
            contract.setAuditorId(operatorId);
            contract.setAuditorName(operatorName);
            addContractLog(contractId, contract.getContractCode(), "审核拒绝", rejectReason, operatorId, operatorName);
        }

        contract.setUpdateTime(LocalDateTime.now());
        parkLeaseContractMapper.update(contract);
    }

    @Transactional(rollbackFor = Exception.class)
    public ParkLeaseContract renew(Long contractId, LocalDate newStartDate, LocalDate newEndDate,
                                   BigDecimal newMonthlyRent, BigDecimal newDepositAmount,
                                   Long operatorId, String operatorName) {
        ParkLeaseContract oldContract = parkLeaseContractMapper.selectOneById(contractId);
        if (oldContract == null) {
            throw new BusinessException("原合约不存在");
        }

        if (oldContract.getContractStatus() != 2) {
            throw new BusinessException("只有\"生效中\"状态的合约可以续签");
        }

        if (newStartDate == null || newEndDate == null) {
            throw new BusinessException("请填写新的租赁期限");
        }

        if (!newStartDate.isAfter(oldContract.getEndDate())) {
            throw new BusinessException("新合约起始日期必须在原合约结束日期之后");
        }

        ParkLeaseContract newContract = new ParkLeaseContract();
        newContract.setTenantId(oldContract.getTenantId());
        newContract.setPropertyId(oldContract.getPropertyId());
        newContract.setOwnerId(oldContract.getOwnerId());
        newContract.setTrusteeId(oldContract.getTrusteeId());
        newContract.setTrusteeName(oldContract.getTrusteeName());
        newContract.setLeaseType(oldContract.getLeaseType());
        newContract.setStartDate(newStartDate);
        newContract.setEndDate(newEndDate);
        newContract.setMonthlyRent(newMonthlyRent != null ? newMonthlyRent : oldContract.getMonthlyRent());
        newContract.setDepositAmount(newDepositAmount != null ? newDepositAmount : oldContract.getDepositAmount());
        newContract.setPaymentMethod(oldContract.getPaymentMethod());
        newContract.setIncludePropertyFee(oldContract.getIncludePropertyFee());
        newContract.setPropertyFeeMonthly(oldContract.getPropertyFeeMonthly());
        newContract.setWaterFeeBearer(oldContract.getWaterFeeBearer());
        newContract.setElectricityFeeBearer(oldContract.getElectricityFeeBearer());
        newContract.setGasFeeBearer(oldContract.getGasFeeBearer());
        newContract.setEarlyTerminationPenalty(oldContract.getEarlyTerminationPenalty());
        newContract.setLatePaymentPenaltyRate(oldContract.getLatePaymentPenaltyRate());
        newContract.setOriginalContractId(oldContract.getId());
        newContract.setContractTerms(oldContract.getContractTerms());

        add(newContract, operatorId, operatorName);

        oldContract.setContractStatus(3);
        oldContract.setUpdateTime(LocalDateTime.now());
        parkLeaseContractMapper.update(oldContract);

        addContractLog(oldContract.getId(), oldContract.getContractCode(), "续签",
                "续签新合约：" + newContract.getContractCode(), operatorId, operatorName);

        return newContract;
    }

    @Transactional(rollbackFor = Exception.class)
    public void requestTermination(Long contractId, String terminationReason, LocalDate terminationDate,
                                   BigDecimal terminationPenalty, Long operatorId, String operatorName) {
        ParkLeaseContract contract = parkLeaseContractMapper.selectOneById(contractId);
        if (contract == null) {
            throw new BusinessException("合约不存在");
        }

        if (contract.getContractStatus() != 2) {
            throw new BusinessException("只有\"生效中\"状态的合约可以解除");
        }

        if (terminationReason == null || terminationReason.isEmpty()) {
            throw new BusinessException("请填写解除原因");
        }
        if (terminationDate == null) {
            throw new BusinessException("请填写解除日期");
        }

        contract.setTerminationReason(terminationReason);
        contract.setTerminationDate(terminationDate);
        contract.setTerminationPenalty(terminationPenalty != null ? terminationPenalty : BigDecimal.ZERO);
        contract.setTerminationAuditStatus(1);
        contract.setUpdateTime(LocalDateTime.now());
        parkLeaseContractMapper.update(contract);

        addContractLog(contractId, contract.getContractCode(), "申请解除",
                "解除原因：" + terminationReason + "，解除日期：" + terminationDate, operatorId, operatorName);
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditTermination(Long contractId, Integer auditResult, String auditOpinion,
                                 Long operatorId, String operatorName) {
        ParkLeaseContract contract = parkLeaseContractMapper.selectOneById(contractId);
        if (contract == null) {
            throw new BusinessException("合约不存在");
        }

        if (contract.getTerminationAuditStatus() != 1) {
            throw new BusinessException("该合约没有待审核的解除申请");
        }

        if (auditResult != 2 && auditResult != 3) {
            throw new BusinessException("无效的审核结果");
        }

        if (auditResult == 2) {
            contract.setContractStatus(4);
            contract.setTerminationAuditStatus(2);
            contract.setTerminationAuditTime(LocalDateTime.now());
            contract.setTerminationAuditorId(operatorId);
            contract.setTerminationAuditorName(operatorName);

            ParkProperty property = parkPropertyMapper.selectOneById(contract.getPropertyId());
            if (property != null) {
                Integer oldStatus = property.getStatus();
                property.setStatus(5);
                property.setTenantId(null);
                property.setUpdateTime(LocalDateTime.now());
                parkPropertyMapper.update(property);

                addPropertyStatusLog(property.getId(), property.getPropertyCode(), oldStatus, 5,
                        "租赁合约解除，房产状态恢复为空置", operatorId, operatorName);
            }

            endTenantPropertyRelation(contract);

            checkAndUpdateTenantStatus(contract.getTenantId());

            addContractLog(contractId, contract.getContractCode(), "解除审核通过",
                    auditOpinion, operatorId, operatorName);
        } else {
            contract.setTerminationAuditStatus(3);
            contract.setTerminationAuditTime(LocalDateTime.now());
            contract.setTerminationAuditorId(operatorId);
            contract.setTerminationAuditorName(operatorName);
            addContractLog(contractId, contract.getContractCode(), "解除审核拒绝",
                    auditOpinion, operatorId, operatorName);
        }

        contract.setUpdateTime(LocalDateTime.now());
        parkLeaseContractMapper.update(contract);
    }

    @Transactional(rollbackFor = Exception.class)
    public void checkAndUpdateExpiredContracts() {
        LocalDate today = LocalDate.now();
        QueryWrapper query = QueryWrapper.create()
                .from(ParkLeaseContract.class)
                .where(PARK_LEASE_CONTRACT.CONTRACT_STATUS.eq(2))
                .and(PARK_LEASE_CONTRACT.END_DATE.lt(today))
                .and(PARK_LEASE_CONTRACT.DELETED.eq(0));

        List<ParkLeaseContract> expiredContracts = parkLeaseContractMapper.selectListByQuery(query);
        for (ParkLeaseContract contract : expiredContracts) {
            contract.setContractStatus(5);
            contract.setUpdateTime(LocalDateTime.now());
            parkLeaseContractMapper.update(contract);

            ParkProperty property = parkPropertyMapper.selectOneById(contract.getPropertyId());
            if (property != null) {
                Integer oldStatus = property.getStatus();
                property.setStatus(5);
                property.setTenantId(null);
                property.setUpdateTime(LocalDateTime.now());
                parkPropertyMapper.update(property);

                addPropertyStatusLog(property.getId(), property.getPropertyCode(), oldStatus, 5,
                        "租赁合约到期，房产状态恢复为空置", null, "系统自动");
            }

            endTenantPropertyRelation(contract);
            checkAndUpdateTenantStatus(contract.getTenantId());

            addContractLog(contract.getId(), contract.getContractCode(), "到期",
                    "合约到期自动结束", null, "系统自动");
        }
    }

    private void validateContract(ParkLeaseContract contract) {
        if (contract.getTenantId() == null) {
            throw new BusinessException("请选择租户");
        }
        if (contract.getPropertyId() == null) {
            throw new BusinessException("请选择租赁房产");
        }
        if (contract.getOwnerId() == null) {
            throw new BusinessException("请选择房产业主");
        }
        if (contract.getLeaseType() == null) {
            throw new BusinessException("请选择租赁类型");
        }
        if (contract.getStartDate() == null) {
            throw new BusinessException("请填写租赁起始日期");
        }
        if (contract.getEndDate() == null) {
            throw new BusinessException("请填写租赁结束日期");
        }
        if (contract.getStartDate().isAfter(contract.getEndDate())) {
            throw new BusinessException("租赁起始日期不能晚于结束日期");
        }
        if (contract.getMonthlyRent() == null || contract.getMonthlyRent().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("请填写有效的月租金");
        }
        if (contract.getPaymentMethod() == null) {
            throw new BusinessException("请选择付款方式");
        }
    }

    private String generateContractCode() {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkLeaseContract.class)
                .orderBy(PARK_LEASE_CONTRACT.CONTRACT_CODE.desc())
                .limit(1);
        ParkLeaseContract last = parkLeaseContractMapper.selectOneByQuery(query);
        int seq = 1;
        if (last != null && last.getContractCode() != null) {
            try {
                seq = Integer.parseInt(last.getContractCode().replace("HT", "")) + 1;
            } catch (Exception ignored) {}
        }
        return String.format("HT%03d", seq);
    }

    private void enrichContract(ParkLeaseContract contract) {
        contract.setLeaseTypeText(contract.getLeaseType() != null ?
                (contract.getLeaseType() == 1 ? "整租" : "合租") : "");
        contract.setContractStatusText(contract.getContractStatus() != null ?
                (contract.getContractStatus() == 1 ? "待生效" :
                        contract.getContractStatus() == 2 ? "生效中" :
                                contract.getContractStatus() == 3 ? "已续签" :
                                        contract.getContractStatus() == 4 ? "已解除" :
                                                contract.getContractStatus() == 5 ? "已到期" : "已拒绝") : "");
        contract.setPaymentMethodText(contract.getPaymentMethod() != null ?
                (contract.getPaymentMethod() == 1 ? "月付" :
                        contract.getPaymentMethod() == 2 ? "季付" :
                                contract.getPaymentMethod() == 3 ? "半年付" : "年付") : "");
        contract.setWaterFeeBearerText(contract.getWaterFeeBearer() != null ?
                (contract.getWaterFeeBearer() == 1 ? "租户承担" : "业主承担") : "");
        contract.setElectricityFeeBearerText(contract.getElectricityFeeBearer() != null ?
                (contract.getElectricityFeeBearer() == 1 ? "租户承担" : "业主承担") : "");
        contract.setGasFeeBearerText(contract.getGasFeeBearer() != null ?
                (contract.getGasFeeBearer() == 1 ? "租户承担" : "业主承担") : "");
        contract.setTerminationAuditStatusText(contract.getTerminationAuditStatus() != null ?
                (contract.getTerminationAuditStatus() == 1 ? "待审核" :
                        contract.getTerminationAuditStatus() == 2 ? "已通过" : "已拒绝") : "");

        if (contract.getTenantId() != null) {
            contract.setTenant(parkTenantMapper.selectOneById(contract.getTenantId()));
        }
        if (contract.getPropertyId() != null) {
            ParkProperty property = parkPropertyMapper.selectOneById(contract.getPropertyId());
            contract.setProperty(property);
            if (property != null && property.getBuildingId() != null) {
                contract.setBuilding(parkBuildingMapper.selectOneById(property.getBuildingId()));
            }
            if (property != null && property.getFloorId() != null) {
                contract.setFloor(parkFloorMapper.selectOneById(property.getFloorId()));
            }
        }
        if (contract.getOwnerId() != null) {
            contract.setOwner(parkOwnerMapper.selectOneById(contract.getOwnerId()));
        }
    }

    private List<ParkLeaseContractLog> getContractLogs(Long contractId) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkLeaseContractLog.class)
                .where(PARK_LEASE_CONTRACT_LOG.CONTRACT_ID.eq(contractId))
                .orderBy(PARK_LEASE_CONTRACT_LOG.CREATE_TIME.desc());
        return parkLeaseContractLogMapper.selectListByQuery(query);
    }

    private void addContractLog(Long contractId, String contractCode, String operationType,
                                String operationDetail, Long operatorId, String operatorName) {
        ParkLeaseContractLog log = new ParkLeaseContractLog();
        log.setContractId(contractId);
        log.setContractCode(contractCode);
        log.setOperationType(operationType);
        log.setOperationDetail(operationDetail);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setCreateTime(LocalDateTime.now());
        parkLeaseContractLogMapper.insert(log);
    }

    private void createTenantPropertyRelation(ParkLeaseContract contract) {
        QueryWrapper existQuery = QueryWrapper.create()
                .from(ParkTenantProperty.class)
                .where(PARK_TENANT_PROPERTY.TENANT_ID.eq(contract.getTenantId()))
                .and(PARK_TENANT_PROPERTY.PROPERTY_ID.eq(contract.getPropertyId()))
                .and(PARK_TENANT_PROPERTY.CONTRACT_ID.eq(contract.getId()));
        Long existCount = parkTenantPropertyMapper.selectCountByQuery(existQuery);
        if (existCount != null && existCount > 0) {
            return;
        }

        ParkTenantProperty relation = new ParkTenantProperty();
        relation.setTenantId(contract.getTenantId());
        relation.setTenantCode(contract.getTenantCode());
        relation.setPropertyId(contract.getPropertyId());
        relation.setPropertyCode(contract.getPropertyCode());
        relation.setContractId(contract.getId());
        relation.setContractCode(contract.getContractCode());
        relation.setRelationStatus(1);
        relation.setStartDate(contract.getStartDate());
        relation.setEndDate(contract.getEndDate());
        relation.setCreateTime(LocalDateTime.now());
        relation.setUpdateTime(LocalDateTime.now());
        parkTenantPropertyMapper.insert(relation);
    }

    private void endTenantPropertyRelation(ParkLeaseContract contract) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkTenantProperty.class)
                .where(PARK_TENANT_PROPERTY.CONTRACT_ID.eq(contract.getId()))
                .and(PARK_TENANT_PROPERTY.RELATION_STATUS.eq(1));
        List<ParkTenantProperty> relations = parkTenantPropertyMapper.selectListByQuery(query);
        for (ParkTenantProperty relation : relations) {
            relation.setRelationStatus(2);
            relation.setEndDate(contract.getTerminationDate() != null ? contract.getTerminationDate() : contract.getEndDate());
            relation.setUpdateTime(LocalDateTime.now());
            parkTenantPropertyMapper.update(relation);
        }
    }

    private void updateTenantStatus(Long tenantId, Integer status) {
        if (tenantId == null) return;
        ParkTenant tenant = parkTenantMapper.selectOneById(tenantId);
        if (tenant != null && tenant.getTenantStatus() != 3) {
            tenant.setTenantStatus(status);
            tenant.setUpdateTime(LocalDateTime.now());
            parkTenantMapper.update(tenant);
        }
    }

    private void checkAndUpdateTenantStatus(Long tenantId) {
        if (tenantId == null) return;

        QueryWrapper query = QueryWrapper.create()
                .from(ParkLeaseContract.class)
                .where(PARK_LEASE_CONTRACT.TENANT_ID.eq(tenantId))
                .and(PARK_LEASE_CONTRACT.CONTRACT_STATUS.eq(2))
                .and(PARK_LEASE_CONTRACT.DELETED.eq(0));
        Long activeCount = parkLeaseContractMapper.selectCountByQuery(query);

        ParkTenant tenant = parkTenantMapper.selectOneById(tenantId);
        if (tenant != null && tenant.getTenantStatus() != 3) {
            if (activeCount != null && activeCount > 0) {
                tenant.setTenantStatus(1);
            } else {
                tenant.setTenantStatus(2);
            }
            tenant.setUpdateTime(LocalDateTime.now());
            parkTenantMapper.update(tenant);
        }
    }

    private void addPropertyStatusLog(Long propertyId, String propertyCode, Integer oldStatus, Integer newStatus,
                                      String changeReason, Long operatorId, String operatorName) {
        ParkPropertyStatusLog log = new ParkPropertyStatusLog();
        log.setPropertyId(propertyId);
        log.setPropertyCode(propertyCode);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setChangeReason(changeReason);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setCreateTime(LocalDateTime.now());
        parkPropertyStatusLogMapper.insert(log);
    }
}
