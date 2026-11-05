package com.logistics.dto;

/**
 * 登录成功后的返回。
 */
public record LoginResponse(String token, UserDTO user) {
}
