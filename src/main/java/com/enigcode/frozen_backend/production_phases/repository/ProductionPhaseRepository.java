package com.enigcode.frozen_backend.production_phases.repository;

import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionPhaseRepository extends JpaRepository<ProductionPhase, Long> {
    boolean existsByIdAndSector_Supervisor_Id(Long phaseId, Long userId);

    List<ProductionPhase> findAllByBatchIdOrderByPhaseOrderAsc(Long id);
}
