package com.logistics.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 统一响应体。
 *
 * <p>这是 record：{@code code()}、{@code message()}、{@code data()}、{@code errors()}
 * 是编译器自动生成的访问器；{@code ok/fail} 是手写的静态工厂方法。
 *
 * <p>字段输出规则（对应 api.md 第 2、8 节）：
 * <ul>
 *   <li>{@code data} 永远输出，失败时为 {@code null}；</li>
 *   <li>{@code errors} 只在参数校验失败时输出，其余情况不出现。</li>
 * </ul>
 *
 * @param code    业务状态码，和 HTTP 状态码一致
 * @param message 提示信息
 * @param data    业务数据，失败时为 null
 * @param errors  字段校验错误，仅参数错误时出现
 */
public record Result<T>(int code, String message, T data, List<FieldError> errors) {

    /** data 始终参与序列化：失败时要能返回 {@code "data": null}。 */
    @Override
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public T data() {
        return data;
    }

    /** errors 为 null 时不输出，避免成功响应里出现 {@code "errors": null}。 */
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<FieldError> errors() {
        return errors;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data, null);
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(ResultCode code, String message) {
        return new Result<>(code.getCode(), message, null, null);
    }

    public static <T> Result<T> fail(ResultCode code, String message, List<FieldError> errors) {
        return new Result<>(code.getCode(), message, null, errors);
    }
}
