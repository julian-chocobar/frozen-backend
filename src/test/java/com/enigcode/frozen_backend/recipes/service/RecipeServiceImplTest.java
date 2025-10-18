package com.enigcode.frozen_backend.recipes.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.product_phases.repository.ProductPhaseRepository;
import com.enigcode.frozen_backend.product_phases.service.ProductPhaseService;
import com.enigcode.frozen_backend.recipes.DTO.RecipeCreateDTO;
import com.enigcode.frozen_backend.recipes.DTO.RecipeResponseDTO;
import com.enigcode.frozen_backend.recipes.DTO.RecipeUpdateDTO;
import com.enigcode.frozen_backend.recipes.mapper.RecipeMapper;
import com.enigcode.frozen_backend.recipes.model.Recipe;
import com.enigcode.frozen_backend.recipes.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RecipeServiceImplTest {
    @Mock RecipeRepository recipeRepository;
    @Mock MaterialRepository materialRepository;
    @Mock ProductPhaseRepository productPhaseRepository;
    @Mock ProductPhaseService productPhaseService;
    @Mock RecipeMapper recipeMapper;
    @InjectMocks RecipeServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createRecipe_success() {
        RecipeCreateDTO dto = RecipeCreateDTO.builder().productPhaseId(1L).materialId(2L).quantity(5.0).build();
        Recipe recipe = new Recipe();
    ProductPhase phase = ProductPhase.builder().id(1L).phase(Phase.MOLIENDA).build(); // MOLIENDA requiere MALTA
        Material material = Material.builder().id(2L).type(MaterialType.MALTA).build();
        Recipe saved = new Recipe();
        RecipeResponseDTO response = new RecipeResponseDTO();

        when(recipeMapper.toEntity(dto)).thenReturn(recipe);
        when(productPhaseRepository.findById(1L)).thenReturn(Optional.of(phase));
        when(materialRepository.findById(2L)).thenReturn(Optional.of(material));
        when(recipeRepository.saveAndFlush(any())).thenReturn(saved);
        when(recipeMapper.toResponseDTO(saved)).thenReturn(response);

        RecipeResponseDTO result = service.createRecipe(dto);
        assertSame(response, result);
        verify(recipeRepository).saveAndFlush(any());
    }

    @Test
    void createRecipe_materialNotAllowed_throwsBadRequest() {
        RecipeCreateDTO dto = RecipeCreateDTO.builder().productPhaseId(1L).materialId(2L).quantity(5.0).build();
        Recipe recipe = new Recipe();
    ProductPhase phase = ProductPhase.builder().id(1L).phase(Phase.COCCION).build(); // COCCION requiere AGUA, LUPULO
        Material material = Material.builder().id(2L).type(MaterialType.MALTA).build();

        when(recipeMapper.toEntity(dto)).thenReturn(recipe);
        when(productPhaseRepository.findById(1L)).thenReturn(Optional.of(phase));
        when(materialRepository.findById(2L)).thenReturn(Optional.of(material));

        assertThrows(BadRequestException.class, () -> service.createRecipe(dto));
    }

    @Test
    void createRecipe_productPhaseNotFound_throwsNotFound() {
        RecipeCreateDTO dto = RecipeCreateDTO.builder().productPhaseId(1L).materialId(2L).quantity(5.0).build();
        when(recipeMapper.toEntity(dto)).thenReturn(new Recipe());
        when(productPhaseRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.createRecipe(dto));
    }

    @Test
    void createRecipe_materialNotFound_throwsNotFound() {
        RecipeCreateDTO dto = RecipeCreateDTO.builder().productPhaseId(1L).materialId(2L).quantity(5.0).build();
        when(recipeMapper.toEntity(dto)).thenReturn(new Recipe());
    ProductPhase phase = ProductPhase.builder().id(1L).phase(Phase.MOLIENDA).build();
        when(productPhaseRepository.findById(1L)).thenReturn(Optional.of(phase));
        when(materialRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.createRecipe(dto));
    }

    @Test
    void updateRecipe_success() {
        RecipeUpdateDTO dto = RecipeUpdateDTO.builder().materialId(2L).quantity(10.0).build();
        Recipe original = new Recipe();
        Material material = Material.builder().id(2L).build();
        Recipe saved = new Recipe();
        RecipeResponseDTO response = new RecipeResponseDTO();

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(original));
        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(recipeRepository.save(original)).thenReturn(saved);
        when(recipeMapper.toResponseDTO(saved)).thenReturn(response);

        RecipeResponseDTO result = service.updateRecipe(1L, dto);
        assertSame(response, result);
        verify(recipeRepository).save(original);
    }

    @Test
    void updateRecipe_notFound_throws() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.updateRecipe(1L, RecipeUpdateDTO.builder().build()));
    }

    @Test
    void deleteRecipe_success() {
        Recipe recipe = new Recipe();
        ProductPhase phase = ProductPhase.builder().isReady(true).build();
        recipe.setProductPhase(phase);
        RecipeResponseDTO response = new RecipeResponseDTO();
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(recipeMapper.toResponseDTO(recipe)).thenReturn(response);
        RecipeResponseDTO result = service.deleteRecipe(1L);
        assertSame(response, result);
        verify(recipeRepository).delete(recipe);
        verify(productPhaseService).reviewIsReady(phase);
    }

    @Test
    void deleteRecipe_notFound_throws() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.deleteRecipe(1L));
    }

    @Test
    void getRecipe_success() {
        Recipe recipe = new Recipe();
        RecipeResponseDTO response = new RecipeResponseDTO();
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(recipeMapper.toResponseDTO(recipe)).thenReturn(response);
        RecipeResponseDTO result = service.getRecipe(1L);
        assertSame(response, result);
    }

    @Test
    void getRecipe_notFound_throws() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getRecipe(1L));
    }

    @Test
    void getRecipeList_success() {
        Recipe recipe = new Recipe();
        RecipeResponseDTO response = new RecipeResponseDTO();
        when(recipeRepository.findAll()).thenReturn(List.of(recipe));
        when(recipeMapper.toResponseDTO(recipe)).thenReturn(response);
        List<RecipeResponseDTO> result = service.getRecipeList();
        assertEquals(1, result.size());
        assertSame(response, result.get(0));
    }
}
