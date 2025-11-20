package com.enigcode.frozen_backend.product_phases.controller;

import com.enigcode.frozen_backend.product_phases.service.ProductPhaseService;
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

@WebMvcTest(ProductPhaseController.class)
class ProductPhaseControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductPhaseService productPhaseService;
    @MockitoBean
    private com.enigcode.frozen_backend.common.SecurityProperties securityProperties;

    @Test
    @DisplayName("GET /product-phases without auth -> 401")
    void getProductPhases_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/product-phases"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /product-phases with auth -> 200")
    void getProductPhases_authenticated_returns200() throws Exception {
        Mockito.when(productPhaseService.findAll(Mockito.any())).thenReturn(Page.empty());
        mockMvc.perform(get("/product-phases"))
                .andExpect(status().isOk());
    }
}
