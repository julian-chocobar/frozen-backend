package com.enigcode.frozen_backend.recipes.controller;

import com.enigcode.frozen_backend.recipes.DTO.RecipeResponseDTO;
import com.enigcode.frozen_backend.recipes.service.RecipeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecipeController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecipeControllerByProductTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecipeService recipeService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        RecipeService recipeService() {
            return Mockito.mock(RecipeService.class);
        }

        @Bean
        com.enigcode.frozen_backend.common.SecurityProperties securityProperties() {
            return Mockito.mock(com.enigcode.frozen_backend.common.SecurityProperties.class);
        }
    }

    @Test
    @DisplayName("GET /recipes/by-product-phase/{id} returns list")
    void getByProductPhase_returnsList() throws Exception {
        Mockito.when(recipeService.getMaterialByPhase(1L)).thenReturn(List.of(new RecipeResponseDTO()));
        mockMvc.perform(get("/recipes/by-product-phase/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /recipes/by-product/{id} returns list")
    void getByProduct_returnsList() throws Exception {
        Mockito.when(recipeService.getMaterialByProduct(2L)).thenReturn(List.of(new RecipeResponseDTO()));
        mockMvc.perform(get("/recipes/by-product/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
