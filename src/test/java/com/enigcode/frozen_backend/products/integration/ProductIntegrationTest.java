package com.enigcode.frozen_backend.products.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER = "test";
    private static final String PASS = "test";

    @Test
    @DisplayName("Create, get, list, update, toggle product - happy path")
    void productCrudHappyPath() throws Exception {
        String createJson = "{" +
                "\"name\":\"Cerveza Rubia\"," +
                "\"isAlcoholic\":true" +
                "}";

        String createResponse = mockMvc.perform(post("/products")
                        .with(httpBasic(USER, PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Cerveza Rubia"))
                .andExpect(jsonPath("$.isAlcoholic").value(true))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.isReady").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/products")
                        .with(httpBasic(USER, PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").exists());

        // No explicit GET /products/{id} endpoint is defined in ProductController,
        // so we validate via list and subsequent operations instead.

        String updateJson = "{" +
                "\"name\":\"Cerveza Roja\"" +
                "}";

        mockMvc.perform(patch("/products/{id}", id)
                        .with(httpBasic(USER, PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Cerveza Roja"));

        mockMvc.perform(patch("/products/{id}/toggle-active", id)
                        .with(httpBasic(USER, PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }
}
