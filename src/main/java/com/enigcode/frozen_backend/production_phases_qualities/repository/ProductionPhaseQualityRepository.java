package com.enigcode.frozen_backend.production_phases_qualities.repository;

import com.enigcode.frozen_backend.production_phases_qualities.model.ProductionPhaseQuality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionPhaseQualityRepository extends JpaRepository<ProductionPhaseQuality, Long> {
}
