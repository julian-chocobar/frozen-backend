package com.enigcode.frozen_backend.product_phases.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseResponseDTO;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseUpdateDTO;
import com.enigcode.frozen_backend.product_phases.mapper.ProductPhaseMapper;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import com.enigcode.frozen_backend.product_phases.repository.ProductPhaseRepository;
import com.enigcode.frozen_backend.products.repository.ProductRepository;
import com.enigcode.frozen_backend.recipes.model.Recipe;
import com.enigcode.frozen_backend.recipes.repository.RecipeRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductPhaseServiceImpl implements ProductPhaseService {

    final ProductPhaseRepository productPhaseRepository;
    final ProductRepository productRepository;
    final RecipeRepository recipeRepository;
    final ProductPhaseMapper productPhaseMapper;

    /**
     * Funcion que modifica parcialmente un product phase
     * Ajusta automáticamente el input de la siguiente fase si se actualiza el output
     * 
     * @param id
     * @param productPhaseUpdateDTO
     * @return
     */
    @Override
    @Transactional
    public ProductPhaseResponseDTO updateProductPhase(Long id, @Valid ProductPhaseUpdateDTO productPhaseUpdateDTO) {
        ProductPhase productPhase = productPhaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductPhase no encontrado con ID: " + id));

        // Validación: Si se intenta editar output, debe haber al menos un ingrediente (recipe)
        // Nota: El input se establece automáticamente, no se puede editar manualmente
        if (productPhaseUpdateDTO.getOutput() != null) {
            List<Recipe> recipes = recipeRepository.findByProductPhase(productPhase);
            if (recipes == null || recipes.isEmpty()) {
                throw new BadRequestException(
                    String.format("No se puede editar el output de la fase %s sin al menos un ingrediente (recipe) cargado. " +
                            "Por favor, agregue al menos un ingrediente antes de definir el valor de output.",
                        productPhase.getPhase()));
            }
        }

        // Advertencia: Si se intenta enviar input, se ignorará ya que se establece automáticamente
        if (productPhaseUpdateDTO.getInput() != null) {
            // El input se establece automáticamente:
            // - MOLIENDA: siempre 0.0
            // - Otras fases: del output de la fase anterior
            // Se ignora el valor enviado en el DTO
        }

        // Guardar el output anterior para comparar
        Double previousOutput = productPhase.getOutput();
        com.enigcode.frozen_backend.materials.model.UnitMeasurement previousOutputUnit = productPhase.getOutputUnit();

        productPhaseMapper.partialUpdate(productPhaseUpdateDTO, productPhase);
        
        // Asegurar que los valores 0.0 se establezcan explícitamente (MapStruct podría ignorarlos)
        if (productPhaseUpdateDTO.getOutput() != null) {
            productPhase.setOutput(productPhaseUpdateDTO.getOutput());
        }
        if (productPhaseUpdateDTO.getEstimatedHours() != null) {
            productPhase.setEstimatedHours(productPhaseUpdateDTO.getEstimatedHours());
        }
        if (productPhaseUpdateDTO.getOutputUnit() != null) {
            productPhase.setOutputUnit(productPhaseUpdateDTO.getOutputUnit());
        }
        
        // Establecer input automáticamente (ignorar cualquier valor enviado en el DTO)
        if (productPhase.getPhase() == Phase.MOLIENDA) {
            // MOLIENDA siempre tiene input = 0.0 (primera fase)
            productPhase.setInput(0.0);
        } else {
            // Para otras fases, obtener el input del output de la fase anterior
            // Recargar las fases desde la BD para asegurar que tenemos los valores más recientes
            List<ProductPhase> phases = productPhaseRepository
                    .findByProductIdOrderByPhaseOrderAsc(productPhase.getProduct().getId());
            
            // Encontrar la fase actual en la lista recargada
            ProductPhase currentPhaseReloaded = phases.stream()
                    .filter(p -> p.getId().equals(productPhase.getId()))
                    .findFirst()
                    .orElse(productPhase);
            
            // Encontrar la fase anterior
            int currentIndex = phases.indexOf(currentPhaseReloaded);
            if (currentIndex > 0) {
                ProductPhase previousPhase = phases.get(currentIndex - 1);
                if (previousPhase.getOutput() != null) {
                    productPhase.setInput(previousPhase.getOutput());
                    // Si la fase actual no tiene unidad de medida, usar la de la fase anterior
                    if (productPhase.getOutputUnit() == null && previousPhase.getOutputUnit() != null) {
                        productPhase.setOutputUnit(previousPhase.getOutputUnit());
                    }
                } else {
                    // Si la fase anterior no tiene output, el input queda en null (se establecerá cuando se actualice la fase anterior)
                    productPhase.setInput(null);
                }
            } else {
                // No hay fase anterior (no debería pasar, pero por seguridad)
                productPhase.setInput(null);
            }
        }
        
        // Si se actualizó el output, ajustar el input de la siguiente fase
        if (productPhaseUpdateDTO.getOutput() != null && 
            (previousOutput == null || !previousOutput.equals(productPhase.getOutput()) ||
             previousOutputUnit != productPhase.getOutputUnit())) {
            
            adjustNextPhaseInput(productPhase);
        }
        
        // Recargar la fase actual desde la base de datos para asegurar que tenemos
        // la versión actualizada con las relaciones del producto (incluyendo la siguiente fase actualizada)
        // Esto es necesario después de ajustar el input de la siguiente fase
        ProductPhase productPhaseForValidation = productPhaseRepository.findById(productPhase.getId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductPhase no encontrado con ID: " + productPhase.getId()));
        
        // Validar que el output coincida con el input de la siguiente fase
        try {
            productPhaseForValidation.validateOutputMatchesNextPhaseInput();
        } catch (BadRequestException e) {
            // Si la validación falla, lanzar la excepción
            throw e;
        }
        
        // Validar que el output no sea mayor que input + ingredientes 
        validateOutputNotGreaterThanInputPlusIngredients(productPhase);
        

        ProductPhase savedProductPhase = productPhaseRepository.save(productPhase);
        return productPhaseMapper.toResponseDto(savedProductPhase);
    }
    
    /**
     * Valida que el output de una fase no sea mayor que el input + total de ingredientes.
     * El output puede ser menor o igual debido a posibles mermas.
     * 
     * @param productPhase Fase a validar
     */
    private void validateOutputNotGreaterThanInputPlusIngredients(ProductPhase productPhase) {
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
    
    /**
     * Ajusta automáticamente el input de la siguiente fase para que coincida con el output de la fase actual.
     * Siempre actualiza el input de la siguiente fase cuando se actualiza el output de la fase actual.
     * 
     * @param currentPhase Fase actual cuyo output se actualizó
     */
    private void adjustNextPhaseInput(ProductPhase currentPhase) {
        if (currentPhase.getOutput() == null || currentPhase.getOutputUnit() == null) {
            return;
        }
        
        // Recargar las fases del producto para asegurar que tenemos la última versión
        List<ProductPhase> phases = productPhaseRepository
                .findByProductIdOrderByPhaseOrderAsc(currentPhase.getProduct().getId());
        
        // Encontrar la fase actual en la lista recargada
        ProductPhase currentPhaseReloaded = phases.stream()
                .filter(p -> p.getId().equals(currentPhase.getId()))
                .findFirst()
                .orElse(currentPhase);
        
        // Encontrar la siguiente fase
        int currentIndex = phases.indexOf(currentPhaseReloaded);
        if (currentIndex >= 0 && currentIndex < phases.size() - 1) {
            ProductPhase nextPhase = phases.get(currentIndex + 1);
            
            // Siempre actualizar el input de la siguiente fase para que coincida con el output actual
            nextPhase.setInput(currentPhase.getOutput());
            
            // Si la siguiente fase no tiene unidad de medida definida, usar la de la fase actual
            if (nextPhase.getOutputUnit() == null) {
                nextPhase.setOutputUnit(currentPhase.getOutputUnit());
            }
            
            // Guardar la siguiente fase con el input actualizado
            productPhaseRepository.save(nextPhase);
        }
    }

    /**
     * Funcion que busca y devuelve todas las productphases paginadas
     * 
     * @param pageable
     * @return
     */
    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<ProductPhaseResponseDTO> findAll(Pageable pageable) {
        Page<ProductPhase> productPhases = productPhaseRepository.findAll(pageable);
        return productPhases.map(productPhaseMapper::toResponseDto);
    }

    /**
     * Funcion que busca y devuelve product phase en especifico
     * 
     * @param id
     * @return
     */
    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public ProductPhaseResponseDTO getProductPhase(Long id) {
        ProductPhase productPhase = productPhaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductPhase no encontrado con ID: " + id));

        return productPhaseMapper.toResponseDto(productPhase);
    }

    /**
     * Funcion que devuelve una lista de las fases segun el id del producto
     * 
     * @param productId
     * @return
     */
    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ProductPhaseResponseDTO> getByProduct(Long productId) {
        List<ProductPhase> phases = productPhaseRepository.findByProductIdOrderByPhaseOrderAsc(productId);

        if (phases.isEmpty() && !productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product no encontrado con ID: " + productId);
        }

        return phases.stream()
                .map(productPhaseMapper::toResponseDto)
                .toList();
    }

    /**
     * Funcion que marca como lista a una fase si tiene todos sus campos completos y
     * los materiales minimos requeridos
     * 
     * @param id
     * @return
     */
    @Override
    @Transactional
    public ProductPhaseResponseDTO toggleReady(Long id) {
        ProductPhase productPhase = productPhaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductPhase no encontrado con ID: " + id));
        if (productPhase.getIsReady()) {
            productPhase.setIsReady(false);
            productPhase.getProduct().setIsReady(false);
            productRepository.save(productPhase.getProduct());
            ProductPhase savedProductPhase = productPhaseRepository.save(productPhase);
            return productPhaseMapper.toResponseDto(savedProductPhase);
        } else {

            if (!productPhase.isComplete())
                throw new BadRequestException(
                        "La fase no puede estar listo debido a que tiene campos requeridos vacíos");

            List<MaterialType> requiredMaterials = productPhase.getRequiredMaterials();

            // Se revisa que no falta ningun material o devuelve una lista con los
            // materiales que faltan (comprobando recetas SOLO de esta fase)
            if ((requiredMaterials != null && !requiredMaterials.isEmpty())
                    && productPhase.getPhase() != Phase.ENVASADO) {
                List<MaterialType> missingMaterials = requiredMaterials.stream()
                        .filter(type -> !recipeRepository.existsByProductPhaseIdAndMaterial_Type(productPhase.getId(),
                                type))
                        .toList();
                if (!missingMaterials.isEmpty()) {
                    throw new BadRequestException("Faltan materiales requeridos en las recetas: " + missingMaterials);
                }
            }

            productPhase.setIsReady(Boolean.TRUE);
            ProductPhase savedProductPhase = productPhaseRepository.save(productPhase);

            // Si con esta fase lista todas las fases del producto quedan listas,
            // marcar el producto como listo
            Long productId = savedProductPhase.getProduct().getId();
            boolean anyPhaseNotReady = productPhaseRepository.existsByProductIdAndIsReadyFalse(productId);
            if (!anyPhaseNotReady) {
                savedProductPhase.getProduct().setIsReady(Boolean.TRUE);
                productRepository.save(savedProductPhase.getProduct());
            }

            return productPhaseMapper.toResponseDto(savedProductPhase);
        }
    }

    /**
     * Funcion que revisa si hay al menos un material del tipo necesario, sino
     * desmarca el ready del productphase
     * 
     * @param productPhase
     */
    @Override
    @Transactional
    public void reviewIsReady(Long productPhaseId) {
        ProductPhase productPhase = productPhaseRepository.findById(productPhaseId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("No se encontró Product Phase con id " + productPhaseId));

        List<MaterialType> requiredMaterials = productPhase.getRequiredMaterials();

        // Si no hay materiales requeridos, desmarcar ready
        if (requiredMaterials == null || requiredMaterials.isEmpty()) {
            productPhase.setIsReady(Boolean.FALSE);
            productPhase.getProduct().setIsReady(Boolean.FALSE);
            productRepository.save(productPhase.getProduct());
            productPhaseRepository.save(productPhase);
            return;
        }
        // Para cada tipo requerido, comprobar si existe al menos una receta para esta
        // fase y tipo
        for (MaterialType type : requiredMaterials) {
            boolean exists = recipeRepository.existsByProductPhaseIdAndMaterial_Type(productPhaseId, type);
            if (!exists) {
                productPhase.setIsReady(Boolean.FALSE);
                productPhase.getProduct().setIsReady(Boolean.FALSE);
                productRepository.save(productPhase.getProduct());
                productPhaseRepository.save(productPhase);
                return;
            }
        }
    }

}
