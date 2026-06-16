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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.permission.entity.table.ParkPropertyBatchLogTableDef.PARK_PROPERTY_BATCH_LOG;
import static com.example.permission.entity.table.ParkPropertyBatchItemTableDef.PARK_PROPERTY_BATCH_ITEM;
import static com.example.permission.entity.table.ParkPropertyTableDef.PARK_PROPERTY;
import static com.example.permission.entity.table.ParkOwnerPropertyTableDef.PARK_OWNER_PROPERTY;

@Service
public class ParkPropertyBatchService {

    @Autowired
    private ParkPropertyBatchLogMapper parkPropertyBatchLogMapper;

    @Autowired
    private ParkPropertyBatchItemMapper parkPropertyBatchItemMapper;

    @Autowired
    private ParkPropertyMapper parkPropertyMapper;

    @Autowired
    private ParkPropertyStatusLogMapper parkPropertyStatusLogMapper;

    @Autowired
    private ParkOwnerPropertyMapper parkOwnerPropertyMapper;

    @Autowired
    private ParkBuildingMapper parkBuildingMapper;

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchChangeStatus(List<Long> ids, Integer targetStatus, String operateReason,
                                                  Long operatorId, String operatorName) {
        if (ids == null || ids.size() > 100) {
            throw new BusinessException("批量操作数量不能超过100条");
        }
        if (operateReason == null || operateReason.isBlank()) {
            throw new BusinessException("操作原因不能为空");
        }
        if (operateReason.length() > 500) {
            throw new BusinessException("操作原因不能超过500字");
        }

        String batchNo = "BATCH" + System.currentTimeMillis();

        ParkPropertyBatchLog batchLog = new ParkPropertyBatchLog();
        batchLog.setBatchNo(batchNo);
        batchLog.setBatchType(1);
        batchLog.setTargetStatus(targetStatus);
        batchLog.setOperateReason(operateReason);
        batchLog.setTotalCount(ids.size());
        batchLog.setSuccessCount(0);
        batchLog.setFailCount(0);
        batchLog.setSkipCount(0);
        batchLog.setOperatorId(operatorId);
        batchLog.setOperatorName(operatorName);
        batchLog.setCreateTime(LocalDateTime.now());
        parkPropertyBatchLogMapper.insert(batchLog);

        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;
        List<ParkPropertyBatchItem> items = new ArrayList<>();

        for (Long id : ids) {
            ParkProperty property = parkPropertyMapper.selectOneById(id);

            ParkPropertyBatchItem item = new ParkPropertyBatchItem();
            item.setBatchId(batchLog.getId());
            item.setBatchNo(batchNo);
            item.setPropertyId(id);
            item.setCreateTime(LocalDateTime.now());

            if (property == null || property.getDeleted() == 1) {
                item.setPropertyCode(property != null ? property.getPropertyCode() : "");
                item.setOldStatus(property != null ? property.getStatus() : null);
                item.setResult(2);
                item.setFailReason("房产不存在或已删除");
                parkPropertyBatchItemMapper.insert(item);
                items.add(item);
                failCount++;
                continue;
            }

            item.setPropertyCode(property.getPropertyCode());
            item.setOldStatus(property.getStatus());

            boolean hasActiveRental = false;
            if (property.getStatus() == 4) {
                QueryWrapper rentalQuery = QueryWrapper.create()
                        .from(ParkOwnerProperty.class)
                        .where(PARK_OWNER_PROPERTY.PROPERTY_ID.eq(property.getId()))
                        .and(PARK_OWNER_PROPERTY.RELATION_STATUS.eq(1));
                Long rentalCount = parkOwnerPropertyMapper.selectCountByQuery(rentalQuery);
                if (rentalCount != null && rentalCount > 0) {
                    hasActiveRental = true;
                }
            }

            if (hasActiveRental) {
                item.setNewStatus(property.getStatus());
                item.setResult(2);
                item.setFailReason("该房产已出租且有租约");
                parkPropertyBatchItemMapper.insert(item);
                items.add(item);
                failCount++;
                continue;
            }

            Integer oldStatus = property.getStatus();
            property.setStatus(targetStatus);
            property.setUpdateTime(LocalDateTime.now());

            if (targetStatus == 5) {
                property.setVacantSince(LocalDate.now());
                property.setIsLongTermVacant(0);
            } else if (oldStatus == 5) {
                property.setVacantSince(null);
                property.setVacancyReason(null);
                property.setIsLongTermVacant(null);
            }

            parkPropertyMapper.update(property);

            ParkPropertyStatusLog statusLog = new ParkPropertyStatusLog();
            statusLog.setPropertyId(property.getId());
            statusLog.setPropertyCode(property.getPropertyCode());
            statusLog.setOldStatus(oldStatus);
            statusLog.setNewStatus(targetStatus);
            statusLog.setChangeReason(operateReason);
            statusLog.setOperatorId(operatorId);
            statusLog.setOperatorName(operatorName);
            statusLog.setCreateTime(LocalDateTime.now());
            parkPropertyStatusLogMapper.insert(statusLog);

            item.setNewStatus(targetStatus);
            item.setResult(1);
            parkPropertyBatchItemMapper.insert(item);
            items.add(item);
            successCount++;
        }

        batchLog.setSuccessCount(successCount);
        batchLog.setFailCount(failCount);
        batchLog.setSkipCount(skipCount);
        parkPropertyBatchLogMapper.update(batchLog);

        Map<String, Object> result = new HashMap<>();
        result.put("batchNo", batchNo);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("items", items);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchChangeAttribute(List<Long> ids, String targetDecorationStatus, String targetSpecialTags,
                                                     String operateReason, Long operatorId, String operatorName) {
        if (ids == null || ids.size() > 100) {
            throw new BusinessException("批量操作数量不能超过100条");
        }
        if (operateReason == null || operateReason.isBlank()) {
            throw new BusinessException("操作原因不能为空");
        }
        if (operateReason.length() > 500) {
            throw new BusinessException("操作原因不能超过500字");
        }

        String batchNo = "BATCH" + System.currentTimeMillis();

        ParkPropertyBatchLog batchLog = new ParkPropertyBatchLog();
        batchLog.setBatchNo(batchNo);
        batchLog.setBatchType(2);
        batchLog.setTargetDecorationStatus(targetDecorationStatus);
        batchLog.setTargetSpecialTags(targetSpecialTags);
        batchLog.setOperateReason(operateReason);
        batchLog.setTotalCount(ids.size());
        batchLog.setSuccessCount(0);
        batchLog.setFailCount(0);
        batchLog.setSkipCount(0);
        batchLog.setOperatorId(operatorId);
        batchLog.setOperatorName(operatorName);
        batchLog.setCreateTime(LocalDateTime.now());
        parkPropertyBatchLogMapper.insert(batchLog);

        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;
        List<ParkPropertyBatchItem> items = new ArrayList<>();

        for (Long id : ids) {
            ParkProperty property = parkPropertyMapper.selectOneById(id);

            ParkPropertyBatchItem item = new ParkPropertyBatchItem();
            item.setBatchId(batchLog.getId());
            item.setBatchNo(batchNo);
            item.setPropertyId(id);
            item.setCreateTime(LocalDateTime.now());

            if (property == null || property.getDeleted() == 1) {
                item.setPropertyCode(property != null ? property.getPropertyCode() : "");
                item.setOldDecorationStatus(property != null ? property.getDecorationStatus() : null);
                item.setOldSpecialTags(property != null ? property.getSpecialTags() : null);
                item.setResult(2);
                item.setFailReason("房产不存在或已删除");
                parkPropertyBatchItemMapper.insert(item);
                items.add(item);
                failCount++;
                continue;
            }

            item.setPropertyCode(property.getPropertyCode());
            item.setOldDecorationStatus(property.getDecorationStatus());
            item.setOldSpecialTags(property.getSpecialTags());

            if (targetDecorationStatus != null) {
                property.setDecorationStatus(targetDecorationStatus);
            }
            if (targetSpecialTags != null) {
                property.setSpecialTags(targetSpecialTags);
            }
            property.setUpdateTime(LocalDateTime.now());
            parkPropertyMapper.update(property);

            item.setNewDecorationStatus(property.getDecorationStatus());
            item.setNewSpecialTags(property.getSpecialTags());
            item.setResult(1);
            parkPropertyBatchItemMapper.insert(item);
            items.add(item);
            successCount++;
        }

        batchLog.setSuccessCount(successCount);
        batchLog.setFailCount(failCount);
        batchLog.setSkipCount(skipCount);
        parkPropertyBatchLogMapper.update(batchLog);

        Map<String, Object> result = new HashMap<>();
        result.put("batchNo", batchNo);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("items", items);
        return result;
    }

    public PageResult<ParkPropertyBatchLog> getBatchLogList(Integer pageNum, Integer pageSize, String batchNo) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkPropertyBatchLog.class);

