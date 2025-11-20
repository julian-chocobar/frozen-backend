package com.enigcode.frozen_backend.movements.controller;

import com.enigcode.frozen_backend.movements.service.MovementService;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovementController.class)
class MovementControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovementService movementService;
    @MockitoBean
    private com.enigcode.frozen_backend.common.SecurityProperties securityProperties;

    @Test
    @DisplayName("GET /movements without auth -> 401")
    void getMovements_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/movements"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /movements with auth -> 200")
    void getMovements_authenticated_returns200() throws Exception {
        Mockito.when(movementService.findAll(Mockito.any(), Mockito.any())).thenReturn(Page.empty());
        mockMvc.perform(get("/movements"))
                .andExpect(status().isOk());
    }
}
