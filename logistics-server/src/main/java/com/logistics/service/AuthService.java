package com.logistics.service;

import com.logistics.auth.Role;
import com.logistics.dto.LoginRequest;
import com.logistics.dto.LoginResponse;
import com.logistics.dto.UserDTO;
import com.logistics.exception.UnauthorizedException;
import com.logistics.model.User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录与 token。
 *
 * <p>课程演示用：账号和 token 都放内存，不建数据库，密码明文。
 * 应用重启后账号恢复初始状态，已登录 token 全部失效。
 *
 * <p>职责：
 * <ul>
 *   <li>预置 admin/admin123（ADMIN）、user/user123（USER）；</li>
 *   <li>登录成功生成 UUID token，存 Map&lt;String, User&gt;；</li>
 *   <li>me/logout 解析 Authorization: Bearer &lt;token&gt;；</li>
 *   <li>token 无效抛 UnauthorizedException。</li>
 * </ul>
 */
@Service
public class AuthService {

    // Authorization 头的前缀，不区分大小写
    private static final String BEARER_PREFIX = "Bearer ";

    // 预置账号：用户名 -> 用户。只读，运行时不增删用户。
    private final Map<String, User> usersByUsername = Map.of(
        "admin", new User(1, "admin", "admin123", Role.ADMIN),
        "user", new User(2, "user", "user123", Role.USER)
    );

    //已登录 token：token -> 用户。ConcurrentHashMap 支持并发请求。
    private final Map<String, User> tokens = new ConcurrentHashMap<>();

    /**
     * 登录
     *
     * @param request 用户名和密码
     * @return token 和当前用户信息
     * @throws UnauthorizedException 用户名或密码错误时抛出
     */
    public LoginResponse login(LoginRequest request) {
        if (request == null || request.username() == null || request.password() == null) {
            throw new UnauthorizedException("用户名或密码错误");
        }

        User user = usersByUsername.get(request.username());
        if (user == null || !user.getPassword().equals(request.password())) {
            // 不区分“用户不存在”和“密码错误”，避免暴露账号是否存在
            throw new UnauthorizedException("用户名或密码错误");
        }

        String token = UUID.randomUUID().toString();
        tokens.put(token, user);
        return new LoginResponse(token, UserDTO.from(user));
    }

    /**
     * 查询当前登录用户
     *
     * @param authorization Authorization: Bearer &lt;token&gt;
     * @return 当前用户，不包含密码
     * @throws UnauthorizedException token 缺失、格式错误或无效时抛出
     */
    public UserDTO me(String authorization) {
        return UserDTO.from(requireUser(authorization));
    }

    /**
     * 退出登录，删除当前 token
     *
     * @param authorization Authorization: Bearer &lt;token&gt;
     * @throws UnauthorizedException token 缺失、格式错误或无效时抛出
     */
    public void logout(String authorization) {
        String token = extractToken(authorization);
        if (tokens.remove(token) == null) {
            throw new UnauthorizedException("未登录或登录已失效");
        }
    }

    /**
     * 供拦截器使用：校验 token 并返回当前用户
     *
     * @param authorization Authorization: Bearer &lt;token&gt;
     * @return 当前用户
     * @throws UnauthorizedException token 缺失、格式错误或无效时抛出
     */
    public User requireUser(String authorization) {
        String token = extractToken(authorization);
        User user = tokens.get(token);
        if (user == null) {
            throw new UnauthorizedException("未登录或登录已失效");
        }
        return user;
    }

    /**
     * 从 Authorization 头中取出 token
     *
     * <p>只接受 {@code Bearer <token>} 格式，Bearer 大小写不敏感，
     * 和 HTTP 认证方案的要求一致。
     *
     * @param authorization Authorization 头
     * @return 去掉 Bearer 前缀后的 token
     * @throws UnauthorizedException 头缺失、前缀不对或 token 为空时抛出
     */
    private String extractToken(String authorization) {
        if (authorization == null) {
            throw new UnauthorizedException("未登录或登录已失效");
        }

        String header = authorization.trim();
        if (!header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            throw new UnauthorizedException("未登录或登录已失效");
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new UnauthorizedException("未登录或登录已失效");
        }
        return token;
    }
}
