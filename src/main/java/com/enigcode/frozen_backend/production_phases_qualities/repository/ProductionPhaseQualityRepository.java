package com.enigcode.frozen_backend.production_phases_qualities.repository;

import com.enigcode.frozen_backend.production_phases_qualities.model.ProductionPhaseQuality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionPhaseQualityRepository extends JpaRepository<ProductionPhaseQuality, Long> {
    List<ProductionPhaseQuality> findAllByProductionPhase_Batch_Id(Long batchId);
    List<ProductionPhaseQuality> findAllByProductionPhaseId(Long phaseId);
}
