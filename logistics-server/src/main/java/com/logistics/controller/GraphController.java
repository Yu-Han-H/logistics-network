package com.logistics.controller;

import com.logistics.common.Result;
import com.logistics.dto.AdjacencyDTO;
import com.logistics.dto.GraphDTO;
import com.logistics.service.GraphService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 图查询接口。
 */
@RestController
@RequestMapping("/api/graph")
public class GraphController {

    private final GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping
    public Result<GraphDTO> graph() {
        return Result.ok(graphService.graph());
    }

    @GetMapping("/adjacency")
    public Result<List<AdjacencyDTO>> adjacency() {
        return Result.ok(graphService.adjacency());
    }
}
