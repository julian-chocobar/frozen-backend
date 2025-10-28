package com.enigcode.frozen_backend.users.integration;

import com.enigcode.frozen_backend.users.model.Role;
import com.enigcode.frozen_backend.users.model.RoleEntity;
import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.repository.RoleRepository;
import com.enigcode.frozen_backend.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserRoleAuthorizationMatrixIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long otherUserId; // a non-admin target user id
    private Long adminId;     // admin user id

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        // Ensure all RoleEntity records exist
        for (Role r : Role.values()) {
            roleRepository.findByName(r.name())
                    .orElseGet(() -> roleRepository.save(RoleEntity.builder().name(r.name()).build()));
        }

        // Seed one user per role: username = "user_" + roleName
        for (Role r : Role.values()) {
            RoleEntity roleEntity = roleRepository.findByName(r.name()).orElseThrow();
            Set<RoleEntity> roles = new HashSet<>();
            roles.add(roleEntity);
            User u = User.builder()
                    .username("user_" + r.name())
                    .password(passwordEncoder.encode("Passw0rd!"))
                    .name("User " + r.name())
                    .email("user_" + r.name().toLowerCase() + "@test.com")
                    .roles(roles)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .creationDate(OffsetDateTime.now())
                    .build();
            userRepository.save(u);
        }

        // Keep handy ids
        adminId = userRepository.findByUsername("user_" + Role.ADMIN.name()).orElseThrow().getId();

        // Use any non-admin as target "other" user (prefer OPERARIO_DE_ALMACEN if exists)
        String otherUsername = "user_" + Role.OPERARIO_DE_ALMACEN.name();
        otherUserId = userRepository.findByUsername(otherUsername)
                .orElseGet(() -> userRepository.findByUsername("user_" + Role.SUPERVISOR_DE_ALMACEN.name()).orElseThrow())
                .getId();
    }

    // ---------- ADMIN-only endpoints -----------

    @ParameterizedTest(name = "createUser: role {0}")
    @EnumSource(Role.class)
    @DisplayName("POST /users requires ADMIN")
    void createUser_requiresAdmin(Role role) throws Exception {
        String payload = """
                {
                  \"username\": \"new_user\",
                  \"password\": \"Password123\",
                  \"name\": \"New User\",
                  \"roles\": [\"OPERARIO_DE_ALMACEN\"],
                  \"email\": \"new_user@test.com\",
                  \"phoneNumber\": \"1234567890\"
                }
                """;

        if (role == Role.ADMIN) {
            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload)
                            .with(csrf())
                            .with(user("user_" + role.name()).roles(role.name())))
                    .andExpect(status().isCreated());
        } else {
            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload)
                            .with(csrf())
                            .with(user("user_" + role.name()).roles(role.name())))
                    .andExpect(status().isForbidden());
        }
    }

    @ParameterizedTest(name = "updateUserRole: role {0}")
    @EnumSource(Role.class)
    @DisplayName("PATCH /users/{id}/roles requires ADMIN")
    void updateUserRole_requiresAdmin(Role role) throws Exception {
        String payload = """
                { "roles": ["OPERARIO_DE_ALMACEN"] }
                """;
        Long targetId = otherUserId;

        if (role == Role.ADMIN) {
            mockMvc.perform(patch("/users/{id}/roles", targetId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload)
                            .with(csrf())
                            .with(user("user_" + role.name()).roles(role.name())))
                    .andExpect(status().isCreated());
        } else {
            mockMvc.perform(patch("/users/{id}/roles", targetId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload)
                            .with(csrf())
                            .with(user("user_" + role.name()).roles(role.name())))
                    .andExpect(status().isForbidden());
        }
    }

    @ParameterizedTest(name = "list users: role {0}")
    @EnumSource(Role.class)
    @DisplayName("GET /users requires ADMIN")
    void listUsers_requiresAdmin(Role role) throws Exception {
        if (role == Role.ADMIN) {
            mockMvc.perform(get("/users").with(csrf()).with(user("user_" + role.name()).roles(role.name())))
                    .andExpect(status().isOk());
        } else {
            mockMvc.perform(get("/users").with(csrf()).with(user("user_" + role.name()).roles(role.name())))
                    .andExpect(status().isForbidden());
        }
    }

    // ---------- self-or-admin endpoints -----------

    @ParameterizedTest(name = "update self: role {0}")
    @EnumSource(Role.class)
    @DisplayName("PATCH /users/{id} allows self or ADMIN")
    void updateUser_selfOrAdmin(Role role) throws Exception {
        Long selfId = idOf("user_" + role.name());
    // Read current names to avoid nulling non-nullable column via mapper
    String selfName = userRepository.findById(selfId).orElseThrow().getName();
    // Use a valid, unique-enough email for self
    String payloadSelf = String.format("{ \"email\": \"updated_%s@test.com\", \"name\": \"%s\" }",
        role.name().toLowerCase(), selfName);

    // self -> OK
    mockMvc.perform(patch("/users/{id}", selfId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(payloadSelf)
            .with(csrf())
            .with(user("user_" + role.name()).roles(role.name())))
        .andExpect(status().isCreated());

        // other -> ADMIN OK, others 403
        Long targetId = otherUserId.equals(selfId) ? adminId : otherUserId;
    String targetName = userRepository.findById(targetId).orElseThrow().getName();
    // Change only phone (non-unique) and include existing name to avoid null
        String payloadTarget = String.format("{ \"phoneNumber\": \"%s\", \"name\": \"%s\" }",
            "9999999999", targetName);
        if (role == Role.ADMIN) {
            mockMvc.perform(patch("/users/{id}", targetId)
                            .contentType(MediaType.APPLICATION_JSON)
                .content(payloadTarget)
                            .with(csrf())
                            .with(user("user_" + role.name()).roles(role.name())))
                    .andExpect(status().isCreated());
        } else {
            mockMvc.perform(patch("/users/{id}", targetId)
                            .contentType(MediaType.APPLICATION_JSON)
                .content(payloadTarget)
                            .with(csrf())
                            .with(user("user_" + role.name()).roles(role.name())))
                    .andExpect(status().isForbidden());
        }
    }

    @ParameterizedTest(name = "update password self: role {0}")
    @EnumSource(Role.class)
    @DisplayName("PATCH /users/{id}/password allows self or ADMIN (when passwords match)")
    void updatePassword_selfOrAdmin(Role role) throws Exception {
        Long selfId = idOf("user_" + role.name());
        String okPayload = """
                { "password": "NewPassword123", "passwordConfirmacion": "NewPassword123" }
                """;

    // self -> OK
    mockMvc.perform(patch("/users/{id}/password", selfId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(okPayload)
            .with(csrf())
            .with(user("user_" + role.name()).roles(role.name())))
                .andExpect(status().isCreated());

        // other -> ADMIN OK, others 403
        Long targetId = otherUserId.equals(selfId) ? adminId : otherUserId;
        if (role == Role.ADMIN) {
            mockMvc.perform(patch("/users/{id}/password", targetId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(okPayload)
                            .with(csrf())
                            .with(user("user_" + role.name()).roles(role.name())))
                    .andExpect(status().isCreated());
        } else {
            mockMvc.perform(patch("/users/{id}/password", targetId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(okPayload)
                            .with(csrf())
                            .with(user("user_" + role.name()).roles(role.name())))
                    .andExpect(status().isForbidden());
        }
    }

    @ParameterizedTest(name = "get user self: role {0}")
    @EnumSource(Role.class)
    @DisplayName("GET /users/{id} allows self or ADMIN")
    void getUser_selfOrAdmin(Role role) throws Exception {
        Long selfId = idOf("user_" + role.name());

    // self -> OK
    mockMvc.perform(get("/users/{id}", selfId)
            .with(csrf())
            .with(user("user_" + role.name()).roles(role.name())))
                .andExpect(status().isOk());

        // other -> ADMIN OK, others 403
        Long targetId = otherUserId.equals(selfId) ? adminId : otherUserId;
        if (role == Role.ADMIN) {
            mockMvc.perform(get("/users/{id}", targetId)
                            .with(csrf())
                            .with(user("user_" + role.name()).roles(role.name())))
                    .andExpect(status().isOk());
        } else {
            mockMvc.perform(get("/users/{id}", targetId)
                            .with(csrf())
                            .with(user("user_" + role.name()).roles(role.name())))
                    .andExpect(status().isForbidden());
        }
    }

    // ---------- toggle-active rules -----------

    @Test
    @WithMockUser(username = "user_" + "ADMIN", roles = {"ADMIN"})
    @DisplayName("ADMIN can toggle non-admin user")
    void admin_canToggle_nonAdmin() throws Exception {
        mockMvc.perform(patch("/users/{id}/toggle-active", otherUserId).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user_" + "ADMIN", roles = {"ADMIN"})
    @DisplayName("ADMIN cannot toggle another ADMIN user")
    void admin_cannotToggle_otherAdmin() throws Exception {
        mockMvc.perform(patch("/users/{id}/toggle-active", adminId).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMIN"}, mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Non-admin cannot toggle anyone (self or others)")
    void nonAdmin_cannotToggle_any(Role role) throws Exception {
        Long selfId = idOf("user_" + role.name());
    mockMvc.perform(patch("/users/{id}/toggle-active", selfId).with(csrf())
            .with(user("user_" + role.name()).roles(role.name())))
                .andExpect(status().isForbidden());
    mockMvc.perform(patch("/users/{id}/toggle-active", otherUserId).with(csrf())
            .with(user("user_" + role.name()).roles(role.name())))
                .andExpect(status().isForbidden());
    }

    // ---------- helpers -----------

    private Long idOf(String username) {
        return userRepository.findByUsername(username).orElseThrow().getId();
    }

    // no-op
}
