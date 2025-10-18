package com.enigcode.frozen_backend.production_orders.mapper;

import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.model.BatchStatus;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderResponseDTO;
import com.enigcode.frozen_backend.production_orders.Mapper.ProductionOrderMapper;
import com.enigcode.frozen_backend.production_orders.Model.OrderStatus;
import com.enigcode.frozen_backend.production_orders.Model.ProductionOrder;
import com.enigcode.frozen_backend.products.model.Product;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProductionOrderMapperTest {

    private final ProductionOrderMapper mapper = Mappers.getMapper(ProductionOrderMapper.class);

    @Test
    void testToEntity_fromCreateDTO() {
        // Arrange
        ProductionOrderCreateDTO createDTO = ProductionOrderCreateDTO.builder()
                .productId(1L)
                .quantity(100.0)
                .packagingId(2L)
                .plannedDate(OffsetDateTime.now())
                .build();

        // Act
        ProductionOrder entity = mapper.toEntity(createDTO);

        // Assert
        assertNotNull(entity);
        // El mapper solo mapea campos directos, los IDs de relaciones se manejan en el servicio
    }

    @Test
    void testToResponseDTO_allFieldsMapped() {
        // Arrange
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime plannedDate = now.plusDays(7);
        OffsetDateTime startDate = now.plusDays(1);
        OffsetDateTime completedDate = now.plusDays(5);
        OffsetDateTime estimatedCompletedDate = now.plusDays(6);

        Packaging packaging = Packaging.builder()
                .id(1L)
                .name("Botella 500ml")
                .quantity(12.0)
                .unitMeasurement(UnitMeasurement.UNIDAD)
                .build();

        Product product = Product.builder()
                .id(1L)
                .name("Cerveza IPA")
                .standardQuantity(100.0)
                .unitMeasurement(UnitMeasurement.LT)
                .isReady(true)
                .build();

        Batch batch = Batch.builder()
                .id(10L)
                .code("BATCH-001")
                .packaging(packaging)
                .status(BatchStatus.EN_PRODUCCION)
                .quantity(50)
                .creationDate(now)
                .plannedDate(plannedDate)
                .startDate(startDate)
                .completedDate(completedDate)
                .estimatedCompletedDate(estimatedCompletedDate)
                .build();

        ProductionOrder order = ProductionOrder.builder()
                .id(100L)
                .batch(batch)
                .product(product)
                .status(OrderStatus.APROBADO)
                .quantity(600.0)
                .creationDate(now)
                .validationDate(now.plusHours(2))
                .build();

        // Act
        ProductionOrderResponseDTO dto = mapper.toResponseDTO(order);

        // Assert
        assertNotNull(dto);
        assertEquals(100L, dto.getId());
        assertEquals(10L, dto.getBatchId());
        assertEquals("BATCH-001", dto.getBatchCode());
        assertEquals("Botella 500ml", dto.getPackagingName());
        assertEquals("Cerveza IPA", dto.getProductName());
        assertEquals(UnitMeasurement.LT, dto.getUnitMeasurement());
        assertEquals(OrderStatus.APROBADO, dto.getStatus());
        assertEquals(600.0, dto.getQuantity());
        assertEquals(now.plusHours(2), dto.getValidationDate());
        assertEquals(plannedDate, dto.getPlannedDate());
        assertEquals(startDate, dto.getStartDate());
        assertEquals(completedDate, dto.getCompletedDate());
        assertEquals(estimatedCompletedDate, dto.getEstimatedCompletedDate());
    }

    @Test
    void testToResponseDTO_withNullDates() {
        // Arrange
        OffsetDateTime now = OffsetDateTime.now();

        Packaging packaging = Packaging.builder()
                .id(1L)
                .name("Botella 500ml")
                .quantity(12.0)
                .unitMeasurement(UnitMeasurement.UNIDAD)
                .build();

        Product product = Product.builder()
                .id(1L)
                .name("Cerveza IPA")
                .standardQuantity(100.0)
                .unitMeasurement(UnitMeasurement.LT)
                .isReady(true)
                .build();

        Batch batch = Batch.builder()
                .id(10L)
                .code("BATCH-001")
                .packaging(packaging)
                .status(BatchStatus.PENDIENTE)
                .quantity(50)
                .creationDate(now)
                .plannedDate(now.plusDays(7))
                .startDate(null)
                .completedDate(null)
                .estimatedCompletedDate(null)
                .build();

        ProductionOrder order = ProductionOrder.builder()
                .id(100L)
                .batch(batch)
                .product(product)
                .status(OrderStatus.PENDIENTE)
                .quantity(600.0)
                .creationDate(now)
                .validationDate(null)
                .build();

        // Act
        ProductionOrderResponseDTO dto = mapper.toResponseDTO(order);

        // Assert
        assertNotNull(dto);
        assertEquals(100L, dto.getId());
        assertEquals(OrderStatus.PENDIENTE, dto.getStatus());
        assertNull(dto.getValidationDate());
        assertNull(dto.getStartDate());
        assertNull(dto.getCompletedDate());
        assertNull(dto.getEstimatedCompletedDate());
        assertNotNull(dto.getPlannedDate());
    }

    @Test
    void testToResponseDTO_pendingOrder() {
        // Arrange
        OffsetDateTime now = OffsetDateTime.now();

        Packaging packaging = Packaging.builder()
                .id(1L)
                .name("Lata 350ml")
                .quantity(24.0)
                .unitMeasurement(UnitMeasurement.UNIDAD)
                .build();

        Product product = Product.builder()
                .id(2L)
                .name("Cerveza Lager")
                .standardQuantity(50.0)
                .unitMeasurement(UnitMeasurement.LT)
                .isReady(true)
                .build();

        Batch batch = Batch.builder()
                .id(20L)
                .code("BATCH-002")
                .packaging(packaging)
                .status(BatchStatus.PENDIENTE)
                .quantity(100)
                .creationDate(now)
                .plannedDate(now.plusDays(3))
                .build();

        ProductionOrder order = ProductionOrder.builder()
                .id(200L)
                .batch(batch)
                .product(product)
                .status(OrderStatus.PENDIENTE)
                .quantity(2400.0)
                .creationDate(now)
                .build();

        // Act
        ProductionOrderResponseDTO dto = mapper.toResponseDTO(order);

        // Assert
        assertNotNull(dto);
        assertEquals(200L, dto.getId());
        assertEquals(20L, dto.getBatchId());
        assertEquals("BATCH-002", dto.getBatchCode());
        assertEquals("Lata 350ml", dto.getPackagingName());
        assertEquals("Cerveza Lager", dto.getProductName());
        assertEquals(UnitMeasurement.LT, dto.getUnitMeasurement());
        assertEquals(OrderStatus.PENDIENTE, dto.getStatus());
        assertEquals(2400.0, dto.getQuantity());
    }

    @Test
    void testToResponseDTO_cancelledOrder() {
        // Arrange
        OffsetDateTime now = OffsetDateTime.now();

        Packaging packaging = Packaging.builder()
                .id(1L)
                .name("Barril 50L")
                .quantity(50.0)
                .unitMeasurement(UnitMeasurement.LT)
                .build();

        Product product = Product.builder()
                .id(3L)
                .name("Cerveza Stout")
                .standardQuantity(100.0)
                .unitMeasurement(UnitMeasurement.LT)
                .isReady(true)
                .build();

        Batch batch = Batch.builder()
                .id(30L)
                .code("BATCH-003")
                .packaging(packaging)
                .status(BatchStatus.CANCELADO)
                .quantity(10)
                .creationDate(now)
                .plannedDate(now.plusDays(5))
                .build();

        ProductionOrder order = ProductionOrder.builder()
                .id(300L)
                .batch(batch)
                .product(product)
                .status(OrderStatus.CANCELADA)
                .quantity(500.0)
                .creationDate(now)
                .validationDate(now.plusHours(1))
                .build();

        // Act
        ProductionOrderResponseDTO dto = mapper.toResponseDTO(order);

        // Assert
        assertNotNull(dto);
        assertEquals(300L, dto.getId());
        assertEquals(OrderStatus.CANCELADA, dto.getStatus());
        assertEquals(30L, dto.getBatchId());
        assertEquals("Cerveza Stout", dto.getProductName());
        assertNotNull(dto.getValidationDate());
    }
}
