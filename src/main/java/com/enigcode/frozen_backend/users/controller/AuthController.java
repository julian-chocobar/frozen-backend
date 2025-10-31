package com.enigcode.frozen_backend.users.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BlockedUserException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.InvalidCredentialsException;
import com.enigcode.frozen_backend.common.security.LoginAttemptService;
import com.enigcode.frozen_backend.users.DTO.AuthResponseDTO;
import com.enigcode.frozen_backend.users.DTO.LoginRequestDTO;
import com.enigcode.frozen_backend.users.DTO.UserDetailDTO;
import com.enigcode.frozen_backend.users.service.UserService;
import com.enigcode.frozen_backend.notifications.service.SseNotificationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContext;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final LoginAttemptService loginAttemptService;
    private final SseNotificationService sseNotificationService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO requestBody,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (loginAttemptService.isBlocked(requestBody.getUsername())) {
            throw new BlockedUserException(loginAttemptService.getBlockedMessage(requestBody.getUsername()));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestBody.getUsername(),
                            requestBody.getPassword()));

            // Crear sesión y guardar SecurityContext explícitamente
            request.getSession(true);
            SecurityContext context = new SecurityContextImpl(authentication);
            SecurityContextHolder.setContext(context);
            new HttpSessionSecurityContextRepository().saveContext(context, request, response); // ← AÑADE ESTA LÍNEA

            loginAttemptService.loginSuccess(requestBody.getUsername());

            UserDetailDTO userDTO = userService.getUserByUsername(requestBody.getUsername());

            return ResponseEntity.ok(AuthResponseDTO.builder()
                    .token("SESSION")
                    .username(userDTO.getUsername())
                    .roles(userDTO.getRoles())
                    .message("Login exitoso")
                    .build());

        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(requestBody.getUsername());
            throw new InvalidCredentialsException(
                    "Credenciales incorrectas",
                    loginAttemptService.getRemainingAttempts(requestBody.getUsername()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // Obtener la autenticación actual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            String username = authentication.getName();
            // Intentar limpiar el cache SSE solo si el usuario ya no tiene conexiones
            // activas
            try {
                sseNotificationService.closeAllConnectionsForUsername(username);
                sseNotificationService.removeUserFromCacheIfNoActiveConnections(username);
            } catch (Exception ignored) {
            }
            // Limpiar el contexto de seguridad
            SecurityContextHolder.clearContext();

            // Invalidar la sesión HTTP
            if (request.getSession(false) != null) {
                request.getSession().invalidate();
            }
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validate(Authentication auth) {
        boolean authenticated = auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(String.valueOf(auth.getPrincipal()));

        if (!authenticated) {
            return ResponseEntity.status(401).build();
        }

        // Additional null check to ensure auth is not null before calling getName()
        if (auth == null) {
            return ResponseEntity.status(401).build();
        }

        UserDetailDTO user = userService.getUserByUsername(auth.getName());
        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")
                .body(java.util.Map.of(
                        "authenticated", true,
                        "username", user.getUsername(),
                        "roles", user.getRoles()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDetailDTO> me(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            return ResponseEntity.status(401).build();
        }
        UserDetailDTO user = userService.getUserByUsername(auth.getName());
        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")
                .body(user);
    }

}
