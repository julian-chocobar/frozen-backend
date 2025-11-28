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
     * 
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
        
        // Validar que el output de la fase no sea mayor que input + ingredientes
        validateProductPhaseOutput(productPhase);

        return recipeMapper.toResponseDTO(savedRecipe);
    }

    /**
     * Funcion que cambia ciertos parametros de una receta preexistente
     * 
     * @param id
     * @param recipeUpdateDTO
     * @return RecipeResponseDTO
     */
    @Override
    @Transactional
    public RecipeResponseDTO updateRecipe(Long id, RecipeUpdateDTO recipeUpdateDTO) {
        Recipe originalRecipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro receta con id " + id));

        if (recipeUpdateDTO.getMaterialId() != null) {
            Material material = materialRepository.findById(recipeUpdateDTO.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Material no encontrado" + recipeUpdateDTO.getMaterialId()));
            originalRecipe.setMaterial(material);
        }

        if (recipeUpdateDTO.getQuantity() != null) {
            originalRecipe.setQuantity(recipeUpdateDTO.getQuantity());
        }

        Recipe savedRecipe = recipeRepository.save(originalRecipe);
        
        // Validar que el output de la fase no sea mayor que input + ingredientes
        validateProductPhaseOutput(savedRecipe.getProductPhase());

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
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro receta con id " + id));

        ProductPhase productPhaseAssociated = recipe.getProductPhase();

        // crear DTO antes de borrar (seguro para devolver datos)
        RecipeResponseDTO dto = recipeMapper.toResponseDTO(recipe);

        // eliminar y asegurar que la operación se materialice en la BD
        recipeRepository.delete(recipe);
        recipeRepository.flush();

        // ahora re-evaluar la fase: si quedó sin recetas, reviewIsReady debe marcar
        // isReady = false
        if (productPhaseAssociated != null && productPhaseAssociated.getIsReady()) {
            productPhaseService.reviewIsReady(productPhaseAssociated.getId());
        }

        return dto;
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
                .map(recipeMapper::toResponseDTO).toList();

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
    public List<RecipeResponseDTO> getMaterialByPhase(Long id) {
        ProductPhase productPhase = productPhaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró Product Phase con id " + id));
        List<Recipe> recipes = recipeRepository.findByProductPhase(productPhase);

        List<RecipeResponseDTO> materialsByPhase = recipes.stream()
                .map(recipeMapper::toResponseDTO)
                .toList();

        return materialsByPhase;
    }

    /**
     * Funcion que busca todas las recetas de un producto en especifico
     * 
     * @param id
     * @return Lista de recipeResponseDto
     */
    @Override
    @Transactional
    public List<RecipeResponseDTO> getMaterialByProduct(Long id) {
        List<Recipe> recipes = getRecipeByProduct(id);

        List<RecipeResponseDTO> materialsByProduct = recipes.stream()
                .map(recipeMapper::toResponseDTO)
                .toList();

        return materialsByProduct;
    }

    /**
     * Funcion que busca todas las recetas de un producto en especifico
     * 
     * @param id
     * @return lista de recetas
     */
    @Override
    public List<Recipe> getRecipeByProduct(Long id) {
        return recipeRepository.findByProductPhase_ProductId(id);
    }
    
    /**
     * Valida que el output de una fase no sea mayor que el input + total de ingredientes.
     * El output puede ser menor o igual debido a posibles mermas.
     * 
     * @param productPhase Fase a validar
     */
    private void validateProductPhaseOutput(ProductPhase productPhase) {
        // Si output es null o 0, no validar (aún no está definido)
        if (productPhase.getOutput() == null || productPhase.getOutput() == 0.0) {
            return;
        }
        
        // Obtener el input (puede ser null o 0 para la primera fase)
        Double input = productPhase.getInput() != null ? productPhase.getInput() : 0.0;
        
        // Calcular el total de ingredientes de esta fase
        List<Recipe> recipes = recipeRepository.findByProductPhase(productPhase);
        Double totalIngredients = recipes.stream()
                .map(Recipe::getQuantity)
                .filter(qty -> qty != null && qty > 0)
                .reduce(0.0, Double::sum);
        
        // Calcular el máximo posible: input + ingredientes
        Double maxPossible = input + totalIngredients;
        
        // Validar que output <= input + ingredientes
        if (productPhase.getOutput() > maxPossible) {
            throw new BadRequestException(
                String.format("El output de la fase %s (%.2f) no puede ser mayor que el input (%.2f) más los ingredientes (%.2f) = %.2f. " +
                        "El output puede ser menor o igual debido a posibles mermas.",
                    productPhase.getPhase(), productPhase.getOutput(), input, totalIngredients, maxPossible));
        }
    }
}
