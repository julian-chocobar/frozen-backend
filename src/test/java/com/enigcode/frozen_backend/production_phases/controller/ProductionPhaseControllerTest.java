package com.enigcode.frozen_backend.production_phases.controller;

import com.enigcode.frozen_backend.common.security.WithMockCustomUser;
import com.enigcode.frozen_backend.production_phases.DTO.ProductionPhaseResponseDTO;
import com.enigcode.frozen_backend.production_phases.DTO.ProductionPhaseUnderReviewDTO;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhaseStatus;
import com.enigcode.frozen_backend.production_phases.service.ProductionPhaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductionPhaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductionPhaseService productionPhaseService;

    @MockBean
    private com.enigcode.frozen_backend.common.security.SecurityService securityService;

    private ProductionPhaseResponseDTO responseDTO;
    private ProductionPhaseUnderReviewDTO underReviewDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new ProductionPhaseResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setStatus(ProductionPhaseStatus.BAJO_REVISION);
        responseDTO.setBatchId(1L);
        responseDTO.setBatchCode("BATCH-001");

        underReviewDTO = ProductionPhaseUnderReviewDTO.builder()
                .input(100.0)
                .output(95.0)
                .build();
    }

    @Test
        @WithMockCustomUser(roles = "SUPERVISOR_DE_PRODUCCION")
    void testSetUnderReview_Success() throws Exception {
        when(securityService.isSupervisorOfPhase(any(), eq(1L))).thenReturn(true);
        when(productionPhaseService.setUnderReview(eq(1L), any(ProductionPhaseUnderReviewDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(patch("/production-phases/set-under-review/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(underReviewDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("BAJO_REVISION"));
    }

    @Test
        @WithMockCustomUser(roles = "SUPERVISOR_DE_PRODUCCION")
    void testSetUnderReview_MissingInput_Returns400() throws Exception {
        when(securityService.isSupervisorOfPhase(any(), eq(1L))).thenReturn(true);
        ProductionPhaseUnderReviewDTO invalidDTO = ProductionPhaseUnderReviewDTO.builder()
                .output(95.0)
                .build();

        mockMvc.perform(patch("/production-phases/set-under-review/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
        @WithMockCustomUser(roles = "SUPERVISOR_DE_PRODUCCION")
    void testSetUnderReview_MissingOutput_Returns400() throws Exception {
        when(securityService.isSupervisorOfPhase(any(), eq(1L))).thenReturn(true);
        ProductionPhaseUnderReviewDTO invalidDTO = ProductionPhaseUnderReviewDTO.builder()
                .input(100.0)
                .build();

        mockMvc.perform(patch("/production-phases/set-under-review/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
        @WithMockCustomUser(roles = "SUPERVISOR_DE_PRODUCCION")
    void testSetUnderReview_NegativeInput_Returns400() throws Exception {
        when(securityService.isSupervisorOfPhase(any(), eq(1L))).thenReturn(true);
        ProductionPhaseUnderReviewDTO invalidDTO = ProductionPhaseUnderReviewDTO.builder()
                .input(-10.0)
                .output(95.0)
                .build();

        mockMvc.perform(patch("/production-phases/set-under-review/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
        @WithMockCustomUser(roles = "SUPERVISOR_DE_CALIDAD")
    void testReviewProductionPhase_Success() throws Exception {
        when(productionPhaseService.reviewProductionPhase(1L)).thenReturn(responseDTO);

        mockMvc.perform(patch("/production-phases/review/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("BAJO_REVISION"));
    }

    @Test
        @WithMockCustomUser
    void testGetProductionPhase_Success() throws Exception {
        when(productionPhaseService.getProductionPhase(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/production-phases/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.batchCode").value("BATCH-001"));
    }

    @Test
        @WithMockCustomUser
    void testGetProductionPhasesByBatch_Success() throws Exception {
        List<ProductionPhaseResponseDTO> phases = List.of(responseDTO);
        when(productionPhaseService.getProductionPhasesByBatch(1L)).thenReturn(phases);

        mockMvc.perform(get("/production-phases/by-batch/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].batchId").value(1));
    }

    @Test
        @WithMockCustomUser
    void testGetProductionPhasesByBatch_EmptyList() throws Exception {
        when(productionPhaseService.getProductionPhasesByBatch(999L)).thenReturn(List.of());

        mockMvc.perform(get("/production-phases/by-batch/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
