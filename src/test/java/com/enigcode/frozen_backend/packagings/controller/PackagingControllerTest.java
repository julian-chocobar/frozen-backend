package com.enigcode.frozen_backend.packagings.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PackagingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String packagingJson;

    @BeforeEach
    void setup() {
        packagingJson = """
                {
                    "name": "Caja de cartón",
                    "capacity": 20
                }
                """;
    }

    @Test
    void testCreatePackaging() throws Exception {
        mockMvc.perform(post("/packagings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(packagingJson))
                .andExpect(status().isCreated());
    }

    @Test
    void testUpdatePackaging() throws Exception {
        String updateJson = """
                {
                    "capacity": 25
                }
                """;

        mockMvc.perform(patch("/packagings/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());
    }

    @Test
    void testToggleActive() throws Exception {
        mockMvc.perform(patch("/packagings/1/toggle-active"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetPackaging() throws Exception {
        mockMvc.perform(get("/packagings/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetPackagings() throws Exception {
        mockMvc.perform(get("/packagings"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetPackagingList() throws Exception {
        mockMvc.perform(get("/packagings/list"))
                .andExpect(status().isOk());
    }
}
