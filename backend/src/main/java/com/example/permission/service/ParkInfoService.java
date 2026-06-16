package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.ParkFacility;
import com.example.permission.entity.ParkImage;
import com.example.permission.entity.ParkInfo;
import com.example.permission.mapper.ParkFacilityMapper;
import com.example.permission.mapper.ParkImageMapper;
import com.example.permission.mapper.ParkInfoMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.permission.entity.table.ParkFacilityTableDef.PARK_FACILITY;
import static com.example.permission.entity.table.ParkImageTableDef.PARK_IMAGE;
import static com.example.permission.entity.table.ParkInfoTableDef.PARK_INFO;

/**
 * 园区信息服务
 */
@Service
public class ParkInfoService {

    @Autowired
    private ParkInfoMapper parkInfoMapper;

    @Autowired
    private ParkFacilityMapper parkFacilityMapper;

    @Autowired
    private ParkImageMapper parkImageMapper;

    /**
     * 获取园区信息（取第一条，全局唯一）
     */
    public ParkInfo getParkInfo() {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkInfo.class)
                .where(PARK_INFO.DELETED.eq(0))
                .orderBy(PARK_INFO.ID.asc())
                .limit(1);
        return parkInfoMapper.selectOneByQuery(query);
    }

    /**
     * 获取园区详情（包含设施和图片）
     */
    public ParkInfo getParkDetail(Long id) {
        ParkInfo parkInfo = parkInfoMapper.selectOneById(id);
        if (parkInfo != null) {
            parkInfo.setFacilities(getFacilities(id));
            parkInfo.setImages(getImages(id));
        }
        return parkInfo;
    }

    /**
     * 保存或更新园区信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(ParkInfo parkInfo) {
        if (parkInfo.getId() == null) {
            parkInfo.setDeleted(0);
            parkInfo.setCreateTime(LocalDateTime.now());
            parkInfo.setUpdateTime(LocalDateTime.now());
            parkInfoMapper.insert(parkInfo);
        } else {
            ParkInfo existPark = parkInfoMapper.selectOneById(parkInfo.getId());
            if (existPark == null) {
                throw new BusinessException("园区信息不存在");
            }
            parkInfo.setUpdateTime(LocalDateTime.now());
            parkInfoMapper.update(parkInfo);
        }
    }

    /**
     * 获取园区配套设施列表
     */
    public List<ParkFacility> getFacilities(Long parkId) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkFacility.class)
                .where(PARK_FACILITY.PARK_ID.eq(parkId))
                .and(PARK_FACILITY.DELETED.eq(0))
                .orderBy(PARK_FACILITY.SORT_ORDER.asc());
        return parkFacilityMapper.selectListByQuery(query);
    }

    /**
     * 保存配套设施（先删后插）
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveFacilities(Long parkId, List<ParkFacility> facilities) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkFacility.class)
                .where(PARK_FACILITY.PARK_ID.eq(parkId));
        List<ParkFacility> existFacilities = parkFacilityMapper.selectListByQuery(query);
        for (ParkFacility facility : existFacilities) {
            facility.setDeleted(1);
            facility.setUpdateTime(LocalDateTime.now());
            parkFacilityMapper.update(facility);
        }

        if (facilities != null && !facilities.isEmpty()) {
            for (ParkFacility facility : facilities) {
                facility.setId(null);
                facility.setParkId(parkId);
                facility.setDeleted(0);
                facility.setCreateTime(LocalDateTime.now());
                facility.setUpdateTime(LocalDateTime.now());
                parkFacilityMapper.insert(facility);
            }
        }
    }

    /**
     * 获取园区图片列表
     */
    public List<ParkImage> getImages(Long parkId) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkImage.class)
                .where(PARK_IMAGE.PARK_ID.eq(parkId))
                .orderBy(PARK_IMAGE.SORT_ORDER.asc());
        return parkImageMapper.selectListByQuery(query);
    }

    /**
     * 保存园区图片（先删后插）
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveImages(Long parkId, List<ParkImage> images) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkImage.class)
                .where(PARK_IMAGE.PARK_ID.eq(parkId));
        parkImageMapper.deleteByQuery(query);

        if (images != null && !images.isEmpty()) {
            for (ParkImage image : images) {
                image.setId(null);
                image.setParkId(parkId);
                image.setCreateTime(LocalDateTime.now());
                parkImageMapper.insert(image);
            }
        }
    }
}
