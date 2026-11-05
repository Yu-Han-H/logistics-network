package com.logistics.exception;

/**
 * 城市不存在。对应 HTTP 404。
 */
public class CityNotFoundException extends RuntimeException {

    public CityNotFoundException(String message) {
        super(message);
    }

    public CityNotFoundException(int cityId) {
        super("城市不存在：" + cityId);
    }
}
