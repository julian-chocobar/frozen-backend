package com.enigcode.frozen_backend.production_materials.repository;

import com.enigcode.frozen_backend.analytics.DTO.MonthlyTotalProjectionDTO;
import com.enigcode.frozen_backend.production_materials.model.ProductionMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ProductionMaterialRepository extends JpaRepository<ProductionMaterial, Long> {
    /**
     * Obtiene todos los ProductionMaterial asociados a un Batch específico.
     *
     * @param batchId ID del Batch
     * @return Lista de ProductionMaterial pertenecientes al batch indicado
     */
    @Query("""
        SELECT pm
        FROM ProductionMaterial pm
        JOIN pm.productionPhase pp
        WHERE pp.batch.id = :batchId
        """)
    List<ProductionMaterial> findAllByBatchId(@Param("batchId") Long batchId);

    List<ProductionMaterial> findAllByProductionPhaseId(Long id);

    @Query("""
        SELECT
            YEAR(pm.productionPhase.endDate) AS year,
            MONTH(pm.productionPhase.endDate) AS month,
            SUM(pm.quantity) AS total
        FROM ProductionMaterial pm
        WHERE pm.productionPhase.status IN ('COMPLETADA', 'RECHAZADA')
          AND pm.productionPhase.endDate BETWEEN :start AND :end
          AND (:materialId IS NULL OR pm.material.id = :materialId)
        GROUP BY YEAR(pm.productionPhase.endDate), MONTH(pm.productionPhase.endDate)
        ORDER BY YEAR(pm.productionPhase.endDate), MONTH(pm.productionPhase.endDate)
        """)
    List<MonthlyTotalProjectionDTO> getMonthlyMaterialConsumption(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("materialId") Long materialId
    );
}
