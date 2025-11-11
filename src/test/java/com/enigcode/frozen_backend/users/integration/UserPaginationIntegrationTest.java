package com.enigcode.frozen_backend.users.integration;

import com.enigcode.frozen_backend.users.model.Role;
import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.enigcode.frozen_backend.sectors.repository.SectorRepository;
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
class UserPaginationIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private SectorRepository sectorRepository;

  

  @BeforeEach
  void seedUsers() {
    sectorRepository.deleteAll();
    userRepository.deleteAll();

  for (int i = 1; i <= 7; i++) {
    Role role = (i % 2 == 0) ? Role.SUPERVISOR_DE_ALMACEN : Role.OPERARIO_DE_ALMACEN;
    Set<Role> mutableRoles = new HashSet<>();
    mutableRoles.add(role);

    User u = User.builder()
      .username("user" + i)
      .password(passwordEncoder.encode("Password123"))
      .name("User " + i)
      .email("user" + i + "@test.com")
      .roles(mutableRoles)
      .enabled(true)
      .accountNonExpired(true)
      .accountNonLocked(true)
      .credentialsNonExpired(true)
      .creationDate(OffsetDateTime.now())
      .build();
    userRepository.save(u);
  }

  Set<Role> adminRoles = new HashSet<>();
  adminRoles.add(Role.ADMIN);

  User admin = User.builder()
    .username("admin_test")
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
  }

  @Test
  @WithMockUser(username = "admin_test", roles = "ADMIN")
  void findAll_defaultPagination_returnsFirstPage() throws Exception {
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
  @WithMockUser(username = "admin_test", roles = "ADMIN")
  void findAll_customPageSize_returnsCorrectNumberOfElements() throws Exception {
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
  @WithMockUser(username = "admin_test", roles = "ADMIN")
  void findAll_sortByNameDesc_returnsSortedResults() throws Exception {
    mockMvc.perform(get("/users")
        .param("sort", "username,asc")
        .param("size", "5")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].username", startsWith("admin")))
        .andExpect(jsonPath("$.content[1].username", startsWith("user")));
  }

  @Test
  @WithMockUser(username = "admin_test", roles = "ADMIN")
  void findAll_outOfRangePage_returnsEmptyContent() throws Exception {
    mockMvc.perform(get("/users")
        .param("page", "999")
        .param("size", "5")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(0)))
        .andExpect(jsonPath("$.currentPage").value(999));
  }

  @Test
  @WithMockUser(username = "admin_test", roles = "ADMIN")
  void toggleActive_success_togglesActiveStatus() throws Exception {
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
