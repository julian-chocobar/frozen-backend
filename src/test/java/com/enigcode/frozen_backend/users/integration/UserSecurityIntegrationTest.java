package com.enigcode.frozen_backend.users.integration;

import com.enigcode.frozen_backend.users.model.Role;
import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User supervisorUser;
    private User operarioUser;

    @BeforeEach
    void setUp() {
        // Crear usuarios de prueba con diferentes roles
        adminUser = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .name("Admin User")
                .email("admin@test.com")
                .phoneNumber("1234567890")
                .role(Role.ADMIN)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        adminUser = userRepository.save(adminUser);

        supervisorUser = User.builder()
                .username("supervisor")
                .password(passwordEncoder.encode("super123"))
                .name("Supervisor User")
                .email("supervisor@test.com")
                .phoneNumber("1234567891")
                .role(Role.SUPERVISOR)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        supervisorUser = userRepository.save(supervisorUser);

        operarioUser = User.builder()
                .username("operario")
                .password(passwordEncoder.encode("oper123"))
                .name("Operario User")
                .email("operario@test.com")
                .phoneNumber("1234567892")
                .role(Role.OPERARIO)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        operarioUser = userRepository.save(operarioUser);
    }

    // ============================================
    // TESTS ADMIN - Puede hacer TODO
    // ============================================

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void admin_canCreateUser() throws Exception {
        String requestBody = """
        {
            "username": "newuser",
                "password": "Password123",
                "passwordConfirmacion": "Password123",
            "name": "New User",
            "email": "newuser@test.com",
            "phoneNumber": "9999999999",
            "role": "OPERARIO"
        }
        """;

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("OPERARIO"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void admin_canListAllUsers() throws Exception {
        mockMvc.perform(get("/users")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void admin_canViewAnyUser() throws Exception {
        mockMvc.perform(get("/users/{id}", supervisorUser.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("supervisor"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void admin_canUpdateAnyUser() throws Exception {
        String requestBody = """
        {
            "name": "Updated Supervisor Name",
            "email": "updated@test.com"
        }
        """;

        mockMvc.perform(patch("/users/{id}", supervisorUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Updated Supervisor Name"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void admin_canToggleAnyUser() throws Exception {
        mockMvc.perform(patch("/users/{id}/toggle-active", operarioUser.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void admin_canChangeAnyRole() throws Exception {
        String requestBody = """
        {
            "role": "SUPERVISOR"
        }
        """;

        mockMvc.perform(patch("/users/{id}/role", operarioUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("SUPERVISOR"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void admin_canChangeAnyPassword() throws Exception {
        String requestBody = """
        {
              "password": "NewPassword123",
              "passwordConfirmacion": "NewPassword123"
        }
        """;

        mockMvc.perform(patch("/users/{id}/password", supervisorUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
                .andExpect(status().isCreated());
    }

    // ============================================
    // TESTS SUPERVISOR - Permisos Propios
    // ============================================

    @Test
    @WithMockUser(username = "supervisor", roles = "SUPERVISOR")
    void supervisor_canViewOwnData() throws Exception {
        mockMvc.perform(get("/users/{id}", supervisorUser.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("supervisor"));
    }

    @Test
    @WithMockUser(username = "supervisor", roles = "SUPERVISOR")
    void supervisor_canUpdateOwnData() throws Exception {
        String requestBody = """
        {
            "name": "Updated Supervisor",
            "email": "supervisor.updated@test.com"
        }
        """;

        mockMvc.perform(patch("/users/{id}", supervisorUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Updated Supervisor"));
    }

    @Test
    @WithMockUser(username = "supervisor", roles = "SUPERVISOR")
    void supervisor_canChangeOwnPassword() throws Exception {
        String requestBody = """
        {
              "password": "NewPassword123",
              "passwordConfirmacion": "NewPassword123"
        }
        """;

        mockMvc.perform(patch("/users/{id}/password", supervisorUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
                .andExpect(status().isCreated());
    }

    // ============================================
    // TESTS SUPERVISOR - Restricciones
    // ============================================

    @Test
    @WithMockUser(username = "supervisor", roles = "SUPERVISOR")
    void supervisor_cannotCreateUser() throws Exception {
        String requestBody = """
        {
            "username": "newuser",
                "password": "Password123",
                "passwordConfirmacion": "Password123",
            "name": "New User",
            "email": "newuser@test.com",
            "phoneNumber": "9999999999",
            "role": "OPERARIO"
        }
        """;

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "supervisor", roles = "SUPERVISOR")
    void supervisor_cannotListAllUsers() throws Exception {
        mockMvc.perform(get("/users")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "supervisor", roles = "SUPERVISOR")
    void supervisor_cannotViewOtherUsers() throws Exception {
        mockMvc.perform(get("/users/{id}", operarioUser.getId())
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "supervisor", roles = "SUPERVISOR")
    void supervisor_cannotUpdateOtherUsers() throws Exception {
        String requestBody = """
        {
            "name": "Hacked Name"
        }
        """;

        mockMvc.perform(patch("/users/{id}", operarioUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "supervisor", roles = "SUPERVISOR")
    void supervisor_cannotToggleUsers() throws Exception {
        mockMvc.perform(patch("/users/{id}/toggle-active", operarioUser.getId())
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "supervisor", roles = "SUPERVISOR")
    void supervisor_cannotChangeRoles() throws Exception {
        String requestBody = """
        {
            "role": "ADMIN"
        }
        """;

        mockMvc.perform(patch("/users/{id}/role", operarioUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "supervisor", roles = "SUPERVISOR")
    void supervisor_cannotChangeOtherPasswords() throws Exception {
        String requestBody = """
        {
              "password": "NewPassword123",
              "passwordConfirmacion": "NewPassword123"
        }
        """;

        mockMvc.perform(patch("/users/{id}/password", operarioUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ============================================
    // TESTS OPERARIO - Permisos Propios
    // ============================================

    @Test
    @WithMockUser(username = "operario", roles = "OPERARIO")
    void operario_canViewOwnData() throws Exception {
        mockMvc.perform(get("/users/{id}", operarioUser.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("operario"));
    }

    @Test
    @WithMockUser(username = "operario", roles = "OPERARIO")
    void operario_canUpdateOwnData() throws Exception {
        String requestBody = """
        {
            "name": "Updated Operario",
            "email": "operario.updated@test.com"
        }
        """;

        mockMvc.perform(patch("/users/{id}", operarioUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Updated Operario"));
    }

    @Test
    @WithMockUser(username = "operario", roles = "OPERARIO")
    void operario_canChangeOwnPassword() throws Exception {
        String requestBody = """
        {
              "password": "NewPassword123",
              "passwordConfirmacion": "NewPassword123"
        }
        """;

        mockMvc.perform(patch("/users/{id}/password", operarioUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
                .andExpect(status().isCreated());
    }

    // ============================================
    // TESTS OPERARIO - Restricciones
    // ============================================

    @Test
    @WithMockUser(username = "operario", roles = "OPERARIO")
    void operario_cannotCreateUser() throws Exception {
        String requestBody = """
        {
            "username": "newuser",
                "password": "Password123",
                "passwordConfirmacion": "Password123",
            "name": "New User",
            "email": "newuser@test.com",
            "phoneNumber": "9999999999",
            "role": "OPERARIO"
        }
        """;

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "operario", roles = "OPERARIO")
    void operario_cannotListAllUsers() throws Exception {
        mockMvc.perform(get("/users")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "operario", roles = "OPERARIO")
    void operario_cannotViewOtherUsers() throws Exception {
        mockMvc.perform(get("/users/{id}", supervisorUser.getId())
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "operario", roles = "OPERARIO")
    void operario_cannotUpdateOtherUsers() throws Exception {
        String requestBody = """
        {
            "name": "Hacked Name"
        }
        """;

        mockMvc.perform(patch("/users/{id}", supervisorUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "operario", roles = "OPERARIO")
    void operario_cannotToggleUsers() throws Exception {
        mockMvc.perform(patch("/users/{id}/toggle-active", supervisorUser.getId())
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "operario", roles = "OPERARIO")
    void operario_cannotChangeRoles() throws Exception {
        String requestBody = """
        {
            "role": "ADMIN"
        }
        """;

        mockMvc.perform(patch("/users/{id}/role", supervisorUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "operario", roles = "OPERARIO")
    void operario_cannotChangeOtherPasswords() throws Exception {
        String requestBody = """
        {
              "password": "NewPassword123",
              "passwordConfirmacion": "NewPassword123"
        }
        """;

        mockMvc.perform(patch("/users/{id}/password", supervisorUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
