package com.logistics.service;

import com.logistics.dto.LoginRequest;
import com.logistics.dto.LoginResponse;
import com.logistics.dto.UserDTO;
import org.springframework.stereotype.Service;

/**
 * 登录与 token。
 *
 * <p>TODO 黄予函实现：
 * <ul>
 *   <li>预置 admin/admin123（ADMIN）、user/user123（USER）；</li>
 *   <li>登录成功生成 UUID token，存 Map&lt;String, User&gt;；</li>
 *   <li>me/logout 解析 Authorization: Bearer &lt;token&gt;；</li>
 *   <li>token 无效抛 UnauthorizedException。</li>
 * </ul>
 */
@Service
public class AuthService {

    public LoginResponse login(LoginRequest request) {
        throw todo("黄予函");
    }

    public UserDTO me(String authorization) {
        throw todo("黄予函");
    }

    public void logout(String authorization) {
        throw todo("黄予函");
    }

    private UnsupportedOperationException todo(String owner) {
        return new UnsupportedOperationException("TODO(" + owner + "): 待实现");
    }
}
