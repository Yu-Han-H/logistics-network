package com.logistics.exception;

/**
 * 城市 id 或名称重复。对应 HTTP 409。
 */
public class DuplicateCityException extends RuntimeException {

    public DuplicateCityException(String message) {
        super(message);
    }
}
