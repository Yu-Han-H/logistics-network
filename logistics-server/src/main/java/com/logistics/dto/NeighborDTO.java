package com.logistics.dto;

/**
 * 邻接表里的一条邻居记录。
 */
public record NeighborDTO(int toId, String toName, double weight) {
}