        if (batchNo != null && !batchNo.isEmpty()) {
            query.and(PARK_PROPERTY_BATCH_LOG.BATCH_NO.like(batchNo));
        }

        query.orderBy(PARK_PROPERTY_BATCH_LOG.CREATE_TIME.desc());

        Page<ParkPropertyBatchLog> page = parkPropertyBatchLogMapper.paginate(Page.of(pageNum, pageSize), query);

        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public ParkPropertyBatchLog getBatchDetail(Long batchId) {
        ParkPropertyBatchLog batchLog = parkPropertyBatchLogMapper.selectOneById(batchId);
        if (batchLog == null) {
            return null;
        }

        QueryWrapper itemQuery = QueryWrapper.create()
                .from(ParkPropertyBatchItem.class)
                .where(PARK_PROPERTY_BATCH_ITEM.BATCH_ID.eq(batchId))
                .orderBy(PARK_PROPERTY_BATCH_ITEM.CREATE_TIME.asc());
        List<ParkPropertyBatchItem> items = parkPropertyBatchItemMapper.selectListByQuery(itemQuery);

        for (ParkPropertyBatchItem item : items) {
            if (item.getPropertyId() != null) {
                ParkProperty property = parkPropertyMapper.selectOneById(item.getPropertyId());
                if (property != null && property.getBuildingId() != null) {
                    ParkBuilding building = parkBuildingMapper.selectOneById(property.getBuildingId());
                    if (building != null) {
                        item.setBuildingName(building.getBuildingName());
                    }
                }
            }
        }

        batchLog.setItems(items);
        return batchLog;
    }

    public List<ParkPropertyBatchItem> getBatchItemsByBatchNo(String batchNo) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkPropertyBatchItem.class)
                .where(PARK_PROPERTY_BATCH_ITEM.BATCH_NO.eq(batchNo))
                .orderBy(PARK_PROPERTY_BATCH_ITEM.CREATE_TIME.asc());
        return parkPropertyBatchItemMapper.selectListByQuery(query);
    }
}
