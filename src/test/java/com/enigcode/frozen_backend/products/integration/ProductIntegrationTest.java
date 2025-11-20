package com.enigcode.frozen_backend.products.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "SUPERVISOR_DE_PRODUCCION")
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("unused")
    private static final String USER = "test";
    @SuppressWarnings("unused")
    private static final String PASS = "test";

    @Test
    @DisplayName("Create, get, list, update, toggle product - happy path")
    void productCrudHappyPath() throws Exception {
        String createJson = "{" +
                "\"name\":\"Cerveza Rubia\"," +
                "\"isAlcoholic\":true," +
                "\"standardQuantity\":1.0," +
                "\"unitMeasurement\":\"UNIDAD\"" +
                "}";

        String createResponse = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Cerveza Rubia"))
                .andExpect(jsonPath("$.isAlcoholic").value(true))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.isReady").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").exists());

        // No explicit GET /products/{id} endpoint is defined in ProductController,
        // so we validate via list and subsequent operations instead.

        String updateJson = "{" +
                "\"name\":\"Cerveza Roja\"" +
                "}";

        mockMvc.perform(patch("/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Cerveza Roja"));

        mockMvc.perform(patch("/products/{id}/toggle-active", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }
}

