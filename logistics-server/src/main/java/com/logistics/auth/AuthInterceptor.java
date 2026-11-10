package com.logistics.auth;

import com.logistics.exception.ForbiddenException;
import com.logistics.model.User;
import com.logistics.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录与角色拦截器。
 *
 * <p>处理顺序：
 * <ol>
 *   <li>放行 OPTIONS 预检请求，避免前端 CORS 预检被 401；</li>
 *   <li>只处理 HandlerMethod，静态资源等直接放行；</li>
 *   <li>从 Authorization: Bearer &lt;token&gt; 校验登录，失败抛 UnauthorizedException；</li>
 *   <li>把当前用户放到 request attribute，供 Controller 使用；</li>
 *   <li>读取方法或类上的 {@link RequireRole}，角色不符抛 ForbiddenException。</li>
 * </ol>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    // 当前登录用户放在 request 里的属性名
    public static final String CURRENT_USER_ATTRIBUTE = "currentUser";

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 浏览器跨域预检不带 token，必须在登录校验之前放行
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        // 只拦截 Controller 方法；静态资源等非 HandlerMethod 直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 缺失或无效 token 都会抛 UnauthorizedException，由 GlobalExceptionHandler 转 401
        User user = authService.requireUser(request.getHeader("Authorization"));
        request.setAttribute(CURRENT_USER_ATTRIBUTE, user);

        // 方法上的 @RequireRole 优先，其次看 Controller 类上的
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole == null) {
            // 没标注解：登录后即可访问
            return true;
        }

        for (Role allowedRole : requireRole.value()) {
            if (allowedRole == user.getRole()) {
                return true;
            }
        }
        throw new ForbiddenException("没有权限执行此操作");
    }
}
