package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.*;
import com.example.permission.mapper.*;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.permission.entity.table.ParkPropertyTransferTableDef.PARK_PROPERTY_TRANSFER;
import static com.example.permission.entity.table.ParkOwnerPropertyTableDef.PARK_OWNER_PROPERTY;
import static com.example.permission.entity.table.ParkPropertyTableDef.PARK_PROPERTY;
import static com.example.permission.entity.table.ParkPropertyStatusLogTableDef.PARK_PROPERTY_STATUS_LOG;

@Service
public class ParkPropertyTransferService {

    @Autowired
    private ParkPropertyTransferMapper parkPropertyTransferMapper;

    @Autowired
    private ParkPropertyMapper parkPropertyMapper;

    @Autowired
    private ParkOwnerPropertyMapper parkOwnerPropertyMapper;

    @Autowired
    private ParkOwnerMapper parkOwnerMapper;

    @Autowired
    private ParkPropertyStatusLogMapper parkPropertyStatusLogMapper;

    @Autowired
    private ParkBuildingMapper parkBuildingMapper;

    @Transactional(rollbackFor = Exception.class)
    public ParkPropertyTransfer applyTransfer(Long propertyId, Long newOwnerId, Integer transferType,
                                               BigDecimal transferPrice, LocalDate transferDate,
                                               String newPropertyCertNo, String transferAgreementUrl,
                                               String newCertUrl, Long operatorId, String operatorName) {
        ParkProperty property = parkPropertyMapper.selectOneById(propertyId);
        if (property == null || property.getDeleted() == 1) {
            throw new BusinessException("房产不存在");
        }

        QueryWrapper ownerQuery = QueryWrapper.create()
                .from(ParkOwnerProperty.class)
                .where(PARK_OWNER_PROPERTY.PROPERTY_ID.eq(propertyId))
                .and(PARK_OWNER_PROPERTY.RELATION_STATUS.eq(1))
                .limit(1);
        ParkOwnerProperty currentOwnerProperty = parkOwnerPropertyMapper.selectOneByQuery(ownerQuery);

        Long oldOwnerId;
        if (currentOwnerProperty != null) {
            oldOwnerId = currentOwnerProperty.getOwnerId();
        } else {
            oldOwnerId = property.getOwnerId();
        }

        ParkOwner oldOwner = parkOwnerMapper.selectOneById(oldOwnerId);
        String oldOwnerName = oldOwner != null ? oldOwner.getName() : "";

        ParkOwner newOwner = parkOwnerMapper.selectOneById(newOwnerId);
        String newOwnerName = newOwner != null ? newOwner.getName() : "";

        if (transferType == 1 && transferPrice == null) {
            throw new BusinessException("买卖转让价格不能为空");
        }

        String transferNo = "TR" + System.currentTimeMillis();

        ParkPropertyTransfer transfer = new ParkPropertyTransfer();
        transfer.setTransferNo(transferNo);
        transfer.setPropertyId(propertyId);
        transfer.setPropertyCode(property.getPropertyCode());
        transfer.setOldOwnerId(oldOwnerId);
        transfer.setOldOwnerName(oldOwnerName);
        transfer.setNewOwnerId(newOwnerId);
        transfer.setNewOwnerName(newOwnerName);
        transfer.setTransferType(transferType);
        transfer.setTransferPrice(transferPrice);
        transfer.setTransferDate(transferDate);
        transfer.setNewPropertyCertNo(newPropertyCertNo);
        transfer.setTransferAgreementUrl(transferAgreementUrl);
        transfer.setNewCertUrl(newCertUrl);
        transfer.setAuditStatus(1);
        transfer.setApplyOperatorId(operatorId);
        transfer.setApplyOperatorName(operatorName);
        transfer.setCreateTime(LocalDateTime.now());
        transfer.setUpdateTime(LocalDateTime.now());
        parkPropertyTransferMapper.insert(transfer);

        return transfer;
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditTransfer(Long transferId, Integer auditResult, String auditOpinion,
                               Long auditorId, String auditorName) {
        ParkPropertyTransfer transfer = parkPropertyTransferMapper.selectOneById(transferId);
        if (transfer == null) {
            throw new BusinessException("转让记录不存在");
        }
        if (transfer.getAuditStatus() != 1) {
            throw new BusinessException("该转让记录已审核");
        }

        if (auditResult == 2) {
            transfer.setAuditStatus(2);
            transfer.setAuditorId(auditorId);
            transfer.setAuditorName(auditorName);
            transfer.setAuditTime(LocalDateTime.now());
            transfer.setAuditOpinion(auditOpinion);
            transfer.setUpdateTime(LocalDateTime.now());
            parkPropertyTransferMapper.update(transfer);

            completeTransfer(transferId);
        } else if (auditResult == 3) {
            transfer.setAuditStatus(3);
            transfer.setAuditorId(auditorId);
            transfer.setAuditorName(auditorName);
            transfer.setAuditTime(LocalDateTime.now());
            transfer.setAuditOpinion(auditOpinion);
            transfer.setUpdateTime(LocalDateTime.now());
            parkPropertyTransferMapper.update(transfer);
        }
    }

    private void completeTransfer(Long transferId) {
        ParkPropertyTransfer transfer = parkPropertyTransferMapper.selectOneById(transferId);

        QueryWrapper oldOwnerQuery = QueryWrapper.create()
                .from(ParkOwnerProperty.class)
                .where(PARK_OWNER_PROPERTY.PROPERTY_ID.eq(transfer.getPropertyId()))
                .and(PARK_OWNER_PROPERTY.OWNER_ID.eq(transfer.getOldOwnerId()))
                .and(PARK_OWNER_PROPERTY.RELATION_STATUS.eq(1));
        List<ParkOwnerProperty> oldOwnerProperties = parkOwnerPropertyMapper.selectListByQuery(oldOwnerQuery);
        for (ParkOwnerProperty op : oldOwnerProperties) {
            op.setRelationStatus(0);
            op.setUnbindTime(LocalDateTime.now());
            op.setUnbindOperatorId(transfer.getAuditorId());
            op.setUnbindOperatorName(transfer.getAuditorName());
            op.setUnbindReason("房产转让: " + transfer.getTransferNo());
            op.setUpdateTime(LocalDateTime.now());
            parkOwnerPropertyMapper.update(op);
        }

        ParkOwner newOwner = parkOwnerMapper.selectOneById(transfer.getNewOwnerId());

        ParkOwnerProperty newOwnerProperty = new ParkOwnerProperty();
        newOwnerProperty.setOwnerId(transfer.getNewOwnerId());
        newOwnerProperty.setOwnerCode(newOwner != null ? newOwner.getOwnerCode() : null);
        newOwnerProperty.setPropertyId(transfer.getPropertyId());
        newOwnerProperty.setPropertyCode(transfer.getPropertyCode());
        int acquireType;
        if (transfer.getTransferType() == 1) {
            acquireType = 1;
        } else if (transfer.getTransferType() == 2) {
            acquireType = 3;
        } else {
            acquireType = 2;
        }
        newOwnerProperty.setAcquireType(acquireType);
        newOwnerProperty.setAcquireDate(transfer.getTransferDate());
        newOwnerProperty.setPropertyCertNo(transfer.getNewPropertyCertNo());
        newOwnerProperty.setRelationStatus(1);
        newOwnerProperty.setCreateTime(LocalDateTime.now());
        newOwnerProperty.setUpdateTime(LocalDateTime.now());
        parkOwnerPropertyMapper.insert(newOwnerProperty);

        ParkProperty property = new ParkProperty();
        property.setId(transfer.getPropertyId());
        property.setOwnerId(transfer.getNewOwnerId());
        if (transfer.getNewPropertyCertNo() != null && !transfer.getNewPropertyCertNo().isEmpty()) {
            property.setPropertyCertNo(transfer.getNewPropertyCertNo());
        }
        property.setUpdateTime(LocalDateTime.now());
        parkPropertyMapper.update(property);

        ParkPropertyStatusLog statusLog = new ParkPropertyStatusLog();
        statusLog.setPropertyId(transfer.getPropertyId());
        statusLog.setPropertyCode(transfer.getPropertyCode());
        statusLog.setChangeReason("房产转让: " + transfer.getTransferNo());
        statusLog.setOperatorId(transfer.getAuditorId());
        statusLog.setOperatorName(transfer.getAuditorName());
        statusLog.setCreateTime(LocalDateTime.now());
        parkPropertyStatusLogMapper.insert(statusLog);
    }

    public PageResult<ParkPropertyTransfer> getTransferList(Integer pageNum, Integer pageSize, Integer auditStatus, Long propertyId) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkPropertyTransfer.class);

