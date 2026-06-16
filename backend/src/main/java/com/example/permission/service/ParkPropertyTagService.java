package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.ParkBuilding;
import com.example.permission.entity.ParkProperty;
import com.example.permission.entity.ParkPropertyTag;
import com.example.permission.entity.ParkPropertyTagRel;
import com.example.permission.mapper.ParkBuildingMapper;
import com.example.permission.mapper.ParkPropertyMapper;
import com.example.permission.mapper.ParkPropertyTagMapper;
import com.example.permission.mapper.ParkPropertyTagRelMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.ParkPropertyTagTableDef.PARK_PROPERTY_TAG;
import static com.example.permission.entity.table.ParkPropertyTagRelTableDef.PARK_PROPERTY_TAG_REL;
import static com.example.permission.entity.table.ParkPropertyTableDef.PARK_PROPERTY;

@Service
public class ParkPropertyTagService {

    @Autowired
    private ParkPropertyTagMapper parkPropertyTagMapper;

    @Autowired
    private ParkPropertyTagRelMapper parkPropertyTagRelMapper;

    @Autowired
    private ParkPropertyMapper parkPropertyMapper;

    @Autowired
    private ParkBuildingMapper parkBuildingMapper;

    public List<ParkPropertyTag> listTags() {
        QueryWrapper query = QueryWrapper.create()
                .from(ParkPropertyTag.class)
                .where(PARK_PROPERTY_TAG.DELETED.eq(0))
                .orderBy(PARK_PROPERTY_TAG.SORT_ORDER.asc(), PARK_PROPERTY_TAG.CREATE_TIME.asc());
        List<ParkPropertyTag> tags = parkPropertyTagMapper.selectListByQuery(query);
        for (ParkPropertyTag tag : tags) {
            Long count = parkPropertyTagRelMapper.selectCountByQuery(
                    QueryWrapper.create()
                            .from(ParkPropertyTagRel.class)
                            .where(PARK_PROPERTY_TAG_REL.TAG_ID.eq(tag.getId()))
            );
            tag.setPropertyCount(count != null ? count.intValue() : 0);
        }
        return tags;
    }

    public void addTag(ParkPropertyTag tag) {
        ParkPropertyTag existing = parkPropertyTagMapper.selectOneByQuery(
                QueryWrapper.create()
                        .from(ParkPropertyTag.class)
                        .where(PARK_PROPERTY_TAG.TAG_NAME.eq(tag.getTagName()))
                        .and(PARK_PROPERTY_TAG.DELETED.eq(0))
        );
        if (existing != null) {
            throw new BusinessException("标签名称已存在");
        }
        tag.setDeleted(0);
        tag.setCreateTime(LocalDateTime.now());
        tag.setUpdateTime(LocalDateTime.now());
        parkPropertyTagMapper.insert(tag);
    }

    public void updateTag(ParkPropertyTag tag) {
        ParkPropertyTag existing = parkPropertyTagMapper.selectOneByQuery(
                QueryWrapper.create()
                        .from(ParkPropertyTag.class)
                        .where(PARK_PROPERTY_TAG.TAG_NAME.eq(tag.getTagName()))
                        .and(PARK_PROPERTY_TAG.ID.ne(tag.getId()))
                        .and(PARK_PROPERTY_TAG.DELETED.eq(0))
        );
        if (existing != null) {
            throw new BusinessException("标签名称已存在");
        }
        tag.setUpdateTime(LocalDateTime.now());
        parkPropertyTagMapper.update(tag);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        ParkPropertyTag tag = parkPropertyTagMapper.selectOneById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        tag.setDeleted(1);
        tag.setUpdateTime(LocalDateTime.now());
        parkPropertyTagMapper.update(tag);

        List<ParkPropertyTagRel> rels = parkPropertyTagRelMapper.selectListByQuery(
                QueryWrapper.create()
                        .from(ParkPropertyTagRel.class)
                        .where(PARK_PROPERTY_TAG_REL.TAG_ID.eq(id))
        );
        for (ParkPropertyTagRel rel : rels) {
            parkPropertyTagRelMapper.deleteById(rel.getId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void setPropertyTags(Long propertyId, List<Long> tagIds) {
        List<ParkPropertyTagRel> existingRels = parkPropertyTagRelMapper.selectListByQuery(
                QueryWrapper.create()
                        .from(ParkPropertyTagRel.class)
                        .where(PARK_PROPERTY_TAG_REL.PROPERTY_ID.eq(propertyId))
        );
        for (ParkPropertyTagRel rel : existingRels) {
            parkPropertyTagRelMapper.deleteById(rel.getId());
        }
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                ParkPropertyTagRel rel = new ParkPropertyTagRel();
                rel.setPropertyId(propertyId);
                rel.setTagId(tagId);
                rel.setCreateTime(LocalDateTime.now());
                parkPropertyTagRelMapper.insert(rel);
            }
        }
    }

    public List<ParkPropertyTag> getPropertyTags(Long propertyId) {
        List<ParkPropertyTagRel> rels = parkPropertyTagRelMapper.selectListByQuery(
                QueryWrapper.create()
                        .from(ParkPropertyTagRel.class)
                        .where(PARK_PROPERTY_TAG_REL.PROPERTY_ID.eq(propertyId))
        );
        List<ParkPropertyTag> tags = new ArrayList<>();
        for (ParkPropertyTagRel rel : rels) {
            ParkPropertyTag tag = parkPropertyTagMapper.selectOneById(rel.getTagId());
            if (tag != null && tag.getDeleted() != null && tag.getDeleted() == 0) {
                tags.add(tag);
            }
        }
        return tags;
    }

    public PageResult<ParkProperty> getPropertiesByTagIds(List<Long> tagIds, Integer pageNum, Integer pageSize) {
        List<ParkPropertyTagRel> rels = parkPropertyTagRelMapper.selectListByQuery(
                QueryWrapper.create()
                        .from(ParkPropertyTagRel.class)
                        .where(PARK_PROPERTY_TAG_REL.TAG_ID.in(tagIds))
        );
        List<Long> propertyIds = rels.stream()
                .map(ParkPropertyTagRel::getPropertyId)
                .distinct()
                .collect(Collectors.toList());

        if (propertyIds.isEmpty()) {
            return new PageResult<>(0L, new ArrayList<>(), (long) pageNum, (long) pageSize);
        }

        List<ParkProperty> allProperties = parkPropertyMapper.selectListByQuery(
                QueryWrapper.create()
                        .from(ParkProperty.class)
                        .where(PARK_PROPERTY.ID.in(propertyIds))
                        .and(PARK_PROPERTY.DELETED.eq(0))
        );

        for (ParkProperty property : allProperties) {
            if (property.getBuildingId() != null) {
                ParkBuilding building = parkBuildingMapper.selectOneById(property.getBuildingId());
                if (building != null) {
                    property.setBuildingName(building.getBuildingName());
                }
            }
            property.setTagList(getPropertyTags(property.getId()));
        }

        int total = allProperties.size();
        int offset = (pageNum - 1) * pageSize;
        int endIndex = Math.min(offset + pageSize, total);
        List<ParkProperty> pageList;
        if (offset >= total) {
            pageList = new ArrayList<>();
        } else {
            pageList = allProperties.subList(offset, endIndex);
        }

        return new PageResult<>((long) total, pageList, (long) pageNum, (long) pageSize);
    }

    public List<ParkPropertyTag> getAllTags() {
        return parkPropertyTagMapper.selectListByQuery(
                QueryWrapper.create()
                        .from(ParkPropertyTag.class)
                        .where(PARK_PROPERTY_TAG.DELETED.eq(0))
        );
    }
}
