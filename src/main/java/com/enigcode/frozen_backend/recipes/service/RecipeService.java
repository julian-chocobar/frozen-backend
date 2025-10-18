package com.enigcode.frozen_backend.recipes.service;

import java.util.List;

import com.enigcode.frozen_backend.recipes.DTO.RecipeCreateDTO;
import com.enigcode.frozen_backend.recipes.DTO.RecipeResponseDTO;
import com.enigcode.frozen_backend.recipes.DTO.RecipeUpdateDTO;
import com.enigcode.frozen_backend.recipes.model.Recipe;

public interface RecipeService {
    RecipeResponseDTO createRecipe(RecipeCreateDTO recipeCreateDTO);
    RecipeResponseDTO updateRecipe(Long id, RecipeUpdateDTO recipeUpdateDTO);
    RecipeResponseDTO deleteRecipe(Long id);
    RecipeResponseDTO getRecipe(Long id);
    List<RecipeResponseDTO> getRecipeList();
    List<RecipeResponseDTO> getMaterialByPhase(Long id);
    List<RecipeResponseDTO> getMaterialByProduct(Long id);
    List<Recipe> getRecipeByProduct(Long id);
}
