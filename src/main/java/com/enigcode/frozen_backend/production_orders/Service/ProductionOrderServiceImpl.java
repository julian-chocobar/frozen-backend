package com.enigcode.frozen_backend.production_orders.Service;

import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.service.BatchService;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.movements.DTO.MovementSimpleCreateDTO;
import com.enigcode.frozen_backend.movements.model.MovementType;
import com.enigcode.frozen_backend.movements.service.MovementService;
import com.enigcode.frozen_backend.notifications.service.NotificationService;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.production_materials.model.ProductionMaterial;
import com.enigcode.frozen_backend.production_materials.repository.ProductionMaterialRepository;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderFilterDTO;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderResponseDTO;
import com.enigcode.frozen_backend.production_orders.Mapper.ProductionOrderMapper;
import com.enigcode.frozen_backend.production_orders.Model.OrderStatus;
import com.enigcode.frozen_backend.production_orders.Model.ProductionOrder;
import com.enigcode.frozen_backend.production_orders.Repository.ProductionOrderRepository;
import com.enigcode.frozen_backend.production_orders.specification.ProductionOrderSpecification;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import com.enigcode.frozen_backend.products.model.Product;
import com.enigcode.frozen_backend.products.repository.ProductRepository;
import com.enigcode.frozen_backend.recipes.service.RecipeService;
import com.enigcode.frozen_backend.users.model.User;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductionOrderServiceImpl implements ProductionOrderService {

    private final ProductionOrderMapper productionOrderMapper;
    private final ProductRepository productRepository;
    private final BatchService batchService;
    private final RecipeService recipeService;
    private final MovementService movementService;
    private final ProductionOrderRepository productionOrderRepository;
    private final NotificationService notificationService;
    private final ProductionMaterialRepository productionMaterialRepository;
    private final com.enigcode.frozen_backend.users.service.UserService userService;

    /**
     * Funcion que crea una nueva orden de produccion en estado pendiente junto al
     * lote que le corresponde
     * Reserva los materiales correspondientes a esa produccion
     * 
     * @param createDTO
     * @return
     */
    @Override
    public ProductionOrderResponseDTO createProductionOrder(ProductionOrderCreateDTO createDTO) {
        Product product = productRepository.findById(createDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró producto con id " + createDTO.getProductId()));

        if (product.getIsReady().equals(Boolean.FALSE))
            throw new BadRequestException("El producto " + product.getId() + "no esta listo para produccion");

        Batch batch = batchService.createBatch(createDTO, product);

        if (!batch.getPackaging().getUnitMeasurement().equals(product.getUnitMeasurement()))
            throw new BadRequestException("La unidad del producto: " + product.getUnitMeasurement() +
                    " y del packaging: " + batch.getPackaging().getUnitMeasurement() + " deben ser la misma");

        // Calcular y redondear la cantidad total a 3 decimales para evitar números
        // excesivos
        Double quantity = roundToDecimals(batch.getQuantity() * batch.getPackaging().getQuantity(), 3);

        // Redondear el multiplicador a 6 decimales para evitar precisión excesiva
        Double materialQuantityMultiplier = roundToDecimals(quantity / product.getStandardQuantity(), 6);

        reserveMaterials(batch, product.getId(), materialQuantityMultiplier);

        // Obtener usuario actual del contexto de seguridad
        Optional<User> currentUser = Optional.ofNullable(userService.getCurrentUser());

        ProductionOrder productionOrder = ProductionOrder.builder()
                .batch(batch)
                .product(product)
                .status(OrderStatus.PENDIENTE)
                .quantity(quantity)
                .creationDate(OffsetDateTime.now())
                .createdByUser(currentUser.orElse(null))
                .build();

        // Asignar el usuario creador al batch también
        batch.setAssignedUser(currentUser.orElse(null));

        ProductionOrder savedProductionOrder = productionOrderRepository.saveAndFlush(productionOrder);

        // Crear notificación para gerentes de planta sobre la nueva orden pendiente
        notificationService.createProductionOrderNotification(savedProductionOrder.getId(), product.getName());

        log.info("Orden de producción {} creada para producto: {}", savedProductionOrder.getId(), product.getName());

        return productionOrderMapper.toResponseDTO(savedProductionOrder);
    }

    /**
     * Funcion auxiliar para reservar materiales segun id de producto
     * 
     * @param batch
     * @param productId
     * @param materialQuantityMultiplier
     */
    private void reserveMaterials(Batch batch, Long productId, Double materialQuantityMultiplier) {
        List<MovementSimpleCreateDTO> reserveMaterialMovements = new ArrayList<>();
        Map<Phase, ProductionPhase> batchPhases = batch.getPhasesAsMap();
        List<ProductionMaterial> productionMaterials = new ArrayList<>();

        recipeService.getRecipeByProduct(productId).stream()
                .forEach(recipe -> {
                    // Redondear cantidad de material a 6 decimales para evitar precisión excesiva
                    Double quantity = roundToDecimals(recipe.getQuantity() * materialQuantityMultiplier, 6);

                    reserveMaterialMovements.add(
                            MovementSimpleCreateDTO.builder()
                                    .material(recipe.getMaterial())
                                    .stock(quantity)
                                    .build());

                    productionMaterials.add(
                            ProductionMaterial.builder()
                                    .material(recipe.getMaterial())
                                    .productionPhase(batchPhases.get(recipe.getProductPhase().getPhase()))
                                    .quantity(quantity)
                                    .build());
                });

        movementService.createReserveOrReturn(MovementType.RESERVA, reserveMaterialMovements);
        productionMaterialRepository.saveAllAndFlush(productionMaterials);
    }

    /**
     * Funcion que permite aprobar una orden de produccion
     * 
     * @param id
     * @return
     */
    @Override
    @Transactional
    public ProductionOrderResponseDTO approveOrder(Long id) {
        ProductionOrder productionOrder = productionOrderRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("No se encontró orden de produccion con id " + id));

        if (!productionOrder.getStatus().equals(OrderStatus.PENDIENTE))
            throw new BadRequestException("La orden esta en estado " + productionOrder.getStatus());

        confirmApprovedMaterials(productionOrder.getBatch().getId());

        // Obtener usuario actual (aprobador) del contexto de seguridad
        User currentUser = userService.getCurrentUser();

        productionOrder.setStatus(OrderStatus.APROBADA);
        productionOrder.setValidationDate(OffsetDateTime.now());
        productionOrder.setApprovedByUser(currentUser);

        ProductionOrder savedProductionOrder = productionOrderRepository.save(productionOrder);

        return productionOrderMapper.toResponseDTO(savedProductionOrder);
    }

    /**
     * Funcion que confirma la aprobacion de los materiales y por cada material lo
     * pasa a los asocia a las fases
     * 
     * @param productId
     * @param materialQuantityMultiplier
     */
    private void confirmApprovedMaterials(Long batchId) {
        List<ProductionMaterial> productionMaterials = productionMaterialRepository.findAllByBatchId(batchId);
        List<MovementSimpleCreateDTO> confirmReservationMovements = productionMaterials.stream()
                .map(recipe -> {
                    return MovementSimpleCreateDTO.builder()
                            .material(recipe.getMaterial())
                            .stock(recipe.getQuantity())
                            .build();
                }).toList();

        movementService.confirmReservation(confirmReservationMovements);
    }

    /**
     * Funcion que permite cancelar una orden de produccion si esta sigue pendiente
     * y devolver los materiales utilizados
     * 
     * @param id
     * @return
     */
    @Override
    @Transactional
    public ProductionOrderResponseDTO returnOrder(Long id, OrderStatus orderStatus) {
        ProductionOrder productionOrder = productionOrderRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("No se encontró orden de produccion con id " + id));

        if (!productionOrder.getStatus().equals(OrderStatus.PENDIENTE))
            throw new BadRequestException("La orden esta en estado " + productionOrder.getStatus());

        if (orderStatus.equals(OrderStatus.PENDIENTE))
            throw new BadRequestException("Esta funcion no cambia a estado " + orderStatus);

        returnReservedMaterials(productionOrder.getBatch().getId());

        productionOrder.setStatus(orderStatus);
        productionOrder.setValidationDate(OffsetDateTime.now());

        ProductionOrder savedProductionOrder = productionOrderRepository.save(productionOrder);

        return productionOrderMapper.toResponseDTO(savedProductionOrder);
    }

    /**
     * Funcion que devuelve de los materiales reservadios debido a la cancelacion de
     * una orden
     *
     * @param productId
     * @param materialQuantityMultiplier
     */
    private void returnReservedMaterials(Long batchId) {
        List<ProductionMaterial> productionMaterials = productionMaterialRepository.findAllByBatchId(batchId);
        List<MovementSimpleCreateDTO> returnReservedMaterialsMovements = productionMaterials.stream()
                .map(recipe -> {
                    return MovementSimpleCreateDTO.builder()
                            .material(recipe.getMaterial())
                            .stock(recipe.getQuantity())
                            .build();
                }).toList();

        movementService.createReserveOrReturn(MovementType.DEVUELTO, returnReservedMaterialsMovements);
    }

    @Override
    public Page<ProductionOrderResponseDTO> findAll(ProductionOrderFilterDTO filterDTO, Pageable pageable) {
        Pageable pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());
        Page<ProductionOrder> orders = productionOrderRepository.findAll(
                ProductionOrderSpecification.createFilter(filterDTO),
                pageRequest);
        return orders.map(productionOrderMapper::toResponseDTO);

    }

    /**
     * Busca y devuelve informacion de un production order especificado por id
     * 
     * @param id
     * @return
     */
    @Override
    public ProductionOrderResponseDTO getProductionOrder(Long id) {
        ProductionOrder productionOrder = productionOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro Orden de id " + id));

        return productionOrderMapper.toResponseDTO(productionOrder);
    }

    /**
     * Redondea un número decimal a la cantidad especificada de decimales
     */
    private Double roundToDecimals(Double value, int decimals) {
        if (value == null) {
            return null;
        }

        java.math.BigDecimal bd = new java.math.BigDecimal(value.toString());
        bd = bd.setScale(decimals, java.math.RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
