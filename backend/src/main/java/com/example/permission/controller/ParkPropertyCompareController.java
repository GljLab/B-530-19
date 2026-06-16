package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.ParkProperty;
import com.example.permission.service.ParkPropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/park/property/compare")
public class ParkPropertyCompareController {

    @Autowired
    private ParkPropertyService parkPropertyService;

    @PostMapping
    @PreAuthorize("hasAuthority('park:property:compare')")
    public Result<List<ParkProperty>> compareProperties(@RequestBody List<Long> ids) {
        List<ParkProperty> result = parkPropertyService.compareProperties(ids);
        return Result.success(result);
    }
}
