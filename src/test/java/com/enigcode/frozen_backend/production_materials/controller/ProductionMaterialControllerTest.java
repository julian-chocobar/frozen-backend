package com.enigcode.frozen_backend.production_materials.controller;

import com.enigcode.frozen_backend.common.exceptions_configs.GlobalExceptionHandler;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.production_materials.DTO.ProductionMaterialResponseDTO;
import com.enigcode.frozen_backend.production_materials.service.ProductionMaterialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductionMaterialController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ProductionMaterialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductionMaterialService service;

    @MockitoBean
    private com.enigcode.frozen_backend.common.SecurityProperties securityProperties;

    private ProductionMaterialResponseDTO dto;

    @BeforeEach
    void setup() {
        dto = ProductionMaterialResponseDTO.builder()
                .id(5L)
                .materialId(11L)
                .materialCode("MAT-001")
                .productionPhaseId(22L)
                .quantity(12.5)
                .build();

        when(service.getProductionMaterial(5L)).thenReturn(dto);
        when(service.getProductionMaterialByPhase(22L)).thenReturn(List.of(dto));
        when(service.getProductionMaterialByBatch(10L)).thenReturn(List.of(dto));
    }

    @Test
    void getProductionMaterial_returns200() throws Exception {
        mockMvc.perform(get("/production-materials/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.materialCode").value("MAT-001"));
    }

    @Test
    void getProductionMaterial_notFound_returns404() throws Exception {
        when(service.getProductionMaterial(999L)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/production-materials/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByPhase_returns200_andArray() throws Exception {
        mockMvc.perform(get("/production-materials/by-production-phase/22"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].productionPhaseId").value(22L));
    }

    @Test
    void getByBatch_returns200_andArray() throws Exception {
        mockMvc.perform(get("/production-materials/by-batch/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].materialId").value(11L));
    }
}
