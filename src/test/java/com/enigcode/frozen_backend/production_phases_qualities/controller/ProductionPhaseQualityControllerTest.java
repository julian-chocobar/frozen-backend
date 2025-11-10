package com.enigcode.frozen_backend.production_phases_qualities.controller;

import com.enigcode.frozen_backend.common.exceptions_configs.GlobalExceptionHandler;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.production_phases_qualities.DTO.ProductionPhaseQualityCreateDTO;
import com.enigcode.frozen_backend.production_phases_qualities.DTO.ProductionPhaseQualityResponseDTO;
import com.enigcode.frozen_backend.production_phases_qualities.DTO.ProductionPhaseQualityUpdateDTO;
import com.enigcode.frozen_backend.production_phases_qualities.service.ProductionPhaseQualityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductionPhaseQualityController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ProductionPhaseQualityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductionPhaseQualityService service;

    @MockBean
    private com.enigcode.frozen_backend.common.SecurityProperties securityProperties;

    @Autowired
    private ObjectMapper objectMapper;

    private String createJson;
    private ProductionPhaseQualityResponseDTO responseDTO;

    @BeforeEach
    void setup() throws Exception {
        ProductionPhaseQualityCreateDTO createDTO = ProductionPhaseQualityCreateDTO.builder()
                .qualityParameterId(2L)
                .productionPhaseId(1L)
                .value("OK")
                .isApproved(true)
                .build();
        createJson = objectMapper.writeValueAsString(createDTO);

        responseDTO = ProductionPhaseQualityResponseDTO.builder()
                .id(10L)
                .qualityParameterName("pH")
                .productionPhaseId(1L)
                .productionPhase(Phase.MOLIENDA)
                .value("OK")
                .isApproved(true)
                .build();

        when(service.createProductionPhaseQuality(any())).thenReturn(responseDTO);
        when(service.updateProductionPhaseQuality(eq(10L), any())).thenReturn(responseDTO);
        when(service.getProductionPhaseQuality(eq(10L))).thenReturn(responseDTO);
        when(service.getProductionPhaseQualityByPhase(eq(1L))).thenReturn(List.of(responseDTO));
        when(service.getProductionPhaseQualityByBatch(eq(100L))).thenReturn(List.of(responseDTO));
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        mockMvc.perform(post("/production-phases-qualities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.productionPhase").value("MOLIENDA"))
                .andExpect(jsonPath("$.qualityParameterName").value("pH"));
    }

    @Test
    void create_missingFields_returns400() throws Exception {
        String invalidJson = """
        {
          "qualityParameterId": 2,
          "value": "OK",
          "isApproved": true
        }
        """;
        mockMvc.perform(post("/production-phases-qualities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_validRequest_returns200() throws Exception {
        ProductionPhaseQualityUpdateDTO updateDTO = ProductionPhaseQualityUpdateDTO.builder()
                .value("Nuevo")
                .isApproved(false)
                .build();
        String updateJson = objectMapper.writeValueAsString(updateDTO);

        mockMvc.perform(patch("/production-phases-qualities/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        when(service.updateProductionPhaseQuality(eq(999L), any()))
                .thenThrow(new ResourceNotFoundException("No se encontró fase de calidad con id 999"));

        String updateJson = """
        {"value":"Nuevo"}
        """;

        mockMvc.perform(patch("/production-phases-qualities/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_returns200() throws Exception {
        mockMvc.perform(get("/production-phases-qualities/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(service.getProductionPhaseQuality(999L)).thenThrow(new ResourceNotFoundException("no existe"));
        mockMvc.perform(get("/production-phases-qualities/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByPhase_returns200_andArray() throws Exception {
        mockMvc.perform(get("/production-phases-qualities/by-phase/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getByBatch_returns200_andArray() throws Exception {
        mockMvc.perform(get("/production-phases-qualities/by-batch/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
