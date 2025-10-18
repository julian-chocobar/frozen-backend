package com.enigcode.frozen_backend.production_orders.Service;

import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.service.BatchService;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.movements.DTO.MovementSimpleCreateDTO;
import com.enigcode.frozen_backend.movements.model.MovementType;
import com.enigcode.frozen_backend.movements.service.MovementService;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderResponseDTO;
import com.enigcode.frozen_backend.production_orders.Mapper.ProductionOrderMapper;
import com.enigcode.frozen_backend.production_orders.Model.OrderStatus;
import com.enigcode.frozen_backend.production_orders.Model.ProductionOrder;
import com.enigcode.frozen_backend.production_orders.Repository.ProductionOrderRepository;
import com.enigcode.frozen_backend.products.model.Product;
import com.enigcode.frozen_backend.products.repository.ProductRepository;
import com.enigcode.frozen_backend.recipes.model.Recipe;
import com.enigcode.frozen_backend.recipes.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionOrderServiceImpl implements ProductionOrderService{

    final ProductionOrderMapper productionOrderMapper;
    final ProductRepository productRepository;
    final BatchService batchService;
    final RecipeService recipeService;
    final MovementService movementService;
    final ProductionOrderRepository productionOrderRepository;

    /**
     * Funcion que crea una nueva orden de produccion en estado pendiente junto al lote que le corresponde
     * Reserva los materiales correspondientes a esa produccion
     * @param createDTO
     * @return
     */
    @Override
    public ProductionOrderResponseDTO createProductionOrder(ProductionOrderCreateDTO createDTO) {
        Product product = productRepository.findById(createDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró producto con id " + createDTO.getProductId()));

        if(product.getIsReady().equals(Boolean.FALSE))
            throw new BadRequestException("El producto "+ product.getId()+ "no esta listo para produccion");

        Batch batch = batchService.createBatch(createDTO.getPackagingId(),
                createDTO.getPlannedDate(), createDTO.getQuantity());

        if(!batch.getPackaging().getUnitMeasurement().equals(product.getUnitMeasurement()))
            throw new BadRequestException("La unidad del producto: " + product.getUnitMeasurement() +
                    " y del packaging: " + batch.getPackaging().getUnitMeasurement() + " deben ser la misma");

        Double quantity = batch.getQuantity() * batch.getPackaging().getQuantity() ;

        Double materialQuantityMultiplier = quantity / product.getStandardQuantity();

        reserveMaterials(product.getId(), materialQuantityMultiplier);

        ProductionOrder productionOrder = ProductionOrder.builder()
                .batch(batch)
                .product(product)
                .status(OrderStatus.PENDIENTE)
                .quantity(quantity)
                .creationDate(OffsetDateTime.now())
                .build();

        ProductionOrder savedProductionOrder = productionOrderRepository.saveAndFlush(productionOrder);
        return productionOrderMapper.toResponseDTO(savedProductionOrder);
    }

    /**
     * Funcion auxiliar para reservar materiales segun id de producto
     * @param productId
     * @param materialQuantityMultiplier
     */
    private void reserveMaterials(Long productId, Double materialQuantityMultiplier) {
        //FALTA IMPLEMENTAR LA VINCULACION DE LOS MATERIALES A LAS DISTINTAS FASES DE PRODUCCION
        List<MovementSimpleCreateDTO> reserveMaterialMovements =
                recipeService.getRecipeByProduct(productId).stream().map(recipe -> {
                    return  MovementSimpleCreateDTO.builder()
                            .material(recipe.getMaterial())
                            .stock(recipe.getQuantity() * materialQuantityMultiplier)
                            .build();
                }).toList();

        movementService.createReserveOrReturn(MovementType.RESERVA, reserveMaterialMovements);
    }
}
