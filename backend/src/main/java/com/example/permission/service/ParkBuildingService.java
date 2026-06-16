package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.ParkBuilding;
import com.example.permission.entity.ParkBuildingImage;
import com.example.permission.entity.ParkFloor;
import com.example.permission.mapper.ParkBuildingImageMapper;
import com.example.permission.mapper.ParkBuildingMapper;
import com.example.permission.mapper.ParkFloorMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.permission.entity.table.ParkBuildingImageTableDef.PARK_BUILDING_IMAGE;
import static com.example.permission.entity.table.ParkBuildingTableDef.PARK_BUILDING;
import static com.example.permission.entity.table.ParkFloorTableDef.PARK_FLOOR;

/**
 * 楼宇服务
 */
@Service
public class ParkBuildingService {

    @Autowired
    private ParkBuildingMapper parkBuildingMapper;

    @Autowired
    private ParkBuildingImageMapper parkBuildingImageMapper;

    @Autowired
    private ParkFloorMapper parkFloorMapper;

    /**
     * 分页查询楼宇列表
     */
    public PageResult<ParkBuilding> pageList(Integer pageNum, Integer pageSize, Long parkId, String buildingName, Integer buildingType, Integer status) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkBuilding.class)
                .where(PARK_BUILDING.DELETED.eq(0));

        if (parkId != null) {
            query.and(PARK_BUILDING.PARK_ID.eq(parkId));
        }
        if (buildingName != null && !buildingName.isEmpty()) {
            query.and(PARK_BUILDING.BUILDING_NAME.like(buildingName).or(PARK_BUILDING.BUILDING_CODE.like(buildingName)));
        }
        if (buildingType != null) {
            query.and(PARK_BUILDING.BUILDING_TYPE.eq(buildingType));
        }
        if (status != null) {
            query.and(PARK_BUILDING.STATUS.eq(status));
        }
        query.orderBy(PARK_BUILDING.CREATE_TIME.desc());

        Page<ParkBuilding> page = parkBuildingMapper.paginate(Page.of(pageNum, pageSize), query);

        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    /**
     * 获取楼宇详情
     */
    public ParkBuilding getById(Long id) {
        return parkBuildingMapper.selectOneById(id);
    }

    /**
     * 获取楼宇详情（包含图片和楼层）
     */
    public ParkBuilding getDetail(Long id) {
        ParkBuilding building = parkBuildingMapper.selectOneById(id);
        if (building != null) {
            building.setImages(getImages(id));
            building.setFloors(listFloorsByBuildingId(id));
        }
        return building;
    }

    /**
     * 获取所有楼宇列表
     */
    public List<ParkBuilding> listAll(Long parkId) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkBuilding.class)
                .where(PARK_BUILDING.DELETED.eq(0));

        if (parkId != null) {
            query.and(PARK_BUILDING.PARK_ID.eq(parkId));
        }
        query.orderBy(PARK_BUILDING.BUILDING_CODE.asc());

        return parkBuildingMapper.selectListByQuery(query);
    }

    /**
     * 新增楼宇
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(ParkBuilding building) {
        QueryWrapper codeQuery = QueryWrapper.create()
                .from(ParkBuilding.class)
                .where(PARK_BUILDING.BUILDING_CODE.eq(building.getBuildingCode()))
                .and(PARK_BUILDING.DELETED.eq(0));
        Long codeCount = parkBuildingMapper.selectCountByQuery(codeQuery);
        if (codeCount != null && codeCount > 0) {
            throw new BusinessException("楼宇编号已存在");
        }

        building.setDeleted(0);
        building.setStatus(building.getStatus() != null ? building.getStatus() : 1);
        building.setCreateTime(LocalDateTime.now());
        building.setUpdateTime(LocalDateTime.now());
        parkBuildingMapper.insert(building);
    }

    /**
     * 更新楼宇
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(ParkBuilding building) {
        ParkBuilding existBuilding = parkBuildingMapper.selectOneById(building.getId());
        if (existBuilding == null) {
            throw new BusinessException("楼宇不存在");
        }

        if (building.getBuildingCode() != null &&
            !building.getBuildingCode().equals(existBuilding.getBuildingCode())) {
            QueryWrapper codeQuery = QueryWrapper.create()
                    .from(ParkBuilding.class)
                    .where(PARK_BUILDING.BUILDING_CODE.eq(building.getBuildingCode()))
                    .and(PARK_BUILDING.DELETED.eq(0));
            Long codeCount = parkBuildingMapper.selectCountByQuery(codeQuery);
            if (codeCount != null && codeCount > 0) {
                throw new BusinessException("楼宇编号已存在");
            }
        }

        building.setUpdateTime(LocalDateTime.now());
        parkBuildingMapper.update(building);
    }

    /**
     * 删除楼宇（删除前检查是否有楼层）
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ParkBuilding building = parkBuildingMapper.selectOneById(id);
        if (building == null) {
            throw new BusinessException("楼宇不存在");
        }

        Long floorCount = countFloorsByBuildingId(id);
        if (floorCount != null && floorCount > 0) {
            throw new BusinessException("该楼宇下存在 " + floorCount + " 个楼层，无法删除");
        }

        QueryWrapper imageQuery = QueryWrapper.create()
                .from(ParkBuildingImage.class)
                .where(PARK_BUILDING_IMAGE.BUILDING_ID.eq(id));
        parkBuildingImageMapper.deleteByQuery(imageQuery);

        building.setDeleted(1);
        building.setUpdateTime(LocalDateTime.now());
        parkBuildingMapper.update(building);
    }

    /**
     * 获取楼宇图片
     */
    public List<ParkBuildingImage> getImages(Long buildingId) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkBuildingImage.class)
                .where(PARK_BUILDING_IMAGE.BUILDING_ID.eq(buildingId))
                .orderBy(PARK_BUILDING_IMAGE.SORT_ORDER.asc());
        return parkBuildingImageMapper.selectListByQuery(query);
    }

    /**
     * 保存楼宇图片
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveImages(Long buildingId, List<ParkBuildingImage> images) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkBuildingImage.class)
                .where(PARK_BUILDING_IMAGE.BUILDING_ID.eq(buildingId));
        parkBuildingImageMapper.deleteByQuery(query);

        if (images != null && !images.isEmpty()) {
            for (ParkBuildingImage image : images) {
                image.setId(null);
                image.setBuildingId(buildingId);
                image.setCreateTime(LocalDateTime.now());
                parkBuildingImageMapper.insert(image);
            }
        }
    }

    /**
     * 统计园区楼宇数量
     */
    public Long countByParkId(Long parkId) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkBuilding.class)
                .where(PARK_BUILDING.PARK_ID.eq(parkId))
                .and(PARK_BUILDING.DELETED.eq(0));
        return parkBuildingMapper.selectCountByQuery(query);
    }

    private List<ParkFloor> listFloorsByBuildingId(Long buildingId) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkFloor.class)
                .where(PARK_FLOOR.BUILDING_ID.eq(buildingId))
                .and(PARK_FLOOR.DELETED.eq(0))
                .orderBy(PARK_FLOOR.SORT_ORDER.asc());
        return parkFloorMapper.selectListByQuery(query);
    }

    private Long countFloorsByBuildingId(Long buildingId) {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkFloor.class)
                .where(PARK_FLOOR.BUILDING_ID.eq(buildingId))
                .and(PARK_FLOOR.DELETED.eq(0));
        return parkFloorMapper.selectCountByQuery(query);
    }
}
