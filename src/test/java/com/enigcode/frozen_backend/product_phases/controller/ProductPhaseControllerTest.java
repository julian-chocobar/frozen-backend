package com.enigcode.frozen_backend.product_phases.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductPhaseController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductPhaseControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private com.enigcode.frozen_backend.product_phases.service.ProductPhaseService productPhaseService;
    @MockitoBean
    private com.enigcode.frozen_backend.common.SecurityProperties securityProperties;

    private String updateJson;

    @BeforeEach
    void setup() {
        updateJson = """
                {
                    "description": "Nueva fase actualizada"
                }
                """;
    }

    @Test
    void testUpdateProductPhase() throws Exception {
    org.mockito.Mockito.when(productPhaseService.updateProductPhase(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
        .thenReturn(new com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseResponseDTO());
        mockMvc.perform(patch("/product-phases/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());
    }

    @Test
    void testGetProductPhases() throws Exception {
        org.mockito.Mockito.when(productPhaseService.findAll(org.mockito.ArgumentMatchers.any())).thenReturn(org.springframework.data.domain.Page.empty());
        mockMvc.perform(get("/product-phases"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetProductPhase() throws Exception {
        org.mockito.Mockito.when(productPhaseService.getProductPhase(1L)).thenReturn(new com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseResponseDTO());
        mockMvc.perform(get("/product-phases/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetProductPhasesByProduct() throws Exception {
        org.mockito.Mockito.when(productPhaseService.getByProduct(1L)).thenReturn(java.util.List.of());
        mockMvc.perform(get("/product-phases/by-product/1"))
                .andExpect(status().isOk());
    }
}
