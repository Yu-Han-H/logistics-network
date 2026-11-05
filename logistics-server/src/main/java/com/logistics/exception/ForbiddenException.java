package com.logistics.exception;

/**
 * 已登录但角色不够。对应 HTTP 403。
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
