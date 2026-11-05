package com.logistics.dto;

/**
 * G6 渲染用的节点。id 转成字符串，避免前端类型不一致。
 */
public record GraphNodeDTO(String id, String name) {
}
