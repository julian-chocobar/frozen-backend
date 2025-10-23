package com.enigcode.frozen_backend.users.security;

import com.enigcode.frozen_backend.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("userSecurity")
@RequiredArgsConstructor
public class UserSecurity {

    private final UserService userService; // opcional, para fallback por username

    public boolean isSelf(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            return false;

        Object principal = auth.getPrincipal();

        // Si tu principal es CustomUserDetails con getId()
        try {
            // intenta reflectivamente obtener getId() si existe
            var method = principal.getClass().getMethod("getId");
            Object idObj = method.invoke(principal);
            if (idObj instanceof Long) {
                return userId.equals((Long) idObj);
            }
        } catch (Exception ignored) {
        }

        // fallback: comparar username con el username guardado en la entidad
        String username = auth.getName();
        try {
            var dto = userService.getUserById(userId);
            return dto != null && username.equals(dto.getUsername());
        } catch (Exception e) {
            return false;
        }
    }
}
