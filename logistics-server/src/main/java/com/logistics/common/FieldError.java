package com.logistics.common;

/**
 * 字段级校验错误。
 *
 * <p>这是 record，不是普通类：编译后自动生成构造方法、
 * {@code field()} / {@code message()} 访问器、{@code equals/hashCode/toString}。
 * 空方法体是正常的，不是漏写。它只负责装一条错误，不负责收集；
 * 收集在 {@link GlobalExceptionHandler}。
 *
 * @param field   字段名
 * @param message 错误说明
 */
public record FieldError(String field, String message) {
}