        if (auditStatus != null) {
            query.and(PARK_PROPERTY_TRANSFER.AUDIT_STATUS.eq(auditStatus));
        }
        if (propertyId != null) {
            query.and(PARK_PROPERTY_TRANSFER.PROPERTY_ID.eq(propertyId));
        }

        query.orderBy(PARK_PROPERTY_TRANSFER.CREATE_TIME.desc());

        Page<ParkPropertyTransfer> page = parkPropertyTransferMapper.paginate(Page.of(pageNum, pageSize), query);

        List<ParkPropertyTransfer> list = page.getRecords();
        for (ParkPropertyTransfer transfer : list) {
            loadBuildingName(transfer);
        }

        return new PageResult<>(page.getTotalRow(), list,
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public List<ParkPropertyTransfer> getTransferHistory(Long propertyId) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkPropertyTransfer.class)
                .where(PARK_PROPERTY_TRANSFER.PROPERTY_ID.eq(propertyId))
                .orderBy(PARK_PROPERTY_TRANSFER.TRANSFER_DATE.desc());
        return parkPropertyTransferMapper.selectListByQuery(query);
    }

    public ParkPropertyTransfer getTransferById(Long id) {
        ParkPropertyTransfer transfer = parkPropertyTransferMapper.selectOneById(id);
        if (transfer != null) {
            loadBuildingName(transfer);
        }
        return transfer;
    }

    private void loadBuildingName(ParkPropertyTransfer transfer) {
        if (transfer.getPropertyId() != null) {
            ParkProperty property = parkPropertyMapper.selectOneById(transfer.getPropertyId());
            if (property != null && property.getBuildingId() != null) {
                ParkBuilding building = parkBuildingMapper.selectOneById(property.getBuildingId());
                if (building != null) {
                    transfer.setBuildingName(building.getBuildingName());
                }
            }
        }
    }
}
