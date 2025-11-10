package com.enigcode.frozen_backend.common.security;

import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.model.Role;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // Convertir los roles String a Role enum
        Set<Role> roleSet = Arrays.stream(customUser.roles())
                .map(this::mapToRole)
                .collect(Collectors.toSet());

        // Crear el User personalizado de tu proyecto
        User user = User.builder()
                .id(customUser.id())
                .username(customUser.username())
                .password(customUser.password())
                .name("Test User")
                .roles(roleSet)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .isActive(true)
                .creationDate(OffsetDateTime.now())
                .build();

        // Crear la autenticación con tu User como principal
        // El User ya tiene getAuthorities() implementado
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user, 
                user.getPassword(), 
                user.getAuthorities()
        );

        context.setAuthentication(auth);
        return context;
    }

    private Role mapToRole(String role) {
        try {
            return Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            return Role.OPERARIO_DE_PRODUCCION;
        }
    }
}
