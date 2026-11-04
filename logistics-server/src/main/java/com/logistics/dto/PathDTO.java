package com.logistics.dto;

import java.util.List;

/**
 * 最短路径对外结果。不可达时 found=false、cost=null、path 为空。
 */
public record PathDTO(
    String mode,
    boolean found,
    int fromId,
    String fromName,
    int toId,
    String toName,
    Double cost,
    int hops,
    List<PathNodeDTO> path,
    long elapsedNanos,
    int graphVersion
) {
}
