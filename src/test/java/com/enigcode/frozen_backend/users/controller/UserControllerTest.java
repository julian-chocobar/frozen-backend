package com.enigcode.frozen_backend.users.controller;

import com.enigcode.frozen_backend.users.DTO.*;
import com.enigcode.frozen_backend.users.model.Role;
import com.enigcode.frozen_backend.users.security.UserSecurity;
import com.enigcode.frozen_backend.users.service.UserService;
import com.enigcode.frozen_backend.users.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.enigcode.frozen_backend.common.exceptions_configs.GlobalExceptionHandler;

@WebMvcTest(controllers = UserController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class))
@Import(UserSecurity.class)
class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private UserService userService;

        @MockBean
        private UserRepository userRepository;

        @Test
        @WithMockUser(roles = "ADMIN")
        void testCreateUser_success() throws Exception {
                UserCreateDTO createDTO = UserCreateDTO.builder()
                                .username("newuser")
                                .password("NewPass123")
                                .name("New User")
                                .roles(Set.of(Role.OPERARIO_DE_CALIDAD.name()))
                                .email("newuser@example.com")
                                .build();

                UserResponseDTO responseDTO = UserResponseDTO.builder()
                                .id(1L)
                                .username("newuser")
                                .name("New User")
                                .roles(Set.of(Role.OPERARIO_DE_CALIDAD.name()))
                                .isActive(true)
                                .build();

                when(userService.createUser(any(UserCreateDTO.class))).thenReturn(responseDTO);

                mockMvc.perform(post("/users")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDTO)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.username").value("newuser"))
                                .andExpect(jsonPath("$.isActive").value(true));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testToggleActive_success() throws Exception {
                UserResponseDTO responseDTO = UserResponseDTO.builder()
                                .id(1L)
                                .username("user1")
                                .name("User One")
                                .roles(Set.of(Role.OPERARIO_DE_ALMACEN.name()))
                                .isActive(false)
                                .build();

                when(userService.toggleActive(1L)).thenReturn(responseDTO);

                mockMvc.perform(patch("/users/1/toggle-active")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.isActive").value(false));
        }

        @Test
        @WithMockUser(username = "user1", roles = "OPERARIO_DE_CALIDAD")
        void testUpdateUser_success() throws Exception {
                UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                                .name("Updated Name")
                                .email("updated@example.com")
                                .build();

                UserResponseDTO responseDTO = UserResponseDTO.builder()
                                .id(1L)
                                .username("user1")
                                .name("Updated Name")
                                .roles(Set.of(Role.OPERARIO_DE_PRODUCCION.name()))
                                .isActive(true)
                                .build();

                when(userService.updateUser(eq(1L), any(UserUpdateDTO.class))).thenReturn(responseDTO);

                mockMvc.perform(patch("/users/1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.name").value("Updated Name"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testUpdateUserRole_success() throws Exception {
                UpdateRoleDTO updateRoleDTO = UpdateRoleDTO.builder()
                                .roles(Set.of(Role.SUPERVISOR_DE_ALMACEN.name()))
                                .build();

                UserResponseDTO responseDTO = UserResponseDTO.builder()
                                .id(1L)
                                .username("user1")
                                .name("User One")
                                .roles(Set.of(Role.SUPERVISOR_DE_ALMACEN.name()))
                                .isActive(true)
                                .build();

                when(userService.updateUserRole(eq(1L), any(UpdateRoleDTO.class))).thenReturn(responseDTO);

                mockMvc.perform(patch("/users/1/roles")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRoleDTO)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.roles[0]").value("SUPERVISOR_DE_ALMACEN"));
        }

        @Test
        @WithMockUser(username = "user1", roles = "OPERARIO_DE_PRODUCCION")
        void testUpdateUserPassword_success() throws Exception {
                UpdatePasswordDTO updatePasswordDTO = UpdatePasswordDTO.builder()
                                .password("NewPassword123")
                                .passwordConfirmacion("NewPassword123")
                                .build();

                UserResponseDTO responseDTO = UserResponseDTO.builder()
                                .id(1L)
                                .username("user1")
                                .name("User One")
                                .roles(Set.of(Role.OPERARIO_DE_PRODUCCION.name()))
                                .isActive(true)
                                .build();

                when(userService.updateUserPassword(eq(1L), any(UpdatePasswordDTO.class))).thenReturn(responseDTO);

                mockMvc.perform(patch("/users/1/password")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatePasswordDTO)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @WithMockUser(username = "user1", roles = "OPERARIO_DE_PRODUCCION")
        void testGetUserById_success() throws Exception {
                UserDetailDTO detailDTO = UserDetailDTO.builder()
                                .id(1L)
                                .username("user1")
                                .name("User One")
                                .roles(Set.of(Role.OPERARIO_DE_PRODUCCION.name()))
                                .email("user1@example.com")
                                .phoneNumber("123456789")
                                .isActive(true)
                                .build();

                when(userService.getUserById(1L)).thenReturn(detailDTO);

                mockMvc.perform(get("/users/1")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.username").value("user1"))
                                .andExpect(jsonPath("$.email").value("user1@example.com"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testFindAll_withPagination() throws Exception {
                UserResponseDTO user1 = UserResponseDTO.builder()
                                .id(1L)
                                .username("user1")
                                .name("User One")
                                .roles(Set.of(Role.OPERARIO_DE_ALMACEN.name()))
                                .isActive(true)
                                .build();

                UserResponseDTO user2 = UserResponseDTO.builder()
                                .id(2L)
                                .username("user2")
                                .name("User Two")
                                .roles(Set.of(Role.ADMIN.name()))
                                .isActive(true)
                                .build();

                Page<UserResponseDTO> page = new PageImpl<>(List.of(user1, user2), PageRequest.of(0, 10), 2);
                when(userService.findAll(any(PageRequest.class))).thenReturn(page);

                mockMvc.perform(get("/users")
                                .with(csrf())
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content[0].username").value("user1"))
                                .andExpect(jsonPath("$.totalItems").value(2))
                                .andExpect(jsonPath("$.totalPages").value(1))
                                .andExpect(jsonPath("$.currentPage").value(0));
        }
}
