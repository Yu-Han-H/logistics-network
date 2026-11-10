package com.logistics.dto;

/**
 * G6 渲染用的节点。id 转成字符串，避免前端类型不一致。
 *
 * <p>额外带上经纬度，前端按等距投影固定节点位置，
 * 保证图上的相对位置和真实地理位置一致，而不是力导布局的随机位置。
 */
public record GraphNodeDTO(
    String id,
    String name,
    double longitude,
    double latitude
) {
}
