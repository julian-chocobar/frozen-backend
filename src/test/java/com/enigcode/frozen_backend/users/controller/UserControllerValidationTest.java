package com.enigcode.frozen_backend.users.controller;

import com.enigcode.frozen_backend.users.DTO.UpdatePasswordDTO;
import com.enigcode.frozen_backend.users.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import com.enigcode.frozen_backend.common.exceptions_configs.GlobalExceptionHandler;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class))
@AutoConfigureMockMvc(addFilters = false)
class UserControllerValidationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private UserService userService;

        @MockitoBean
        private com.enigcode.frozen_backend.common.SecurityProperties securityProperties;

    // No repository needed for validation-only tests

        @Test
        @WithMockUser(roles = "ADMIN")
        void testCreateUser_missingUsername_returns400() throws Exception {
                String invalidJson = """
                                {
                                    "password": "password123",
                                    "name": "New User",
                                    "roles": ["OPERARIO_DE_CALIDAD"]
                                }
                                """;

                mockMvc.perform(post("/users")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testCreateUser_missingPassword_returns400() throws Exception {
                String invalidJson = """
                                {
                                    "username": "newuser",
                                    "name": "New User",
                                    "roles": ["OPERARIO_DE_CALIDAD"]
                                }
                                """;

                mockMvc.perform(post("/users")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testCreateUser_missingRole_returns400() throws Exception {
                String invalidJson = """
                                {
                                    "username": "newuser",
                                    "password": "password123",
                                    "name": "New User"
                                }
                                """;

                mockMvc.perform(post("/users")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson))
                                .andExpect(status().isBadRequest());
        }

        @Test
    @WithMockUser(username = "user1", roles = "OPERARIO_DE_PRODUCCION")
        void testUpdateUserPassword_mismatch_returns400() throws Exception {
                UpdatePasswordDTO updatePasswordDTO = UpdatePasswordDTO.builder()
                                .password("NewPassword123")
                                .passwordConfirmacion("DifferentPass123")
                                .build();

                mockMvc.perform(patch("/users/1/password")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatePasswordDTO)))
                                .andExpect(status().isBadRequest());
        }
}
