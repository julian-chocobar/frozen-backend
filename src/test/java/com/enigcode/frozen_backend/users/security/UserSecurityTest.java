package com.enigcode.frozen_backend.users.security;

import com.enigcode.frozen_backend.users.DTO.UserDetailDTO;
import com.enigcode.frozen_backend.users.model.RoleEntity;
import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.repository.UserRepository;
import com.enigcode.frozen_backend.users.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserSecurityTest {

    private UserService userService;
    private UserRepository userRepository;
    private UserSecurity userSecurity;

    @BeforeEach
    void setup() {
        userService = mock(UserService.class);
        userRepository = mock(UserRepository.class);
        userSecurity = new UserSecurity(userService, userRepository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void isSelf_returnsTrue_whenIdsMatch() {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            "alice",
            "N/A",
            java.util.List.of(() -> "ROLE_USER")
        )
    );
        when(userService.getUserByUsername("alice")).thenReturn(
                UserDetailDTO.builder().id(42L).username("alice").build()
        );

        assertThat(userSecurity.isSelf(42L)).isTrue();
        assertThat(userSecurity.isSelf(41L)).isFalse();
    }

    @Test
    void isSelf_returnsFalse_whenAnonymous() {
        // No auth in context
        assertThat(userSecurity.isSelf(1L)).isFalse();
    }

    @Test
    void canDeactivateUser_adminCannotToggleSelf_orOtherAdmin_butCanToggleNonAdmin() {
        // Current user = admin1
        Set<RoleEntity> adminRole = new HashSet<>();
        adminRole.add(RoleEntity.builder().id(1L).name("ADMIN").build());

        User currentAdmin = User.builder().id(100L).username("admin1").roles(adminRole).build();
        when(userRepository.findByUsername("admin1")).thenReturn(Optional.of(currentAdmin));

        // Target 1: self
        when(userRepository.findById(100L)).thenReturn(Optional.of(currentAdmin));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin1", "N/A")
        );

        assertThat(userSecurity.canDeactivateUser(100L)).isFalse(); // no se puede a sí mismo

        // Target 2: another admin
        User otherAdmin = User.builder().id(101L).username("admin2").roles(adminRole).build();
        when(userRepository.findById(101L)).thenReturn(Optional.of(otherAdmin));

        assertThat(userSecurity.canDeactivateUser(101L)).isFalse();

        // Target 3: non-admin
        Set<RoleEntity> nonAdminRoles = new HashSet<>();
        nonAdminRoles.add(RoleEntity.builder().id(2L).name("OPERARIO_DE_ALMACEN").build());
        User worker = User.builder().id(200L).username("worker").roles(nonAdminRoles).build();
        when(userRepository.findById(200L)).thenReturn(Optional.of(worker));

        assertThat(userSecurity.canDeactivateUser(200L)).isTrue();
    }
}
