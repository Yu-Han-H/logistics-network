package com.logistics.dto;

import com.logistics.model.User;

/**
 * 对外用户对象。不返回密码。
 */
public record UserDTO(int id, String username, String role) {

    public static UserDTO from(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getRole().name());
    }
}
