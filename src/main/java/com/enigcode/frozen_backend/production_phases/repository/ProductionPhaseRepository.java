package com.enigcode.frozen_backend.production_phases.repository;

import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionPhaseRepository extends JpaRepository<ProductionPhase, Long> {
}
