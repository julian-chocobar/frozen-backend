package com.enigcode.frozen_backend.production_orders.Service;

import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.service.BatchService;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.movements.DTO.MovementSimpleCreateDTO;
import com.enigcode.frozen_backend.movements.model.MovementType;
import com.enigcode.frozen_backend.movements.service.MovementService;
import com.enigcode.frozen_backend.product_phases.service.ProductPhaseService;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderResponseDTO;
import com.enigcode.frozen_backend.production_orders.Mapper.ProductionOrderMapper;
import com.enigcode.frozen_backend.production_orders.Model.OrderStatus;
import com.enigcode.frozen_backend.production_orders.Model.ProductionOrder;
import com.enigcode.frozen_backend.production_orders.Repository.ProductionOrderRepository;
import com.enigcode.frozen_backend.products.model.Product;
import com.enigcode.frozen_backend.products.repository.ProductRepository;
import com.enigcode.frozen_backend.recipes.service.RecipeService;
import jakarta.transaction.Transactional;
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

        Batch batch = batchService.createBatch(createDTO, product);

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
     * TODO: Cuando esten las fases de produccion de un lote se deben adherir los materiales a cada una de las fases correspondientes
     * @param productId
     * @param materialQuantityMultiplier la cantidad de producto dividido el estandar del mismo
     */
    private void reserveMaterials(Long productId, Double materialQuantityMultiplier) {
        //FIXME: FALTA IMPLEMENTAR LA VINCULACION DE LOS MATERIALES A LAS DISTINTAS FASES DE PRODUCCION
        List<MovementSimpleCreateDTO> reserveMaterialMovements =
                recipeService.getRecipeByProduct(productId).stream().map(recipe -> {
                    return  MovementSimpleCreateDTO.builder()
                            .material(recipe.getMaterial())
                            .stock(recipe.getQuantity() * materialQuantityMultiplier)
                            .build();
                }).toList();

        movementService.createReserveOrReturn(MovementType.RESERVA, reserveMaterialMovements);
    }

    /**
     * Funcion que permite aprobar una orden de produccion si tenes un rol determinado
     * TODO: Actualmente vuelve a calcular los materiales necesarios y reduce de la reserva a partir de eso, cuando se tenga el modulo ProductionMaterial se debe cambiar
     * @param id
     * @return
     */
    @Override
    @Transactional
    public ProductionOrderResponseDTO approveOrder(Long id) {
        ProductionOrder productionOrder = productionOrderRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("No se encontró orden de produccion con id " + id));

        if(!productionOrder.getStatus().equals(OrderStatus.PENDIENTE))
            throw new BadRequestException("La orden esta en estado " + productionOrder.getStatus());

        //FIXME: FUNCION QUE DEBE SER MODIFICADA EN PROXIMO SPRINT (MODULO NO COMPLETADO)
        confirmApprovedMaterials(productionOrder.getProduct().getId(),
                productionOrder.getQuantity()/productionOrder.getProduct().getStandardQuantity());

        productionOrder.setStatus(OrderStatus.APROBADO);
        productionOrder.setValidationDate(OffsetDateTime.now());

        ProductionOrder savedProductionOrder = productionOrderRepository.save(productionOrder);

        return productionOrderMapper.toResponseDTO(savedProductionOrder);
    }

    /**
     * Funcion que confirma la aprobacion de los materiales y por cada material lo pasa a los asocia a las fases
     * TODO: Actualmente la logica se calcula de nuevo, esto debe ser remplazado para buscar los materiales y cantidades ya calculadas en la creacion
     * @param productId
     * @param materialQuantityMultiplier
     */
    private void confirmApprovedMaterials(Long productId, Double materialQuantityMultiplier) {
        //FIXME: Esta lista se debera buscar de la ProducionMaterials ya reservadas para ese producto
        List<MovementSimpleCreateDTO> confirmReservationMovements =
                recipeService.getRecipeByProduct(productId).stream().map(recipe -> {
                    return  MovementSimpleCreateDTO.builder()
                            .material(recipe.getMaterial())
                            .stock(recipe.getQuantity() * materialQuantityMultiplier)
                            .build();
                }).toList();

        movementService.confirmReservation(confirmReservationMovements);
    }

    /**
     * Funcion que permite cancelar una orden de produccion si esta sigue pendiente y devolver los materiales utilizados
     * TODO: Actualmente vuelve a calcular los materiales necesarios y reduce de la reserva a partir de eso, cuando se tenga el modulo ProductionMaterial se debe cambiar
     * @param id
     * @return
     */
    @Override
    @Transactional
    public ProductionOrderResponseDTO returnOrder(Long id, OrderStatus orderStatus) {
        ProductionOrder productionOrder = productionOrderRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("No se encontró orden de produccion con id " + id));

        if(!productionOrder.getStatus().equals(OrderStatus.PENDIENTE))
            throw new BadRequestException("La orden esta en estado " + productionOrder.getStatus());

        if(orderStatus.equals(OrderStatus.PENDIENTE))
            throw new BadRequestException("Esta funcion no cambia a estado " + orderStatus);

        //FIXME: FUNCION QUE DEBE SER MODIFICADA EN PROXIMO SPRINT (MODULO NO COMPLETADO)
        returnReservedMaterials(productionOrder.getProduct().getId(),
                productionOrder.getQuantity()/productionOrder.getProduct().getStandardQuantity());

        productionOrder.setStatus(orderStatus);
        productionOrder.setValidationDate(OffsetDateTime.now());

        ProductionOrder savedProductionOrder = productionOrderRepository.save(productionOrder);

        return productionOrderMapper.toResponseDTO(savedProductionOrder);
    }

    /**
     * Busca y devuelve informacion de un production order especificado por id
     * @param id
     * @return
     */
    @Override
    public ProductionOrderResponseDTO getProductionOrder(Long id) {
        ProductionOrder productionOrder = productionOrderRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("No se encontro Orden de id "+ id));

        return productionOrderMapper.toResponseDTO(productionOrder);
    }

    /**
     * Funcion que devuelve de los materiales reservadios debido a la cancelacion de una orden
     * TODO: Actualmente la logica se calcula de nuevo, esto debe ser remplazado para buscar los materiales y cantidades ya calculadas en la creacion
     * @param productId
     * @param materialQuantityMultiplier
     */
    private void returnReservedMaterials(Long productId, Double materialQuantityMultiplier) {
        //FIXME: Esta lista se debera buscar de la ProducionMaterials ya reservadas para ese producto
        List<MovementSimpleCreateDTO> returnReservedMaterialsMovements =
                recipeService.getRecipeByProduct(productId).stream().map(recipe -> {
                    return  MovementSimpleCreateDTO.builder()
                            .material(recipe.getMaterial())
                            .stock(recipe.getQuantity() * materialQuantityMultiplier)
                            .build();
                }).toList();

        movementService.createReserveOrReturn(MovementType.DEVUELTO, returnReservedMaterialsMovements);
    }
}
