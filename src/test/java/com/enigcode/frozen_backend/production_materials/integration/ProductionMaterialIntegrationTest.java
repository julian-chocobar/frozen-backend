package com.enigcode.frozen_backend.production_materials.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductionMaterialIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void getProductionMaterial_notFound_returns404() throws Exception {
        mockMvc.perform(get("/production-materials/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getProductionMaterialByPhase_emptyOrNotFound_returns200Or404() throws Exception {
        try {
            mockMvc.perform(get("/production-materials/by-production-phase/999999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        } catch (AssertionError ae) {
            mockMvc.perform(get("/production-materials/by-production-phase/999999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @WithMockUser
    void getProductionMaterialByBatch_emptyOrNotFound_returns200Or404() throws Exception {
        try {
            mockMvc.perform(get("/production-materials/by-batch/999999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        } catch (AssertionError ae) {
            mockMvc.perform(get("/production-materials/by-batch/999999"))
                    .andExpect(status().isNotFound());
        }
    }
}
