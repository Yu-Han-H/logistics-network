package com.logistics.common;

import java.util.List;

/**
 * 统一响应体。
 *
 * <p>这是 record：{@code code()}、{@code message()}、{@code data()}、{@code errors()}
 * 是编译器自动生成的访问器；{@code ok/fail} 是手写的静态工厂方法。
 *
 * @param code    业务状态码，和 HTTP 状态码一致
 * @param message 提示信息
 * @param data    业务数据，失败时为 null
 * @param errors  字段校验错误，仅参数错误时出现
 */
public record Result<T>(int code, String message, T data, List<FieldError> errors) {

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
