package com.logistics.controller;

import com.logistics.auth.RequireRole;
import com.logistics.auth.Role;
import com.logistics.common.Result;
import com.logistics.dto.CityDTO;
import com.logistics.dto.CityRequest;
import com.logistics.dto.SortedCitiesDTO;
import com.logistics.service.CityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 城市接口。Controller 只做参数接收和 Result 包装，不写业务逻辑。
 */
@RestController
@RequestMapping("/api/cities")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping
    public Result<List<CityDTO>> list() {
        return Result.ok(cityService.list());
    }

    @GetMapping("/sorted")
    public Result<SortedCitiesDTO> sorted(@RequestParam(required = false) Integer reference) {
        return Result.ok(cityService.sorted(reference));
    }

    @GetMapping("/{id}")
    public Result<CityDTO> get(@PathVariable int id) {
        return Result.ok(cityService.get(id));
    }

    @PostMapping
    @RequireRole(Role.ADMIN)
    public Result<CityDTO> add(@Valid @RequestBody CityRequest request) {
        return Result.ok(cityService.add(request));
    }

    @PutMapping("/{id}")
    @RequireRole(Role.ADMIN)
    public Result<CityDTO> update(@PathVariable int id, @Valid @RequestBody CityRequest request) {
        return Result.ok(cityService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequireRole(Role.ADMIN)
    public Result<Void> delete(@PathVariable int id) {
        cityService.delete(id);
        return Result.ok();
    }
}
