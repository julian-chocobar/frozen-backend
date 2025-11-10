package com.enigcode.frozen_backend.production_phases.integration;

import com.enigcode.frozen_backend.common.security.WithMockCustomUser;
import com.enigcode.frozen_backend.production_phases.DTO.ProductionPhaseUnderReviewDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductionPhaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.enigcode.frozen_backend.common.security.SecurityService securityService;

    private ProductionPhaseUnderReviewDTO underReviewDTO;

    @BeforeEach
    void setUp() {
        underReviewDTO = ProductionPhaseUnderReviewDTO.builder()
                .input(100.0)
                .output(95.0)
                .build();
        // Permitir pasar el @PreAuthorize para alcanzar la capa de servicio/controlador
        when(securityService.isSupervisorOfPhase(any(), anyLong())).thenReturn(true);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        com.enigcode.frozen_backend.common.security.SecurityService securityService() {
            return org.mockito.Mockito.mock(com.enigcode.frozen_backend.common.security.SecurityService.class);
        }
    }

    @Test
        @WithMockCustomUser
    void testGetProductionPhase_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/production-phases/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
        @WithMockCustomUser
    void testGetProductionPhasesByBatch_NotFound_Returns404() throws Exception {
        // Asumiendo que batch 99999 no existe o no tiene production phases
        mockMvc.perform(get("/production-phases/by-batch/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
        @WithMockCustomUser(roles = "SUPERVISOR_DE_PRODUCCION")
    void testSetUnderReview_NotFound_Returns404() throws Exception {
        mockMvc.perform(patch("/production-phases/set-under-review/99999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(underReviewDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
        @WithMockCustomUser(roles = "SUPERVISOR_DE_CALIDAD")
    void testReviewProductionPhase_NotFound_Returns404() throws Exception {
        mockMvc.perform(patch("/production-phases/review/99999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
        @WithMockCustomUser(roles = "SUPERVISOR_DE_PRODUCCION")
    void testSetUnderReview_InvalidInput_Returns400() throws Exception {
        ProductionPhaseUnderReviewDTO invalidDTO = ProductionPhaseUnderReviewDTO.builder()
                .input(-10.0) // Valor negativo inválido
                .output(95.0)
                .build();

        mockMvc.perform(patch("/production-phases/set-under-review/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }
}
