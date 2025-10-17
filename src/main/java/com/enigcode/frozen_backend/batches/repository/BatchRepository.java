package com.enigcode.frozen_backend.batches.repository;

import com.enigcode.frozen_backend.batches.model.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
}
