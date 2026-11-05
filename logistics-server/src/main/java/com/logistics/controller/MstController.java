package com.logistics.controller;

import com.logistics.common.Result;
import com.logistics.dto.MstDTO;
import com.logistics.service.MstService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 最小生成树接口。
 */
@RestController
@RequestMapping("/api/mst")
public class MstController {

    private final MstService mstService;

    public MstController(MstService mstService) {
        this.mstService = mstService;
    }

    @GetMapping
    public Result<MstDTO> mst(@RequestParam(defaultValue = "prim") String algorithm) {
        return Result.ok(mstService.compute(algorithm));
    }
}
