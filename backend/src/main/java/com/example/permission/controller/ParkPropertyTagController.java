package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.ParkProperty;
import com.example.permission.entity.ParkPropertyTag;
import com.example.permission.security.LoginUser;
import com.example.permission.service.ParkPropertyTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/park/property/tag")
public class ParkPropertyTagController {

    @Autowired
    private ParkPropertyTagService parkPropertyTagService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('park:tag:list')")
    public Result<List<ParkPropertyTag>> tagList() {
        List<ParkPropertyTag> result = parkPropertyTagService.listTags();
        return Result.success(result);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('park:tag:query')")
    public Result<List<ParkPropertyTag>> allTags() {
        List<ParkPropertyTag> result = parkPropertyTagService.getAllTags();
        return Result.success(result);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('park:tag:add')")
    public Result<Void> addTag(@RequestBody ParkPropertyTag tag) {
        parkPropertyTagService.addTag(tag);
        return Result.success(null);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('park:tag:edit')")
    public Result<Void> updateTag(@RequestBody ParkPropertyTag tag) {
        parkPropertyTagService.updateTag(tag);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('park:tag:delete')")
    public Result<Void> deleteTag(@PathVariable Long id) {
        parkPropertyTagService.deleteTag(id);
        return Result.success(null);
    }

    @PostMapping("/property")
    @PreAuthorize("hasAuthority('park:property:setTag')")
    public Result<Void> setPropertyTags(@RequestBody Map<String, Object> params) {
        Long propertyId = Long.valueOf(params.get("propertyId").toString());
        @SuppressWarnings("unchecked")
        List<Long> tagIds = (List<Long>) params.get("tagIds");
        parkPropertyTagService.setPropertyTags(propertyId, tagIds);
        return Result.success(null);
    }

    @GetMapping("/property/{propertyId}")
    @PreAuthorize("hasAuthority('park:property:query')")
    public Result<List<ParkPropertyTag>> propertyTags(@PathVariable Long propertyId) {
        List<ParkPropertyTag> result = parkPropertyTagService.getPropertyTags(propertyId);
        return Result.success(result);
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAuthority('park:property:list')")
    public Result<PageResult<ParkProperty>> filterByTags(
            @RequestParam String tagIds,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        List<Long> tagIdList = Arrays.stream(tagIds.split(","))
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList());
        PageResult<ParkProperty> result = parkPropertyTagService.getPropertiesByTagIds(tagIdList, pageNum, pageSize);
        return Result.success(result);
    }
}
