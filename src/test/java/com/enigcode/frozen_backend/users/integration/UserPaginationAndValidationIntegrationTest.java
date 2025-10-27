package com.enigcode.frozen_backend.users.integration;

import com.enigcode.frozen_backend.users.model.Role;
import com.enigcode.frozen_backend.users.model.RoleEntity;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserPaginationAndValidationIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void seedUsers() {
    // Limpieza por si algún test previo dejó datos (dentro de la Tx del test)
    userRepository.deleteAll();

    // Creamos varios usuarios para probar paginación y ordenamiento
    for (int i = 1; i <= 7; i++) {
      User u = User.builder()
          .username("user" + i)
          .password(passwordEncoder.encode("Password123"))
          .name("User " + i)
          .email("user" + i + "@test.com")
          .roles(Set.of(
              RoleEntity.builder()
                  .name(i % 2 == 0 ? Role.SUPERVISOR_DE_ALMACEN.name() : Role.OPERARIO_DE_ALMACEN.name()).build()))
          .enabled(true)
          .accountNonExpired(true)
          .accountNonLocked(true)
          .credentialsNonExpired(true)
          .build();
      userRepository.save(u);
    }
    // ADMIN para autenticación
    User admin = User.builder()
        .username("admin")
        .password(passwordEncoder.encode("AdminPass123"))
        .name("Admin")
        .email("admin@test.com")
        .roles(Set.of(RoleEntity.builder().name(Role.ADMIN.name()).build()))
        .enabled(true)
        .accountNonExpired(true)
        .accountNonLocked(true)
        .credentialsNonExpired(true)
        .build();
    userRepository.save(admin);
  }

  // =============================
  // Paginación y ordenamiento
  // =============================

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void listUsers_pagination_defaultSortByCreationDateDesc() throws Exception {
    mockMvc.perform(get("/users")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(10))))
        .andExpect(jsonPath("$.currentPage").value(0))
        .andExpect(jsonPath("$.size").value(10))
        .andExpect(jsonPath("$.totalItems", greaterThanOrEqualTo(8)))
        .andExpect(jsonPath("$.totalPages", greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.hasNext").isBoolean())
        .andExpect(jsonPath("$.hasPrevious").value(false))
        .andExpect(jsonPath("$.isFirst").value(true));
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void listUsers_pagination_pageAndSize_work() throws Exception {
    // page=1, size=3 debería devolver 3 elementos y currentPage=1
    mockMvc.perform(get("/users")
        .param("page", "1")
        .param("size", "3")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(3)))
        .andExpect(jsonPath("$.currentPage").value(1))
        .andExpect(jsonPath("$.size").value(3))
        .andExpect(jsonPath("$.totalItems", greaterThanOrEqualTo(8)))
        .andExpect(jsonPath("$.totalPages", greaterThanOrEqualTo(3)));
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void listUsers_sortByUsernameAsc() throws Exception {
    mockMvc.perform(get("/users")
        .param("sort", "username,asc")
        .param("size", "5")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].username", startsWith("admin")))
        .andExpect(jsonPath("$.content[1].username", startsWith("user")));
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void listUsers_pageOutOfRange_returnsEmptyContent() throws Exception {
    mockMvc.perform(get("/users")
        .param("page", "999")
        .param("size", "5")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(0)))
        .andExpect(jsonPath("$.currentPage").value(999));
  }

  // =============================
  // Payload inválidos (400)
  // =============================

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void createUser_invalidPayload_returns400WithDetails() throws Exception {
    String badRequest = """
        {
          "username": "",
          "password": "short",
          "name": "A",
          "role": null,
          "email": "not-an-email",
          "phoneNumber": "12"
        }
        """;

    mockMvc.perform(post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(badRequest)
        .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details.username", not(emptyString())))
        .andExpect(jsonPath("$.details.password", not(emptyString())))
        .andExpect(jsonPath("$.details.name", not(emptyString())))
        .andExpect(jsonPath("$.details.role", not(emptyString())))
        .andExpect(jsonPath("$.details.email", not(emptyString())))
        .andExpect(jsonPath("$.details.phoneNumber", not(emptyString())));
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void updateUser_invalidEmail_returns400() throws Exception {
    // Tomamos un usuario existente (user1)
    Long userId = userRepository.findByUsername("user1").orElseThrow().getId();

    String badEmailPayload = """
        {
          "email": "invalid-email"
        }
        """;

    mockMvc.perform(patch("/users/{id}", userId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(badEmailPayload)
        .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details.email", not(emptyString())));
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void updatePassword_mismatch_returns400() throws Exception {
    Long userId = userRepository.findByUsername("user1").orElseThrow().getId();

    String mismatch = """
        {
          "password": "NewPassword123",
          "passwordConfirmacion": "Different123"
        }
        """;

    mockMvc.perform(patch("/users/{id}/password", userId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(mismatch)
        .with(csrf()))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void updateRole_nullRole_returns400() throws Exception {
    Long userId = userRepository.findByUsername("user1").orElseThrow().getId();

    String nullRole = """
        {
          "role": null
        }
        """;

    mockMvc.perform(patch("/users/{id}/role", userId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(nullRole)
        .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details.role", not(emptyString())));
  }

  // =============================
  // Toggle-active: doble toggle vuelve al estado original
  // =============================
  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void toggleActive_twice_returnsToOriginalState() throws Exception {
    Long userId = userRepository.findByUsername("user1").orElseThrow().getId();

    // Primer toggle
    mockMvc.perform(patch("/users/{id}/toggle-active", userId)
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isActive").value(false));

    // Segundo toggle
    mockMvc.perform(patch("/users/{id}/toggle-active", userId)
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isActive").value(true));
  }
}
