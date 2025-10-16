package com.enigcode.frozen_backend.materials.controller;

import com.enigcode.frozen_backend.materials.service.MaterialService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc

class MaterialControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private MaterialService materialService;
    private String materialJson;

    @BeforeEach
    void setup() throws Exception {
        materialJson = """ 
        { 
            "name": "Malta",
            "quantity": 10 
        } 
"""; 
    } 
    
    @Test
    void testCreateMaterial() throws Exception {
        mockMvc.perform(post("/materials")
                .contentType(MediaType.APPLICATION_JSON)
                .content(materialJson))
                .andExpect(status().isCreated());
    }

    @Test
    void testUpdateMaterial() throws Exception {
        String updateJson = """ 
        { 
            "quantity": 20 
        } 
        """;
        mockMvc.perform(patch("/materials/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)).andExpect(status().isOk());
    }

    @Test
    void testToggleActive() throws Exception {
        mockMvc.perform(patch("/materials/1/toggle-active"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMaterials() throws Exception {
        mockMvc.perform(get("/materials")).andExpect(status().isOk());
    }

    @Test
    void testGetMaterial() throws Exception {
        mockMvc.perform(get("/materials/1")).andExpect(status().isOk());
    }
}
