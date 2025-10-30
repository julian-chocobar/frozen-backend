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
import com.enigcode.frozen_backend.recipes.repository.RecipeRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductPhaseServiceImpl implements ProductPhaseService {

    final ProductPhaseRepository productPhaseRepository;
    final ProductRepository productRepository;
    final RecipeRepository recipeRepository;
    final ProductPhaseMapper productPhaseMapper;

    /**
     * Funcion que modifica parcialmente un product phase
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

        productPhaseMapper.partialUpdate(productPhaseUpdateDTO, productPhase);

        ProductPhase savedProductPhase = productPhaseRepository.save(productPhase);
        return productPhaseMapper.toResponseDto(savedProductPhase);
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
        List<ProductPhase> phases = productPhaseRepository.findByProductIdOrderByIdAsc(productId);

        if (phases.isEmpty() && !productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product no encontrado con ID: " + productId);
        }

        return phases.stream()
                .map(productPhaseMapper::toResponseDto)
                .collect(Collectors.toList());
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
