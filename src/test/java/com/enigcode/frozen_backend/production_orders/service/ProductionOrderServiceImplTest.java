package com.enigcode.frozen_backend.production_orders.service;

import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.service.BatchService;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.movements.DTO.MovementSimpleCreateDTO;
import com.enigcode.frozen_backend.movements.model.MovementType;
import com.enigcode.frozen_backend.movements.service.MovementService;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderFilterDTO;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderResponseDTO;
import com.enigcode.frozen_backend.production_orders.Mapper.ProductionOrderMapper;
import com.enigcode.frozen_backend.production_orders.Model.OrderStatus;
import com.enigcode.frozen_backend.production_orders.Model.ProductionOrder;
import com.enigcode.frozen_backend.production_orders.Repository.ProductionOrderRepository;
import com.enigcode.frozen_backend.production_orders.Service.ProductionOrderServiceImpl;
import com.enigcode.frozen_backend.products.model.Product;
import com.enigcode.frozen_backend.products.repository.ProductRepository;
import com.enigcode.frozen_backend.recipes.model.Recipe;
import com.enigcode.frozen_backend.recipes.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductionOrderServiceImplTest {

        @Mock
        private ProductionOrderMapper productionOrderMapper;
        @Mock
        private ProductRepository productRepository;
        @Mock
        private BatchService batchService;
        @Mock
        private RecipeService recipeService;
        @Mock
        private MovementService movementService;
        @Mock
        private ProductionOrderRepository productionOrderRepository;
        @Mock
        private com.enigcode.frozen_backend.notifications.service.NotificationService notificationService;

        @InjectMocks
        private ProductionOrderServiceImpl service;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
        }

        @Test
        void createProductionOrder_success() {
                // Arrange
                ProductionOrderCreateDTO createDTO = ProductionOrderCreateDTO.builder()
                                .productId(1L)
                                .quantity(100.0)
                                .packagingId(1L)
                                .plannedDate(OffsetDateTime.now())
                                .build();

                Product product = Product.builder()
                                .id(1L)
                                .name("Test Product")
                                .isReady(true)
                                .standardQuantity(10.0)
                                .unitMeasurement(UnitMeasurement.UNIDAD)
                                .build();

                Packaging packaging = Packaging.builder()
                                .id(1L)
                                .quantity(10.0)
                                .unitMeasurement(UnitMeasurement.UNIDAD)
                                .build();

                Batch batch = Batch.builder()
                                .id(1L)
                                .quantity(10)
                                .packaging(packaging)
                                .build();

                Material material1 = Material.builder()
                                .id(1L)
                                .name("Material 1")
                                .type(MaterialType.MALTA)
                                .build();

                Recipe recipe1 = Recipe.builder()
                                .id(1L)
                                .material(material1)
                                .quantity(5.0)
                                .build();

                ProductionOrder productionOrder = ProductionOrder.builder()
                                .id(1L)
                                .batch(batch)
                                .product(product)
                                .status(OrderStatus.PENDIENTE)
                                .quantity(100.0)
                                .creationDate(OffsetDateTime.now())
                                .build();

                ProductionOrderResponseDTO responseDTO = new ProductionOrderResponseDTO();

                when(productRepository.findById(1L)).thenReturn(Optional.of(product));
                when(batchService.createBatch(createDTO, product)).thenReturn(batch);
                when(recipeService.getRecipeByProduct(1L)).thenReturn(List.of(recipe1));
                when(productionOrderRepository.saveAndFlush(any(ProductionOrder.class))).thenReturn(productionOrder);
                when(productionOrderMapper.toResponseDTO(productionOrder)).thenReturn(responseDTO);

                // Act
                ProductionOrderResponseDTO result = service.createProductionOrder(createDTO);

                // Assert
                assertSame(responseDTO, result);
                verify(productRepository).findById(1L);
                verify(batchService).createBatch(createDTO, product);
                verify(recipeService).getRecipeByProduct(1L);
                verify(movementService).createReserveOrReturn(eq(MovementType.RESERVA), anyList());
                verify(productionOrderRepository).saveAndFlush(any(ProductionOrder.class));
                verify(productionOrderMapper).toResponseDTO(productionOrder);
        }

        @Test
        void createProductionOrder_productNotFound_throws() {
                // Arrange
                ProductionOrderCreateDTO createDTO = ProductionOrderCreateDTO.builder()
                                .productId(999L)
                                .quantity(100.0)
                                .packagingId(1L)
                                .plannedDate(OffsetDateTime.now())
                                .build();

                when(productRepository.findById(999L)).thenReturn(Optional.empty());

                // Act & Assert
                assertThrows(ResourceNotFoundException.class, () -> service.createProductionOrder(createDTO));
                verify(productRepository).findById(999L);
                verify(batchService, never()).createBatch(any(), any());
                verify(productionOrderRepository, never()).saveAndFlush(any());
        }

        @Test
        void createProductionOrder_productNotReady_throws() {
                // Arrange
                ProductionOrderCreateDTO createDTO = ProductionOrderCreateDTO.builder()
                                .productId(1L)
                                .quantity(100.0)
                                .packagingId(1L)
                                .plannedDate(OffsetDateTime.now())
                                .build();

                Product product = Product.builder()
                                .id(1L)
                                .name("Test Product")
                                .isReady(false)
                                .standardQuantity(10.0)
                                .unitMeasurement(UnitMeasurement.UNIDAD)
                                .build();

                when(productRepository.findById(1L)).thenReturn(Optional.of(product));

                // Act & Assert
                assertThrows(BadRequestException.class, () -> service.createProductionOrder(createDTO));
                verify(productRepository).findById(1L);
                verify(batchService, never()).createBatch(any(), any());
        }

        @Test
        void createProductionOrder_unitMeasurementMismatch_throws() {
                // Arrange
                ProductionOrderCreateDTO createDTO = ProductionOrderCreateDTO.builder()
                                .productId(1L)
                                .quantity(100.0)
                                .packagingId(1L)
                                .plannedDate(OffsetDateTime.now())
                                .build();

                Product product = Product.builder()
                                .id(1L)
                                .name("Test Product")
                                .isReady(true)
                                .standardQuantity(10.0)
                                .unitMeasurement(UnitMeasurement.UNIDAD)
                                .build();

                Packaging packaging = Packaging.builder()
                                .id(1L)
                                .quantity(10.0)
                                .unitMeasurement(UnitMeasurement.KG) // Different from product
                                .build();

                Batch batch = Batch.builder()
                                .id(1L)
                                .quantity(10)
                                .packaging(packaging)
                                .build();

                when(productRepository.findById(1L)).thenReturn(Optional.of(product));
                when(batchService.createBatch(createDTO, product)).thenReturn(batch);

                // Act & Assert
                assertThrows(BadRequestException.class, () -> service.createProductionOrder(createDTO));
                verify(productRepository).findById(1L);
                verify(batchService).createBatch(createDTO, product);
                verify(productionOrderRepository, never()).saveAndFlush(any());
        }

        @Test
        void createProductionOrder_verifiesCorrectMaterialReservation() {
                // Arrange
                ProductionOrderCreateDTO createDTO = ProductionOrderCreateDTO.builder()
                                .productId(1L)
                                .quantity(100.0)
                                .packagingId(1L)
                                .plannedDate(OffsetDateTime.now())
                                .build();

                Product product = Product.builder()
                                .id(1L)
                                .name("Test Product")
                                .isReady(true)
                                .standardQuantity(10.0) // Standard quantity
                                .unitMeasurement(UnitMeasurement.UNIDAD)
                                .build();

                Packaging packaging = Packaging.builder()
                                .id(1L)
                                .quantity(10.0) // Each batch contains 10 units
                                .unitMeasurement(UnitMeasurement.UNIDAD)
                                .build();

                Batch batch = Batch.builder()
                                .id(1L)
                                .quantity(10) // 10 batches
                                .packaging(packaging)
                                .build();

                Material material1 = Material.builder()
                                .id(1L)
                                .name("Material 1")
                                .type(MaterialType.MALTA)
                                .build();

                Recipe recipe1 = Recipe.builder()
                                .id(1L)
                                .material(material1)
                                .quantity(2.0) // 2.0 units of material per standard quantity
                                .build();

                ProductionOrder productionOrder = ProductionOrder.builder()
                                .id(1L)
                                .batch(batch)
                                .product(product)
                                .status(OrderStatus.PENDIENTE)
                                .quantity(100.0) // Total: 10 batches * 10 units = 100 units
                                .creationDate(OffsetDateTime.now())
                                .build();

                ProductionOrderResponseDTO responseDTO = new ProductionOrderResponseDTO();

                when(productRepository.findById(1L)).thenReturn(Optional.of(product));
                when(batchService.createBatch(createDTO, product)).thenReturn(batch);
                when(recipeService.getRecipeByProduct(1L)).thenReturn(List.of(recipe1));
                when(productionOrderRepository.saveAndFlush(any(ProductionOrder.class))).thenReturn(productionOrder);
                when(productionOrderMapper.toResponseDTO(productionOrder)).thenReturn(responseDTO);

                // Act
                service.createProductionOrder(createDTO);

                // Assert - Verify the material reservation
                ArgumentCaptor<List<MovementSimpleCreateDTO>> movementsCaptor = ArgumentCaptor.forClass(List.class);
                verify(movementService).createReserveOrReturn(eq(MovementType.RESERVA), movementsCaptor.capture());

                List<MovementSimpleCreateDTO> reservedMaterials = movementsCaptor.getValue();
                assertEquals(1, reservedMaterials.size());

                MovementSimpleCreateDTO reservedMaterial = reservedMaterials.get(0);
                assertEquals(material1, reservedMaterial.getMaterial());
                // materialQuantityMultiplier = 100 / 10 = 10
                // reserved stock = 2.0 * 10 = 20.0
                assertEquals(20.0, reservedMaterial.getStock());
        }

        @Test
        void approveOrder_success() {
                // Arrange
                Long orderId = 1L;

                Product product = Product.builder()
                                .id(1L)
                                .name("Test Product")
                                .standardQuantity(10.0)
                                .unitMeasurement(UnitMeasurement.UNIDAD)
                                .build();

                ProductionOrder productionOrder = ProductionOrder.builder()
                                .id(orderId)
                                .product(product)
                                .status(OrderStatus.PENDIENTE)
                                .quantity(100.0)
                                .creationDate(OffsetDateTime.now())
                                .build();

                Material material1 = Material.builder()
                                .id(1L)
                                .name("Material 1")
                                .type(MaterialType.MALTA)
                                .build();

                Recipe recipe1 = Recipe.builder()
                                .id(1L)
                                .material(material1)
                                .quantity(5.0)
                                .build();

                ProductionOrderResponseDTO responseDTO = new ProductionOrderResponseDTO();

                when(productionOrderRepository.findById(orderId)).thenReturn(Optional.of(productionOrder));
                when(recipeService.getRecipeByProduct(1L)).thenReturn(List.of(recipe1));
                when(productionOrderRepository.save(any(ProductionOrder.class))).thenReturn(productionOrder);
                when(productionOrderMapper.toResponseDTO(productionOrder)).thenReturn(responseDTO);

                // Act
                ProductionOrderResponseDTO result = service.approveOrder(orderId);

                // Assert
                assertSame(responseDTO, result);
                assertEquals(OrderStatus.APROBADA, productionOrder.getStatus());
                assertNotNull(productionOrder.getValidationDate());
                verify(productionOrderRepository).findById(orderId);
                verify(recipeService).getRecipeByProduct(1L);
                verify(movementService).confirmReservation(anyList());
                verify(productionOrderRepository).save(productionOrder);
        }

        @Test
        void approveOrder_orderNotFound_throws() {
                // Arrange
                Long orderId = 999L;
                when(productionOrderRepository.findById(orderId)).thenReturn(Optional.empty());

                // Act & Assert
                assertThrows(BadRequestException.class, () -> service.approveOrder(orderId));
                verify(productionOrderRepository).findById(orderId);
                verify(movementService, never()).confirmReservation(anyList());
                verify(productionOrderRepository, never()).save(any());
        }

        @Test
        void approveOrder_orderNotPending_throws() {
                // Arrange
                Long orderId = 1L;

                Product product = Product.builder()
                                .id(1L)
                                .name("Test Product")
                                .standardQuantity(10.0)
                                .build();

                ProductionOrder productionOrder = ProductionOrder.builder()
                                .id(orderId)
                                .product(product)
                                .status(OrderStatus.APROBADA) // Already approved
                                .quantity(100.0)
                                .build();

                when(productionOrderRepository.findById(orderId)).thenReturn(Optional.of(productionOrder));

                // Act & Assert
                assertThrows(BadRequestException.class, () -> service.approveOrder(orderId));
                verify(productionOrderRepository).findById(orderId);
                verify(movementService, never()).confirmReservation(anyList());
                verify(productionOrderRepository, never()).save(any());
        }

        @Test
        void approveOrder_verifiesCorrectMaterialConfirmation() {
                // Arrange
                Long orderId = 1L;

                Product product = Product.builder()
                                .id(1L)
                                .name("Test Product")
                                .standardQuantity(10.0)
                                .unitMeasurement(UnitMeasurement.UNIDAD)
                                .build();

                ProductionOrder productionOrder = ProductionOrder.builder()
                                .id(orderId)
                                .product(product)
                                .status(OrderStatus.PENDIENTE)
                                .quantity(100.0)
                                .creationDate(OffsetDateTime.now())
                                .build();

                Material material1 = Material.builder()
                                .id(1L)
                                .name("Material 1")
                                .type(MaterialType.MALTA)
                                .build();

                Material material2 = Material.builder()
                                .id(2L)
                                .name("Material 2")
                                .type(MaterialType.LEVADURA)
                                .build();

                Recipe recipe1 = Recipe.builder()
                                .id(1L)
                                .material(material1)
                                .quantity(2.0)
                                .build();

                Recipe recipe2 = Recipe.builder()
                                .id(2L)
                                .material(material2)
                                .quantity(1.5)
                                .build();

                ProductionOrderResponseDTO responseDTO = new ProductionOrderResponseDTO();

                when(productionOrderRepository.findById(orderId)).thenReturn(Optional.of(productionOrder));
                when(recipeService.getRecipeByProduct(1L)).thenReturn(List.of(recipe1, recipe2));
                when(productionOrderRepository.save(any(ProductionOrder.class))).thenReturn(productionOrder);
                when(productionOrderMapper.toResponseDTO(productionOrder)).thenReturn(responseDTO);

                // Act
                service.approveOrder(orderId);

                // Assert - Verify the material confirmation
                ArgumentCaptor<List<MovementSimpleCreateDTO>> movementsCaptor = ArgumentCaptor.forClass(List.class);
                verify(movementService).confirmReservation(movementsCaptor.capture());

                List<MovementSimpleCreateDTO> confirmedMaterials = movementsCaptor.getValue();
                assertEquals(2, confirmedMaterials.size());

                // materialQuantityMultiplier = 100 / 10 = 10
                // material1: 2.0 * 10 = 20.0
                // material2: 1.5 * 10 = 15.0
                assertEquals(20.0, confirmedMaterials.get(0).getStock());
                assertEquals(15.0, confirmedMaterials.get(1).getStock());
        }

        @Test
        void returnOrder_toCancelled_success() {
                // Arrange
                Long orderId = 1L;

                Product product = Product.builder()
                                .id(1L)
                                .name("Test Product")
                                .standardQuantity(10.0)
                                .unitMeasurement(UnitMeasurement.UNIDAD)
                                .build();

                ProductionOrder productionOrder = ProductionOrder.builder()
                                .id(orderId)
                                .product(product)
                                .status(OrderStatus.PENDIENTE)
                                .quantity(100.0)
                                .creationDate(OffsetDateTime.now())
                                .build();

                Material material1 = Material.builder()
                                .id(1L)
                                .name("Material 1")
                                .type(MaterialType.MALTA)
                                .build();

                Recipe recipe1 = Recipe.builder()
                                .id(1L)
                                .material(material1)
                                .quantity(5.0)
                                .build();

                ProductionOrderResponseDTO responseDTO = new ProductionOrderResponseDTO();

                when(productionOrderRepository.findById(orderId)).thenReturn(Optional.of(productionOrder));
                when(recipeService.getRecipeByProduct(1L)).thenReturn(List.of(recipe1));
                when(productionOrderRepository.save(any(ProductionOrder.class))).thenReturn(productionOrder);
                when(productionOrderMapper.toResponseDTO(productionOrder)).thenReturn(responseDTO);

                // Act
                ProductionOrderResponseDTO result = service.returnOrder(orderId, OrderStatus.CANCELADA);

                // Assert
                assertSame(responseDTO, result);
                assertEquals(OrderStatus.CANCELADA, productionOrder.getStatus());
                assertNotNull(productionOrder.getValidationDate());
                verify(productionOrderRepository).findById(orderId);
                verify(recipeService).getRecipeByProduct(1L);
                verify(movementService).createReserveOrReturn(eq(MovementType.DEVUELTO), anyList());
                verify(productionOrderRepository).save(productionOrder);
        }

        @Test
        void returnOrder_toRejected_success() {
                // Arrange
                Long orderId = 1L;

                Product product = Product.builder()
                                .id(1L)
                                .name("Test Product")
                                .standardQuantity(10.0)
                                .unitMeasurement(UnitMeasurement.UNIDAD)
                                .build();

                ProductionOrder productionOrder = ProductionOrder.builder()
                                .id(orderId)
                                .product(product)
                                .status(OrderStatus.PENDIENTE)
                                .quantity(100.0)
                                .creationDate(OffsetDateTime.now())
                                .build();

                Material material1 = Material.builder()
                                .id(1L)
                                .name("Material 1")
                                .type(MaterialType.MALTA)
                                .build();

                Recipe recipe1 = Recipe.builder()
                                .id(1L)
                                .material(material1)
                                .quantity(5.0)
                                .build();

                ProductionOrderResponseDTO responseDTO = new ProductionOrderResponseDTO();

                when(productionOrderRepository.findById(orderId)).thenReturn(Optional.of(productionOrder));
                when(recipeService.getRecipeByProduct(1L)).thenReturn(List.of(recipe1));
                when(productionOrderRepository.save(any(ProductionOrder.class))).thenReturn(productionOrder);
                when(productionOrderMapper.toResponseDTO(productionOrder)).thenReturn(responseDTO);

                // Act
                ProductionOrderResponseDTO result = service.returnOrder(orderId, OrderStatus.RECHAZADA);

                // Assert
                assertSame(responseDTO, result);
                assertEquals(OrderStatus.RECHAZADA, productionOrder.getStatus());
                verify(movementService).createReserveOrReturn(eq(MovementType.DEVUELTO), anyList());
        }

        @Test
        void returnOrder_orderNotFound_throws() {
                // Arrange
                Long orderId = 999L;
                when(productionOrderRepository.findById(orderId)).thenReturn(Optional.empty());

                // Act & Assert
                assertThrows(BadRequestException.class, () -> service.returnOrder(orderId, OrderStatus.CANCELADA));
                verify(productionOrderRepository).findById(orderId);
                verify(movementService, never()).createReserveOrReturn(any(), anyList());
                verify(productionOrderRepository, never()).save(any());
        }

        @Test
        void returnOrder_orderNotPending_throws() {
                // Arrange
                Long orderId = 1L;

                Product product = Product.builder()
                                .id(1L)
                                .name("Test Product")
                                .standardQuantity(10.0)
                                .build();

                ProductionOrder productionOrder = ProductionOrder.builder()
                                .id(orderId)
                                .product(product)
                                .status(OrderStatus.APROBADA) // Already approved
                                .quantity(100.0)
                                .build();

                when(productionOrderRepository.findById(orderId)).thenReturn(Optional.of(productionOrder));

                // Act & Assert
                assertThrows(BadRequestException.class, () -> service.returnOrder(orderId, OrderStatus.CANCELADA));
                verify(productionOrderRepository).findById(orderId);
                verify(movementService, never()).createReserveOrReturn(any(), anyList());
                verify(productionOrderRepository, never()).save(any());
        }

        @Test
        void returnOrder_toPendingStatus_throws() {
                // Arrange
                Long orderId = 1L;

                Product product = Product.builder()
                                .id(1L)
                                .name("Test Product")
                                .standardQuantity(10.0)
                                .build();

                ProductionOrder productionOrder = ProductionOrder.builder()
                                .id(orderId)
                                .product(product)
                                .status(OrderStatus.PENDIENTE)
                                .quantity(100.0)
                                .build();

                when(productionOrderRepository.findById(orderId)).thenReturn(Optional.of(productionOrder));

                // Act & Assert
                assertThrows(BadRequestException.class, () -> service.returnOrder(orderId, OrderStatus.PENDIENTE));
                verify(productionOrderRepository).findById(orderId);
                verify(movementService, never()).createReserveOrReturn(any(), anyList());
                verify(productionOrderRepository, never()).save(any());
        }

        @Test
        void returnOrder_verifiesCorrectMaterialReturn() {
                // Arrange
                Long orderId = 1L;

                Product product = Product.builder()
                                .id(1L)
                                .name("Test Product")
                                .standardQuantity(10.0)
                                .unitMeasurement(UnitMeasurement.UNIDAD)
                                .build();

                ProductionOrder productionOrder = ProductionOrder.builder()
                                .id(orderId)
                                .product(product)
                                .status(OrderStatus.PENDIENTE)
                                .quantity(100.0)
                                .creationDate(OffsetDateTime.now())
                                .build();

                Material material1 = Material.builder()
                                .id(1L)
                                .name("Material 1")
                                .type(MaterialType.MALTA)
                                .build();

                Recipe recipe1 = Recipe.builder()
                                .id(1L)
                                .material(material1)
                                .quantity(3.0)
                                .build();

                ProductionOrderResponseDTO responseDTO = new ProductionOrderResponseDTO();

                when(productionOrderRepository.findById(orderId)).thenReturn(Optional.of(productionOrder));
                when(recipeService.getRecipeByProduct(1L)).thenReturn(List.of(recipe1));
                when(productionOrderRepository.save(any(ProductionOrder.class))).thenReturn(productionOrder);
                when(productionOrderMapper.toResponseDTO(productionOrder)).thenReturn(responseDTO);

                // Act
                service.returnOrder(orderId, OrderStatus.CANCELADA);

                // Assert - Verify the material return
                ArgumentCaptor<List<MovementSimpleCreateDTO>> movementsCaptor = ArgumentCaptor.forClass(List.class);
                verify(movementService).createReserveOrReturn(eq(MovementType.DEVUELTO), movementsCaptor.capture());

                List<MovementSimpleCreateDTO> returnedMaterials = movementsCaptor.getValue();
                assertEquals(1, returnedMaterials.size());

                MovementSimpleCreateDTO returnedMaterial = returnedMaterials.get(0);
                assertEquals(material1, returnedMaterial.getMaterial());
                // materialQuantityMultiplier = 100 / 10 = 10
                // returned stock = 3.0 * 10 = 30.0
                assertEquals(30.0, returnedMaterial.getStock());
        }

        @Test
        void getProductionOrder_success() {
                // Arrange
                Long orderId = 1L;

                ProductionOrder productionOrder = ProductionOrder.builder()
                                .id(orderId)
                                .status(OrderStatus.PENDIENTE)
                                .quantity(100.0)
                                .build();

                ProductionOrderResponseDTO responseDTO = new ProductionOrderResponseDTO();

                when(productionOrderRepository.findById(orderId)).thenReturn(Optional.of(productionOrder));
                when(productionOrderMapper.toResponseDTO(productionOrder)).thenReturn(responseDTO);

                // Act
                ProductionOrderResponseDTO result = service.getProductionOrder(orderId);

                // Assert
                assertSame(responseDTO, result);
                verify(productionOrderRepository).findById(orderId);
                verify(productionOrderMapper).toResponseDTO(productionOrder);
        }

        @Test
        void getProductionOrder_notFound_throws() {
                // Arrange
                Long orderId = 999L;
                when(productionOrderRepository.findById(orderId)).thenReturn(Optional.empty());

                // Act & Assert
                assertThrows(ResourceNotFoundException.class, () -> service.getProductionOrder(orderId));
                verify(productionOrderRepository).findById(orderId);
                verify(productionOrderMapper, never()).toResponseDTO(any());
        }

        @Test
        void findAll_success() {
                // Arrange
                ProductionOrderFilterDTO filterDTO = new ProductionOrderFilterDTO();
                Pageable pageable = PageRequest.of(0, 10);

                ProductionOrder order1 = ProductionOrder.builder()
                                .id(1L)
                                .status(OrderStatus.PENDIENTE)
                                .quantity(100.0)
                                .build();

                ProductionOrder order2 = ProductionOrder.builder()
                                .id(2L)
                                .status(OrderStatus.APROBADA)
                                .quantity(200.0)
                                .build();

                Page<ProductionOrder> ordersPage = new PageImpl<>(List.of(order1, order2), pageable, 2);
                ProductionOrderResponseDTO responseDTO1 = new ProductionOrderResponseDTO();
                ProductionOrderResponseDTO responseDTO2 = new ProductionOrderResponseDTO();

                when(productionOrderRepository.findAll(any(Specification.class), any(Pageable.class)))
                                .thenReturn(ordersPage);
                when(productionOrderMapper.toResponseDTO(order1)).thenReturn(responseDTO1);
                when(productionOrderMapper.toResponseDTO(order2)).thenReturn(responseDTO2);

                // Act
                Page<ProductionOrderResponseDTO> result = service.findAll(filterDTO, pageable);

                // Assert
                assertEquals(2, result.getContent().size());
                assertEquals(2, result.getTotalElements());
                assertSame(responseDTO1, result.getContent().get(0));
                assertSame(responseDTO2, result.getContent().get(1));
                verify(productionOrderRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        void findAll_emptyResults() {
                // Arrange
                ProductionOrderFilterDTO filterDTO = new ProductionOrderFilterDTO();
                Pageable pageable = PageRequest.of(0, 10);

                Page<ProductionOrder> emptyPage = new PageImpl<>(List.of(), pageable, 0);

                when(productionOrderRepository.findAll(any(Specification.class), any(Pageable.class)))
                                .thenReturn(emptyPage);

                // Act
                Page<ProductionOrderResponseDTO> result = service.findAll(filterDTO, pageable);

                // Assert
                assertEquals(0, result.getContent().size());
                assertEquals(0, result.getTotalElements());
                verify(productionOrderRepository).findAll(any(Specification.class), any(Pageable.class));
        }
}
