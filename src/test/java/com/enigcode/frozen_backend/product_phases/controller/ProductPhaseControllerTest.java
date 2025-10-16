package com.enigcode.frozen_backend.product_phases.controller;

import com.enigcode.frozen_backend.product_phases.service.ProductPhaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductPhaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductPhaseService productPhaseService;

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
        mockMvc.perform(patch("/product-phases/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());
    }

    @Test
    void testGetProductPhases() throws Exception {
        mockMvc.perform(get("/product-phases"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetProductPhase() throws Exception {
        mockMvc.perform(get("/product-phases/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetProductPhasesByProduct() throws Exception {
        mockMvc.perform(get("/product-phases/by-product/1"))
                .andExpect(status().isOk());
    }
}
