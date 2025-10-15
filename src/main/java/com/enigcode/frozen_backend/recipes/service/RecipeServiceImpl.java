package com.enigcode.frozen_backend.recipes.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import com.enigcode.frozen_backend.product_phases.repository.ProductPhaseRepository;
import com.enigcode.frozen_backend.recipes.DTO.RecipeCreateDTO;
import com.enigcode.frozen_backend.recipes.DTO.RecipeResponseDTO;
import com.enigcode.frozen_backend.recipes.DTO.RecipeUpdateDTO;
import com.enigcode.frozen_backend.recipes.mapper.RecipeMapper;
import com.enigcode.frozen_backend.recipes.repository.RecipeRepository;
import com.enigcode.frozen_backend.recipes.model.Recipe;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {

    final RecipeRepository recipeRepository;
    final MaterialRepository materialRepository;
    final ProductPhaseRepository productPhaseRepository;
    final RecipeMapper recipeMapper;

    @Override
    @Transactional
    public RecipeResponseDTO createRecipe(RecipeCreateDTO recipeCreateDTO) {
        Recipe recipe = recipeMapper.toEntity(recipeCreateDTO);
        recipe.setCreationDate(OffsetDateTime.now());

        ProductPhase productPhase = productPhaseRepository.findById(recipeCreateDTO.getProductPhaseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ProductPhase no encontrada" + recipeCreateDTO.getProductPhaseId()));
        Material material = materialRepository.findById(recipeCreateDTO.getMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Material no encontrado" + recipeCreateDTO.getMaterialId()));

        if (!productPhase.getRequiredMaterials().contains(material.getType())
                && !material.getType().equals(MaterialType.OTROS))
            throw new BadRequestException("El tipo de material " + material.getType() + " no esta permitido en la fase "
                    + productPhase.getPhase());

        recipe.setProductPhase(productPhase);
        recipe.setMaterial(material);

        Recipe savedRecipe = recipeRepository.saveAndFlush(recipe);

        return recipeMapper.toResponseDTO(savedRecipe);
    }

    @Override
    @Transactional
    public RecipeResponseDTO updateRecipe(Long id, RecipeUpdateDTO recipeUpdateDTO) {
        return null;
    }

    @Override
    @Transactional
    public RecipeResponseDTO deleteRecipe(Long id) {
        return null;
    }

    @Override
    @Transactional
    public RecipeResponseDTO getRecipe(Long id) {
        return null;
    }

    @Override
    @Transactional
    public List<RecipeResponseDTO> getRecipeList() {
        return null;
    }
}
