package com.enigcode.frozen_backend.recipes.controller;

import com.enigcode.frozen_backend.recipes.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("recipes")
@RequiredArgsConstructor
public class RecipeController {

    final RecipeService recipeService;
}
