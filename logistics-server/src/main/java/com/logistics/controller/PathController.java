package com.logistics.controller;

import com.logistics.common.Result;
import com.logistics.dto.PathDTO;
import com.logistics.service.PathService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 最短路径接口。
 */
@RestController
@RequestMapping("/api/paths")
public class PathController {

    private final PathService pathService;

    public PathController(PathService pathService) {
        this.pathService = pathService;
    }

    @GetMapping("/shortest")
    public Result<PathDTO> shortest(
        @RequestParam int from,
        @RequestParam int to,
        @RequestParam(defaultValue = "graph") String mode
    ) {
        return Result.ok(pathService.shortest(from, to, mode));
    }
}
