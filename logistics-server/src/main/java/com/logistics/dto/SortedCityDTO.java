package com.logistics.dto;

/**
 * 排序链表里的一个城市节点。prevId/nextId 用来画双向循环链表。
 */
public record SortedCityDTO(
    int id,
    String name,
    double distance,
    int prevId,
    int nextId
) {
}
