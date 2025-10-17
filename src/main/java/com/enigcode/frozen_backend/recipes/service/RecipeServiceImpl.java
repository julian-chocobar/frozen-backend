package com.enigcode.frozen_backend.recipes.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import com.enigcode.frozen_backend.product_phases.repository.ProductPhaseRepository;
import com.enigcode.frozen_backend.product_phases.service.ProductPhaseService;
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
    final ProductPhaseService productPhaseService;
    final RecipeMapper recipeMapper;

     /**
     * Crea una nueva receta en la base de datos segun DTO
     * @param recipeCreateDTO
     * @return RecipeResponseDTO
     */
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


     /**
     * Funcion que cambia ciertos parametros de una receta preexistente
     * @param id
     * @param recipeUpdateDTO
     * @return RecipeResponseDTO
     */
    @Override
    @Transactional
    public RecipeResponseDTO updateRecipe(Long id, RecipeUpdateDTO recipeUpdateDTO) {
        Recipe originalRecipe = recipeRepository.findById(id)
                        .orElseThrow(()-> new ResourceNotFoundException("No se encontro receta con id "+ id));

        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Material no encontrado" + recipeUpdateDTO.getMaterialId()));
        
        originalRecipe.setMaterial(material);
        originalRecipe.setQuantity(recipeUpdateDTO.getQuantity().intValue());
        

        Recipe savedRecipe = recipeRepository.save(originalRecipe);

        return recipeMapper.toResponseDTO(savedRecipe);
    }


      /**
     * Funcion para eliminar una receta especifico segun id
     * 
     * @param id
     * @return Id de la receta eliminada
     */
    @Override
    @Transactional
    public RecipeResponseDTO deleteRecipe(Long id) {
        Recipe recipe = recipeRepository.findById(id)   
                .orElseThrow(()-> new ResourceNotFoundException("No se encontro receta con id "+ id));

        ProductPhase productPhaseAssociated = recipe.getProductPhase();

        recipeRepository.delete(recipe);

        if(productPhaseAssociated.getIsReady())
            productPhaseService.reviewIsReady(recipe.getProductPhase());

        return recipeMapper.toResponseDTO(recipe);
    }

    /**
     * Funcion para mostrar una receta especifico segun id
     * 
     * @param id
     * @return Vista detallada de los elementos de la receta
     */
    @Override
    @Transactional
    public RecipeResponseDTO getRecipe(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Receta no encontrado con ID: " + id));

        return recipeMapper.toResponseDTO(recipe);
    }

     /**
     * Funcion para mostrar a todas las recetas activos
     *
     * @return Vista detallada de las recetas activos
     */
    @Override
    @Transactional
    public List<RecipeResponseDTO> getRecipeList() {
        List<RecipeResponseDTO> activeRecipes = recipeRepository.findAll().stream()
            .map(recipeMapper :: toResponseDTO).toList();

        return activeRecipes;
    }

    /**
     * Funcion para mostrar todos los materiales asociados a una fase
     * 
     * @param id id de la fase
     * @return RecipeResponseDTO de las recetas de esa fase
     */
    @Override
    @Transactional
    public List<RecipeResponseDTO> getMaterialByPhase(Long id){
        List<RecipeResponseDTO> materialsByPhase = recipeRepository.findAll()
                .stream().filter(recipe -> recipe.getProductPhase().getId() == id).map(recipeMapper :: toResponseDTO).toList();
        
        return materialsByPhase;
    }

    @Override
    @Transactional
    public List<RecipeResponseDTO> getMaterialByProduct(Long id){
        List<RecipeResponseDTO> materialsByProduct = recipeRepository.findAll()
                .stream().filter(recipe -> recipe.getProductPhase().getProduct().getId() == id).map(recipeMapper :: toResponseDTO).toList();
        
        return materialsByProduct;
    }
}
