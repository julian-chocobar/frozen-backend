package com.enigcode.frozen_backend.quality_parameters.repository;

import com.enigcode.frozen_backend.quality_parameters.model.QualityParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface QualityParameterRepository extends JpaRepository<QualityParameter, Long>, JpaSpecificationExecutor<QualityParameter> {
}
