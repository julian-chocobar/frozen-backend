package com.enigcode.frozen_backend.batches.service;

import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.repository.BatchRepository;
import com.enigcode.frozen_backend.production_materials.repository.ProductionMaterialRepository;
import com.enigcode.frozen_backend.production_phases.repository.ProductionPhaseRepository;
import com.enigcode.frozen_backend.production_phases_qualities.repository.ProductionPhaseQualityRepository;
import com.enigcode.frozen_backend.production_orders.Model.ProductionOrder;
import com.enigcode.frozen_backend.products.model.Product;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.production_orders.Model.OrderStatus;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.batches.model.BatchStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchTraceabilityServiceTest {

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private ProductionPhaseRepository productionPhaseRepository;

    @Mock
    private ProductionMaterialRepository productionMaterialRepository;

    @Mock
    private ProductionPhaseQualityRepository productionPhaseQualityRepository;

    @InjectMocks
    private BatchTraceabilityService traceabilityService;

    private Batch batchWithOrder;

    @BeforeEach
    void setUp() {
        // Build a minimal batch with associated production order, product and packaging
        Packaging packaging = new Packaging();
        packaging.setName("PKG");
        packaging.setQuantity(1.0);
        packaging.setUnitMeasurement(UnitMeasurement.UNIDAD);

        Product product = new Product();
        product.setName("Test Product");
        product.setUnitMeasurement(UnitMeasurement.UNIDAD);
        product.setStandardQuantity(1.0);

        ProductionOrder order = new ProductionOrder();
        order.setId(10L);
        order.setProduct(product);
        order.setCreationDate(OffsetDateTime.now());
        order.setStatus(OrderStatus.PENDIENTE);

        batchWithOrder = new Batch();
        batchWithOrder.setId(1L);
        batchWithOrder.setCode("BATCH-TEST");
        batchWithOrder.setPackaging(packaging);
        batchWithOrder.setProductionOrder(order);
        batchWithOrder.setStatus(BatchStatus.PENDIENTE);
        batchWithOrder.setQuantity(50);
        batchWithOrder.setCreationDate(OffsetDateTime.now());
        batchWithOrder.setPlannedDate(OffsetDateTime.now().plusDays(1));
    }

    @Test
    void getBatchTraceabilityData_batchNotFound_throws() {
        when(batchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> traceabilityService.getBatchTraceabilityData(999L));
    }

    @Test
    void getBatchTraceabilityData_noProductionOrder_throws() {
        Batch batch = new Batch();
        batch.setId(2L);
        batch.setCode("NO_ORDER_BATCH");
        when(batchRepository.findById(2L)).thenReturn(Optional.of(batch));

        assertThrows(ResourceNotFoundException.class, () -> traceabilityService.getBatchTraceabilityData(2L));
    }

    @Test
    void generateTraceabilityPDF_withMinimalData_returnsBytes() {
        when(batchRepository.findById(1L)).thenReturn(Optional.of(batchWithOrder));
        when(productionPhaseRepository.findAllByBatchIdOrderByPhaseOrderAsc(1L)).thenReturn(List.of());

        byte[] pdf = traceabilityService.generateTraceabilityPDF(1L);

        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(0);
    }
}
