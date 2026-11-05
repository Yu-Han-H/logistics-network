package com.logistics.dto;

import java.util.List;

/**
 * 最小生成树对外结果。
 */
public record MstDTO(
    String algorithm,
    int nodeCount,
    int edgeCount,
    boolean connected,
    double totalWeight,
    List<MstEdgeDTO> edges,
    long elapsedNanos,
    int graphVersion
) {
}
