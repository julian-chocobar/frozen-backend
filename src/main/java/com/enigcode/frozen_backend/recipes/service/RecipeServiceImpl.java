package com.enigcode.frozen_backend.recipes.service;

import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.enigcode.frozen_backend.products.repository.ProductRepository;
import com.enigcode.frozen_backend.recipes.mapper.RecipeMapper;
import com.enigcode.frozen_backend.recipes.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService{

    final RecipeRepository recipeRepository;
    final MaterialRepository materialRepository;
    final ProductRepository productRepository;
    final RecipeMapper recipeMapper;
}
