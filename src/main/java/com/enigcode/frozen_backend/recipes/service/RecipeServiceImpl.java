package com.enigcode.frozen_backend.recipes.service;

import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.enigcode.frozen_backend.products.repository.ProductRepository;
import com.enigcode.frozen_backend.recipes.DTO.RecipeCreateDTO;
import com.enigcode.frozen_backend.recipes.DTO.RecipeResponseDTO;
import com.enigcode.frozen_backend.recipes.DTO.RecipeUpdateDTO;
import com.enigcode.frozen_backend.recipes.mapper.RecipeMapper;
import com.enigcode.frozen_backend.recipes.repository.RecipeRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService{

    final RecipeRepository recipeRepository;
    final MaterialRepository materialRepository;
    final ProductRepository productRepository;
    final RecipeMapper recipeMapper;

    @Override
    @Transactional
    public RecipeResponseDTO createRecipe(RecipeCreateDTO recipeCreateDTO){ 
        return null;
    }



    @Override
    @Transactional
    public RecipeResponseDTO updateRecipe(Long id, RecipeUpdateDTO recipeUpdateDTO){
        return null;
    }



    @Override
    @Transactional
    public RecipeResponseDTO deleteRecipe(Long id){
        return null;
    }



    @Override
    @Transactional
    public RecipeResponseDTO getRecipe(Long id){
        return null;
    }



    @Override
    @Transactional
    public List<RecipeResponseDTO> getRecipeList(){
        return null;
    }
}
