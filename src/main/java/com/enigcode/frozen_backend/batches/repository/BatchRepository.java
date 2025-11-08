package com.enigcode.frozen_backend.batches.repository;

import com.enigcode.frozen_backend.batches.model.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long>, JpaSpecificationExecutor<Batch> {
    @Query("""
    SELECT b
    FROM Batch b
    WHERE b.startDate >= :startOfDay
      AND b.startDate < :endOfDay
      AND b.status = 'PENDIENTE'
      ORDER BY b.id ASC
    """)
    List<Batch> findAllStartingToday(@Param("startOfDay") OffsetDateTime startOfDay,
                                         @Param("endOfDay") OffsetDateTime endOfDay);

}
