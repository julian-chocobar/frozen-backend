package com.enigcode.frozen_backend.product_phases.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductPhaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER = "test";
    private static final String PASS = "test";

    @Test
    @DisplayName("Product phases happy path: create product -> list phases -> update phase -> get phase -> mark-ready")
    void productPhasesHappyPath() throws Exception {
        // 1) Create a product to seed its phases
        String createProductJson = "{" +
                "\"name\":\"IPA Fases\"," +
                "\"isAlcoholic\":true" +
                "}";

        String productResponse = mockMvc.perform(post("/products")
                        .with(httpBasic(USER, PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createProductJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long productId = objectMapper.readTree(productResponse).get("id").asLong();

        // 2) Fetch phases by product and pick one with no required materials (FILTRACION)
        String byProductJson = mockMvc.perform(get("/product-phases/by-product/{productId}", productId)
                        .with(httpBasic(USER, PASS)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode phasesArray = objectMapper.readTree(byProductJson);
        assertThat(phasesArray.isArray()).isTrue();
        assertThat(phasesArray.size()).isGreaterThan(0);

        Long phaseId = null;
        for (JsonNode node : phasesArray) {
            if (node.hasNonNull("phase") && "FILTRACION".equals(node.get("phase").asText())) {
                phaseId = node.get("id").asLong();
                break;
            }
        }
        if (phaseId == null) {
            // Fallback to first phase if FILTRACION isn't present for any reason
            phaseId = phasesArray.get(0).get("id").asLong();
        }

        // 3) Update the chosen phase to make it complete
        String updatePhaseJson = "{" +
                "\"input\": 1.5," +
                "\"output\": 1.0," +
                "\"outputUnit\": \"KG\"," +
                "\"estimatedHours\": 2.0" +
                "}";

        mockMvc.perform(patch("/product-phases/{id}", phaseId)
                        .with(httpBasic(USER, PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePhaseJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.input").value(1.5))
                .andExpect(jsonPath("$.output").value(1.0))
                .andExpect(jsonPath("$.outputUnit").value("KG"))
                .andExpect(jsonPath("$.estimatedHours").value(2.0));

        // 4) Get the phase by id and verify fields
        mockMvc.perform(get("/product-phases/{id}", phaseId)
                        .with(httpBasic(USER, PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(phaseId))
                .andExpect(jsonPath("$.input").value(1.5))
                .andExpect(jsonPath("$.output").value(1.0))
                .andExpect(jsonPath("$.outputUnit").value("KG"))
                .andExpect(jsonPath("$.estimatedHours").value(2.0));

        // 5) Mark the phase as ready (happy path)
        mockMvc.perform(patch("/product-phases/{id}/mark-ready", phaseId)
                        .with(httpBasic(USER, PASS)))
                .andExpect(status().isOk());
    }
}
