package com.enigcode.frozen_backend.production_phases.repository;

import com.enigcode.frozen_backend.analytics.DTO.MonthlyTotalProjectionDTO;
import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ProductionPhaseRepository extends JpaRepository<ProductionPhase, Long> {
    boolean existsByIdAndSector_Supervisor_Id(Long phaseId, Long userId);
    List<ProductionPhase> findAllByBatchIdOrderByPhaseOrderAsc(Long id);

    @Query("""
        SELECT YEAR(pp.endDate) AS year,
               MONTH(pp.endDate) AS month,
               SUM(pp.output) AS total
        FROM ProductionPhase pp
        WHERE pp.phase = 'ENVASADO'
          AND pp.status = 'COMPLETADA'
          AND pp.endDate BETWEEN :start AND :end
          AND (:productId IS NULL OR pp.batch.productionOrder.product.id = :productId)
        GROUP BY YEAR(pp.endDate), MONTH(pp.endDate)
        ORDER BY YEAR(pp.endDate), MONTH(pp.endDate)
        """)
    List<MonthlyTotalProjectionDTO> getFinalProductionByMonth(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("productId") Long productId
    );

    @Query("""
    SELECT p
    FROM ProductionPhase p
    WHERE p.batch = :batch
      AND p.phaseOrder = :order - 1
    """)
    ProductionPhase findPreviousPhase(
            @Param("batch") Batch batch,
            @Param("order") Integer order
    );

    @Query("""
    SELECT
        YEAR(pp.endDate) AS year,
        MONTH(pp.endDate) AS month,
        SUM(
            CASE
                WHEN pp.status = 'COMPLETADA' THEN COALESCE(pp.productWaste, 0)
                WHEN pp.status = 'RECHAZADA' THEN COALESCE(pp.output, 0)
                ELSE 0
            END
            +
            CASE 
                WHEN :phase IS NULL THEN COALESCE(pp.movementWaste, 0)
                ELSE 0
            END
        ) AS total
    FROM ProductionPhase pp
    WHERE pp.endDate BETWEEN :start AND :end
      AND (:phase IS NULL OR pp.phase = :phase)
    GROUP BY YEAR(pp.endDate), MONTH(pp.endDate)
    ORDER BY YEAR(pp.endDate), MONTH(pp.endDate)
    """)
    List<MonthlyTotalProjectionDTO> getMonthlyWaste(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("phase") Phase phase
    );

    @Query("""
    SELECT
        YEAR(pp.endDate) AS year,
        MONTH(pp.endDate) AS month,
        SUM(COALESCE(pp.movementWaste, 0)) AS total
    FROM ProductionPhase pp
    WHERE pp.endDate BETWEEN :start AND :end
    GROUP BY YEAR(pp.endDate), MONTH(pp.endDate)
    ORDER BY YEAR(pp.endDate), MONTH(pp.endDate)
    """)
    List<MonthlyTotalProjectionDTO> getMonthlyMovementWaste(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

}
