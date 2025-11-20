package com.enigcode.frozen_backend.materials.controller;

import com.enigcode.frozen_backend.materials.service.MaterialService;
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

@WebMvcTest(MaterialController.class)
class MaterialControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MaterialService materialService;
    @MockitoBean
    private com.enigcode.frozen_backend.common.SecurityProperties securityProperties;

    @Test
    @DisplayName("GET /materials without auth -> 401")
    void getMaterials_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/materials"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /materials with auth -> 200")
    void getMaterials_authenticated_returns200() throws Exception {
        Mockito.when(materialService.findAll(Mockito.any(), Mockito.any())).thenReturn(Page.empty());
        mockMvc.perform(get("/materials"))
                .andExpect(status().isOk());
    }
}
