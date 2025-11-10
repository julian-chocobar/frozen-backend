package com.enigcode.frozen_backend.production_phases_qualities.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProductionPhaseQuality module.
 * Simplified to test endpoint availability and error handling.
 * Full CRUD flows are covered by service/controller unit tests since we can't create
 * ProductionPhase entities without additional infrastructure.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductionPhaseQualityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = {"OPERARIO_DE_CALIDAD","SUPERVISOR_DE_CALIDAD"})
    @DisplayName("Get ProductionPhaseQuality not found -> 404")
    void getProductionPhaseQuality_notFound() throws Exception {
        mockMvc.perform(get("/production-phases-qualities/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"OPERARIO_DE_CALIDAD","SUPERVISOR_DE_CALIDAD"})
    @DisplayName("Update ProductionPhaseQuality not found -> 404")
    void updateProductionPhaseQuality_notFound() throws Exception {
        String patchBody = """
        { "value": "X" }
        """;
        mockMvc.perform(patch("/production-phases-qualities/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchBody))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"OPERARIO_DE_CALIDAD","SUPERVISOR_DE_CALIDAD"})
    @DisplayName("List by phase with invalid ID returns empty or 404")
    void listByPhase_invalidPhase() throws Exception {
        // Returns 404 because service validates phase existence first
        mockMvc.perform(get("/production-phases-qualities/by-phase/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"OPERARIO_DE_CALIDAD","SUPERVISOR_DE_CALIDAD"})
    @DisplayName("List by batch with invalid ID returns empty or 404")
    void listByBatch_invalidBatch() throws Exception {
        // Returns 404 because service validates batch existence first
        mockMvc.perform(get("/production-phases-qualities/by-batch/999999"))
                .andExpect(status().isNotFound());
    }
}
