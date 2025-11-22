package com.enigcode.frozen_backend.users.controller;

import com.enigcode.frozen_backend.users.DTO.AuthResponseDTO;
import com.enigcode.frozen_backend.users.DTO.LoginRequestDTO;
import com.enigcode.frozen_backend.users.DTO.UserDetailDTO;
import com.enigcode.frozen_backend.users.service.UserService;
import com.enigcode.frozen_backend.common.security.LoginAttemptService;
import com.enigcode.frozen_backend.notifications.service.SseNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
 

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    UserService userService;

    @Mock
    LoginAttemptService loginAttemptService;

    @Mock
    SseNotificationService sseNotificationService;

    @InjectMocks
    com.enigcode.frozen_backend.users.controller.AuthController controller;

    @BeforeEach
    void setup() {
    }

    @Test
    @DisplayName("login throws BlockedUserException when blocked")
    void login_blocked_throws() throws Exception {
        LoginRequestDTO req = LoginRequestDTO.builder().username("bob").password("x").build();
        when(loginAttemptService.isBlocked("bob")).thenReturn(true);

        HttpServletRequest httpReq = mock(HttpServletRequest.class);
        HttpServletResponse httpRes = mock(HttpServletResponse.class);

        assertThatThrownBy(() -> controller.login(req, httpReq, httpRes)).isInstanceOf(com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BlockedUserException.class);
    }

    @Test
    @DisplayName("login success returns AuthResponseDTO and calls loginSuccess")
    void login_success() throws Exception {
        LoginRequestDTO req = LoginRequestDTO.builder().username("alice").password("pw").build();

        when(loginAttemptService.isBlocked("alice")).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenAnswer(inv -> inv.getArgument(0));

        UserDetailDTO userDTO = UserDetailDTO.builder().username("alice").roles(java.util.Set.of("ADMIN")).build();
        when(userService.getUserByUsername("alice")).thenReturn(userDTO);

        HttpServletRequest httpReq = mock(HttpServletRequest.class);
        when(httpReq.getSession(true)).thenReturn(mock(jakarta.servlet.http.HttpSession.class));
        HttpServletResponse httpRes = mock(HttpServletResponse.class);

        AuthResponseDTO resp = controller.login(req, httpReq, httpRes).getBody();

        assertThat(resp).isNotNull();
        assertThat(resp.getUsername()).isEqualTo("alice");
        verify(loginAttemptService).loginSuccess("alice");
    }

    @Test
    @DisplayName("logout clears SSE and invalidates session when authenticated")
    void logout_clearsSseAndInvalidatesSession() {
        // Prepare auth context with username
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            "john",
            "x",
            java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        HttpServletRequest req = mock(HttpServletRequest.class);
        jakarta.servlet.http.HttpSession session = mock(jakarta.servlet.http.HttpSession.class);
        when(req.getSession(false)).thenReturn(session);
        when(req.getSession()).thenReturn(session);
        HttpServletResponse res = mock(HttpServletResponse.class);

        controller.logout(req, res);

        verify(sseNotificationService).closeAllConnectionsForUsername("john");
        verify(sseNotificationService).removeUserFromCacheIfNoActiveConnections("john");
        verify(session).invalidate();
    }

    @Test
    @DisplayName("me returns 401 when not authenticated and 200 when authenticated")
    void me_and_validate() {
        // unauthenticated
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        assertThat(controller.me(null).getStatusCodeValue()).isEqualTo(401);

        // authenticated
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            "me",
            "x",
            java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")));
        when(userService.getUserByUsername("me")).thenReturn(UserDetailDTO.builder().username("me").build());
        assertThat(controller.me(auth).getStatusCodeValue()).isEqualTo(200);
    }
}
