package com.enigcode.frozen_backend.quality_parameters.repository;

import com.enigcode.frozen_backend.quality_parameters.model.QualityParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QualityParameterRepository extends JpaRepository<QualityParameter, Long> {
    List<QualityParameter> findByIsActiveTrueOrderByNameAsc();

    List<QualityParameter> findByPhaseAndIsActiveTrueOrderByNameAsc(
            com.enigcode.frozen_backend.product_phases.model.Phase phase);
}
