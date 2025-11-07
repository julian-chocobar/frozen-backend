package com.enigcode.frozen_backend.production_materials.repository;

import com.enigcode.frozen_backend.production_materials.model.ProductionMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
