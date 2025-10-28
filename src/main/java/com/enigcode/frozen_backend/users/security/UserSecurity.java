package com.enigcode.frozen_backend.users.security;

import com.enigcode.frozen_backend.users.service.UserService;

import jakarta.persistence.EntityNotFoundException;

import com.enigcode.frozen_backend.users.DTO.UserDetailDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.repository.UserRepository;

@Component("userSecurity")
@RequiredArgsConstructor
public class UserSecurity {

    private final UserService userService;
    private final UserRepository userRepository;

    public boolean isSelf(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            return false;
        }
        UserDetailDTO me = userService.getUserByUsername(auth.getName());
        return me != null && me.getId() != null && me.getId().equals(userId);
    }

    public boolean canDeactivateUser(Long targetUserId) {
        // Obtener el usuario autenticado actual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // No puede desactivarse a sí mismo
        if (currentUser.getId().equals(targetUserId)) {
            return false;
        }
        // Buscar el usuario objetivo
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario objetivo no encontrado"));

        // Verificar si el usuario actual tiene rol ADMIN
        boolean currentUserIsAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));

        // Verificar si el usuario objetivo tiene rol ADMIN
        boolean targetUserIsAdmin = targetUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));

        // Un ADMIN no puede desactivar a otro ADMIN
        if (currentUserIsAdmin && targetUserIsAdmin) {
            return false;
        }

        return true;
    }
}
