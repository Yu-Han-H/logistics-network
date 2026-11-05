package com.logistics.dto;

import java.util.List;

/**
 * 整张图：节点、边、规模。
 */
public record GraphDTO(
    List<GraphNodeDTO> nodes,
    List<GraphEdgeDTO> edges,
    int nodeCount,
    int edgeCount
) {
}
