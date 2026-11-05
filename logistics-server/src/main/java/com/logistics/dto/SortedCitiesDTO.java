package com.logistics.dto;

import java.util.List;

/**
 * 按距离排序后的完整链表视图。
 */
public record SortedCitiesDTO(
    CityRefDTO reference,
    String order,
    int headId,
    List<SortedCityDTO> cities
) {
}
