package com.enigcode.frozen_backend.production_phases_qualities.repository;

import com.enigcode.frozen_backend.production_phases_qualities.model.ProductionPhaseQuality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionPhaseQualityRepository extends JpaRepository<ProductionPhaseQuality, Long> {
    List<ProductionPhaseQuality> findAllByProductionPhase_Batch_Id(Long batchId);

    List<ProductionPhaseQuality> findAllByProductionPhaseId(Long phaseId);

    /**
     * Obtiene todos los parámetros de calidad activos de una fase
     */
    List<ProductionPhaseQuality> findAllByProductionPhaseIdAndIsActiveTrue(Long phaseId);

    /**
     * Obtiene todos los parámetros de calidad de una fase para una versión
     * específica
     */
    List<ProductionPhaseQuality> findAllByProductionPhaseIdAndVersion(Long phaseId, Integer version);

    /**
     * Obtiene todos los parámetros de calidad activos de una fase para una versión
     * específica
     */
    List<ProductionPhaseQuality> findAllByProductionPhaseIdAndVersionAndIsActiveTrue(Long phaseId, Integer version);

    /**
     * Obtiene la versión más alta de parámetros para una fase específica
     */
    @Query("SELECT MAX(ppq.version) FROM ProductionPhaseQuality ppq WHERE ppq.productionPhase.id = :phaseId")
    Integer findMaxVersionByProductionPhaseId(@Param("phaseId") Long phaseId);

    /**
     * Obtiene todos los parámetros activos de un lote (solo versiones activas)
     */
    List<ProductionPhaseQuality> findAllByProductionPhase_Batch_IdAndIsActiveTrue(Long batchId);
}
