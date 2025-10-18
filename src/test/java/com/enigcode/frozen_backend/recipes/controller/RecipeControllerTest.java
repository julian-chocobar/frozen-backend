package com.enigcode.frozen_backend.recipes.controller;

import com.enigcode.frozen_backend.recipes.DTO.RecipeCreateDTO;
import com.enigcode.frozen_backend.recipes.DTO.RecipeResponseDTO;
import com.enigcode.frozen_backend.recipes.DTO.RecipeUpdateDTO;
import com.enigcode.frozen_backend.recipes.service.RecipeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
// Security filters disabled for controller behavior tests

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecipeController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecipeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private RecipeService recipeService;

    @Test
    @DisplayName("POST /recipes - success")
    void createRecipe_success() throws Exception {
    RecipeCreateDTO createDTO = RecipeCreateDTO.builder()
        .productPhaseId(1L)
        .materialId(2L)
        .quantity(5.0)
        .build();
        RecipeResponseDTO responseDTO = new RecipeResponseDTO();
        Mockito.when(recipeService.createRecipe(any())).thenReturn(responseDTO);
        mockMvc.perform(post("/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("PATCH /recipes/{id} - success")
    void updateRecipe_success() throws Exception {
        RecipeUpdateDTO updateDTO = new RecipeUpdateDTO();
        RecipeResponseDTO responseDTO = new RecipeResponseDTO();
        Mockito.when(recipeService.updateRecipe(eq(1L), any())).thenReturn(responseDTO);
        mockMvc.perform(patch("/recipes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("DELETE /recipes/{id} - success")
    void deleteRecipe_success() throws Exception {
        RecipeResponseDTO responseDTO = new RecipeResponseDTO();
        Mockito.when(recipeService.deleteRecipe(1L)).thenReturn(responseDTO);
        mockMvc.perform(delete("/recipes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /recipes/{id} - success")
    void getRecipe_success() throws Exception {
        RecipeResponseDTO responseDTO = new RecipeResponseDTO();
        Mockito.when(recipeService.getRecipe(1L)).thenReturn(responseDTO);
        mockMvc.perform(get("/recipes/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /recipes/list - success")
    void getRecipeList_success() throws Exception {
        List<RecipeResponseDTO> list = List.of(new RecipeResponseDTO());
        Mockito.when(recipeService.getRecipeList()).thenReturn(list);
        mockMvc.perform(get("/recipes/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    // Error cases: validation, not found, business errors can be added as needed
}
