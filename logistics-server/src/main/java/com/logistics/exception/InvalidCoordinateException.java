package com.logistics.exception;

/**
 * 经纬度非法。对应 HTTP 400。
 */
public class InvalidCoordinateException extends RuntimeException {

    public InvalidCoordinateException(String message) {
        super(message);
    }
}
