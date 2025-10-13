package com.enigcode.frozen_backend.product_phases.repository;

import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductPhaseRepository extends JpaRepository<ProductPhase, Long> {
}
