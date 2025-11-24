package com.enigcode.frozen_backend.analytics.repository;

import com.enigcode.frozen_backend.analytics.DTO.GeneralDashboardProjectionDTO;
import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.model.BatchStatus;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.production_materials.model.ProductionMaterial;
import com.enigcode.frozen_backend.production_orders.Model.ProductionOrder;
import com.enigcode.frozen_backend.production_orders.Model.OrderStatus;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhaseStatus;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AnalyticsRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Test
    void getDashboardDataReturnsAggregatedMetrics() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = now.minusDays(15);
        OffsetDateTime end = now.plusDays(1);

        // Setup materials
        Material material = Material.builder()
                .name("Test Material")
                .type(MaterialType.MALTA)
                .unitMeasurement(UnitMeasurement.KG)
                .stock(100.0)
                .threshold(10.0)
                .creationDate(now)
                .build();
        em.persist(material);

        // Setup packaging
        Packaging packaging = Packaging.builder()
                .name("Test Package")
                .packagingMaterial(material)
                .labelingMaterial(material)
                .quantity(1.0)
                .unitMeasurement(UnitMeasurement.UNIDAD)
                .creationDate(now)
                .build();
        em.persist(packaging);

        // Setup batches
        Batch batchInProgress = Batch.builder()
                .packaging(packaging)
                .status(BatchStatus.EN_PRODUCCION)
                .quantity(10)
                .creationDate(now.minusDays(5))
                .startDate(now.minusDays(5))
                .plannedDate(now.minusDays(5))
                .build();
        em.persist(batchInProgress);

        Batch batchCompleted = Batch.builder()
                .packaging(packaging)
                .status(BatchStatus.COMPLETADO)
                .quantity(20)
                .creationDate(now.minusDays(10))
                .startDate(now.minusDays(10))
                .plannedDate(now.minusDays(10))
                .completedDate(now.minusDays(2))
                .build();
        em.persist(batchCompleted);

        Batch batchCancelled = Batch.builder()
                .packaging(packaging)
                .status(BatchStatus.CANCELADO)
                .quantity(5)
                .creationDate(now.minusDays(8))
                .startDate(now.minusDays(8))
                .plannedDate(now.minusDays(8))
                .completedDate(now.minusDays(3))
                .build();
        em.persist(batchCancelled);

        // Setup production phases - ENVASADO completed (counts as produced)
        ProductionPhase envasadoPhase = ProductionPhase.builder()
                .batch(batchCompleted)
                .phase(Phase.ENVASADO)
                .status(ProductionPhaseStatus.COMPLETADA)
                .output(100.0)
                .productWaste(5.0)
                .movementWaste(2.0)
                .startDate(now.minusDays(3))
                .endDate(now.minusDays(2))
                .build();
        em.persist(envasadoPhase);

        // Setup production phase - REJECTED (counts as waste)
        ProductionPhase rejectedPhase = ProductionPhase.builder()
                .batch(batchInProgress)
                .phase(Phase.MOLIENDA)
                .status(ProductionPhaseStatus.RECHAZADA)
                .output(50.0)
                .movementWaste(3.0)
                .startDate(now.minusDays(6))
                .endDate(now.minusDays(5))
                .build();
        em.persist(rejectedPhase);

        // Setup production materials
        ProductionMaterial prodMat1 = ProductionMaterial.builder()
                .productionPhase(envasadoPhase)
                .material(material)
                .quantity(20.0)
                .build();
        em.persist(prodMat1);

        ProductionMaterial prodMat2 = ProductionMaterial.builder()
                .productionPhase(rejectedPhase)
                .material(material)
                .quantity(15.0)
                .build();
        em.persist(prodMat2);

        // Setup production orders
        ProductionOrder rejectedOrder = ProductionOrder.builder()
                .batch(batchCompleted)
                .product(null) // Product not needed for this test
                .quantity(30.0)
                .status(OrderStatus.RECHAZADA)
                .creationDate(now.minusDays(4))
                .build();
        em.persist(rejectedOrder);

        em.flush();

        // Execute query
        GeneralDashboardProjectionDTO result = analyticsRepository.getDashboardData(start, end);

        // Assertions
        assertThat(result).isNotNull();
        
        // Total produced: ENVASADO completed = 100.0
        assertThat(result.getTotalProduced()).isEqualTo(100.0);

        // Total waste: 
        // - Completed phase (envasado): productWaste(5) + movementWaste(2) = 7
        // - Rejected phase: output(50) + movementWaste(3) = 53
        // Total = 60.0
        assertThat(result.getTotalWaste()).isEqualTo(60.0);

        // Total materials: both phases are completed/rejected = 20 + 15 = 35.0
        assertThat(result.getTotalMaterialsUsed()).isEqualTo(35.0);

        // Batches in progress: 1 (batchInProgress started in range)
        assertThat(result.getBatchesInProgress()).isEqualTo(1L);

        // Batches completed: 1 (batchCompleted)
        assertThat(result.getBatchesCompleted()).isEqualTo(1L);

        // Batches cancelled: 1 (batchCancelled)
        assertThat(result.getBatchesCancelled()).isEqualTo(1L);

        // Orders rejected: 1 (rejectedOrder)
        assertThat(result.getOrdersRejected()).isEqualTo(1L);
    }
}
