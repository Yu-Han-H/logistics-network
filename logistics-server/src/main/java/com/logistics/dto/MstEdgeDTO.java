package com.logistics.dto;

/**
 * MST 的一条边。对外用 fromId/toId，而不是内部 Edge 的 startId/endId。
 */
public record MstEdgeDTO(
    int fromId,
    String fromName,
    int toId,
    String toName,
    double weight
) {
}
