package com.logistics.exception;

/**
 * 未登录或 token 无效。对应 HTTP 401。
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
