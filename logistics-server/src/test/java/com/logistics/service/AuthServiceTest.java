package com.logistics.service;

import com.logistics.dto.LoginRequest;
import com.logistics.dto.LoginResponse;
import com.logistics.dto.UserDTO;
import com.logistics.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AuthService 的单元测试：登录、me、logout、Bearer 解析。
 */
class AuthServiceTest {

    private final AuthService authService = new AuthService();

    @Test
    void adminLoginReturnsAdminToken() {
        LoginResponse response = authService.login(new LoginRequest("admin", "admin123"));

        assertThat(response.token()).isNotBlank();
        assertThat(response.user().id()).isEqualTo(1);
        assertThat(response.user().username()).isEqualTo("admin");
        assertThat(response.user().role()).isEqualTo("ADMIN");
    }

    @Test
    void userLoginReturnsUserToken() {
        LoginResponse response = authService.login(new LoginRequest("user", "user123"));

        assertThat(response.token()).isNotBlank();
        assertThat(response.user().id()).isEqualTo(2);
        assertThat(response.user().username()).isEqualTo("user");
        assertThat(response.user().role()).isEqualTo("USER");
    }

    @Test
    void wrongPasswordIsRejected() {
        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "wrong")))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void unknownUsernameIsRejected() {
        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody", "admin123")))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void meReturnsCurrentUserWithValidToken() {
        LoginResponse response = authService.login(new LoginRequest("user", "user123"));

        UserDTO user = authService.me("Bearer " + response.token());

        assertThat(user).isEqualTo(response.user());
    }

    @Test
    void bearerSchemeIsCaseInsensitive() {
        LoginResponse response = authService.login(new LoginRequest("admin", "admin123"));

        UserDTO user = authService.me("bEaReR " + response.token());

        assertThat(user.username()).isEqualTo("admin");
    }

    @Test
    void missingOrMalformedAuthorizationIsRejected() {
        assertThatThrownBy(() -> authService.me(null))
            .isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> authService.me(""))
            .isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> authService.me("token-without-bearer"))
            .isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> authService.me("Bearer "))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void invalidTokenIsRejected() {
        assertThatThrownBy(() -> authService.me("Bearer not-a-real-token"))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining("未登录或登录已失效");
    }

    @Test
    void logoutInvalidatesToken() {
        LoginResponse response = authService.login(new LoginRequest("admin", "admin123"));
        String authorization = "Bearer " + response.token();

        authService.logout(authorization);

        assertThatThrownBy(() -> authService.me(authorization))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void logoutWithInvalidTokenIsRejected() {
        assertThatThrownBy(() -> authService.logout("Bearer not-a-real-token"))
            .isInstanceOf(UnauthorizedException.class);
    }
}
