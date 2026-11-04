package com.logistics.common;

/**
 * 字段级校验错误。
 *
 * @param field   字段名
 * @param message 错误说明
 */
public record FieldError(String field, String message) {
}
