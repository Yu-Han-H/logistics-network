package com.logistics.dto;

import java.util.List;

/**
 * 邻接表的一行：一个城市和它的所有邻居。
 */
public record AdjacencyDTO(int cityId, String cityName, List<NeighborDTO> neighbors) {
}
