package com.logistics.auth;

import com.logistics.dto.LoginRequest;
import com.logistics.exception.ForbiddenException;
import com.logistics.exception.UnauthorizedException;
import com.logistics.model.User;
import com.logistics.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AuthInterceptor 的单元测试：401、403、放行规则和 currentUser attribute。
 */
class AuthInterceptorTest {

    private final AuthService authService = new AuthService();
    private final AuthInterceptor interceptor = new AuthInterceptor(authService);

    @Test
    void missingTokenIsUnauthorized() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/cities");
        HandlerMethod handler = handler(TestController.class, "adminOnly");

        assertThatThrownBy(() -> interceptor.preHandle(
            request, new MockHttpServletResponse(), handler))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void normalUserCannotAccessAdminMethod() throws Exception {
        MockHttpServletRequest request = requestWithToken(token("user", "user123"));
        HandlerMethod handler = handler(TestController.class, "adminOnly");

        assertThatThrownBy(() -> interceptor.preHandle(
            request, new MockHttpServletResponse(), handler))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("没有权限");
    }

    @Test
    void adminCanAccessAdminMethodAndCurrentUserIsSet() throws Exception {
        MockHttpServletRequest request = requestWithToken(token("admin", "admin123"));
        HandlerMethod handler = handler(TestController.class, "adminOnly");

        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), handler)).isTrue();

        Object currentUser = request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE);
        assertThat(currentUser).isInstanceOf(User.class);
        assertThat(((User) currentUser).getUsername()).isEqualTo("admin");
        assertThat(((User) currentUser).getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void methodWithoutRequireRoleOnlyNeedsLogin() throws Exception {
        MockHttpServletRequest request = requestWithToken(token("user", "user123"));
        HandlerMethod handler = handler(TestController.class, "loggedInOnly");

        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), handler)).isTrue();
    }

    @Test
    void classLevelRequireRoleIsApplied() throws Exception {
        HandlerMethod handler = handler(ClassProtectedController.class, "anyMethod");

        MockHttpServletRequest userRequest = requestWithToken(token("user", "user123"));
        assertThatThrownBy(() -> interceptor.preHandle(
            userRequest, new MockHttpServletResponse(), handler))
            .isInstanceOf(ForbiddenException.class);

        MockHttpServletRequest adminRequest = requestWithToken(token("admin", "admin123"));
        assertThat(interceptor.preHandle(
            adminRequest, new MockHttpServletResponse(), handler)).isTrue();
    }

    @Test
    void optionsRequestIsAllowedWithoutToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/cities");
        HandlerMethod handler = handler(TestController.class, "adminOnly");

        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), handler)).isTrue();
    }

    @Test
    void nonHandlerMethodIsAllowed() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/index.html");

        assertThat(interceptor.preHandle(
            request, new MockHttpServletResponse(), new Object())).isTrue();
    }

    private String token(String username, String password) {
        return authService.login(new LoginRequest(username, password)).token();
    }

    private MockHttpServletRequest requestWithToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/cities");
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private HandlerMethod handler(Class<?> type, String methodName) throws Exception {
        Object bean = type.getDeclaredConstructor().newInstance();
        Method method = type.getMethod(methodName);
        return new HandlerMethod(bean, method);
    }

    /** 测试用 Controller：方法级角色注解。 */
    static class TestController {

        @RequireRole(Role.ADMIN)
        public void adminOnly() {
        }

        public void loggedInOnly() {
        }
    }

    /** 测试用 Controller：类级角色注解。 */
    @RequireRole(Role.ADMIN)
    static class ClassProtectedController {

        public void anyMethod() {
        }
    }
}
