package com.logistics.controller;

import com.logistics.auth.RequireRole;
import com.logistics.auth.Role;
import com.logistics.common.Result;
import com.logistics.service.CityService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理接口：重置演示数据。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final CityService cityService;

    public AdminController(CityService cityService) {
        this.cityService = cityService;
    }

    @PostMapping("/reset")
    @RequireRole(Role.ADMIN)
    public Result<Void> reset() {
        cityService.reset();
        return Result.ok();
    }
}
