package com.logistics.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录与角色拦截器。
 *
 * <p>当前是骨架：直接放行。由黄予函补完：
 * <ol>
 *   <li>从 Authorization: Bearer &lt;token&gt; 取 token；</li>
 *   <li>token 无效或缺失时抛 UnauthorizedException；</li>
 *   <li>读取 HandlerMethod 上的 {@link RequireRole}，角色不符时抛 ForbiddenException；</li>
 *   <li>把当前用户放到 request attribute，供 Controller 使用。</li>
 * </ol>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // TODO 黄予函：实现 token 校验和 @RequireRole 检查。
        // 现在直接放行，保证前端可以先联调，不会因为没登录全部 401。
        return true;
    }
}
