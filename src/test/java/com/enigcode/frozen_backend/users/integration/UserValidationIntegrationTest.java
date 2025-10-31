package com.enigcode.frozen_backend.users.integration;

import com.enigcode.frozen_backend.users.model.Role;
import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.repository.UserRepository;
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
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Set;
import java.util.HashSet;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserValidationIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();

    Set<Role> adminRoles = new HashSet<>();
    adminRoles.add(Role.ADMIN);
    User admin = User.builder()
      .username("admin")
      .password(passwordEncoder.encode("AdminPass123"))
      .name("Admin")
      .email("admin@test.com")
      .roles(adminRoles)
      .enabled(true)
      .accountNonExpired(true)
      .accountNonLocked(true)
      .credentialsNonExpired(true)
      .creationDate(OffsetDateTime.now())
      .build();
    userRepository.save(admin);

    Set<Role> userRoles = new HashSet<>();
    userRoles.add(Role.OPERARIO_DE_ALMACEN);
    User user1 = User.builder()
      .username("user1")
      .password(passwordEncoder.encode("Password123"))
      .name("User One")
      .email("user1@test.com")
      .roles(userRoles)
      .enabled(true)
      .accountNonExpired(true)
      .accountNonLocked(true)
      .credentialsNonExpired(true)
      .creationDate(OffsetDateTime.now())
      .build();
    userRepository.save(user1);
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void createUser_invalidPayload_returns400() throws Exception {
    String badRequest = """
        {
          "username": "",
          "password": "short",
          "name": "A",
          "roles": null,
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
    .andExpect(jsonPath("$.details.roles", not(emptyString())))
        .andExpect(jsonPath("$.details.email", not(emptyString())))
        .andExpect(jsonPath("$.details.phoneNumber", not(emptyString())));
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void createUser_weakPassword_returns400() throws Exception {
    String badPwd = """
        {
          "username": "newuser1",
          "password": "password",
          "name": "New User",
          "roles": ["OPERARIO_DE_ALMACEN"],
          "email": "newuser1@test.com"
        }
        """;

    mockMvc.perform(post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(badPwd)
        .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details.password", not(emptyString())));
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void createUser_emptyRoles_returns400() throws Exception {
    Long userId = userRepository.findByUsername("user1").orElseThrow().getId();

    String emptyRoles = """
        {
          "roles": []
        }
        """;

    mockMvc.perform(patch("/users/{id}/roles", userId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(emptyRoles)
        .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details.roles", not(emptyString())));
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void createUser_invalidEmail_returns400() throws Exception {
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
  void updateUser_invalidPayload_returns400() throws Exception {
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
  void updatePassword_weakPassword_returns400() throws Exception {
    Long userId = userRepository.findByUsername("user1").orElseThrow().getId();

    String weakPwd = """
        {
          "password": "weakpass",
          "passwordConfirmacion": "weakpass"
        }
        """;

    mockMvc.perform(patch("/users/{id}/password", userId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(weakPwd)
        .with(csrf()))
        .andExpect(status().isBadRequest());
  }
}
