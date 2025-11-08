package com.enigcode.frozen_backend.sectors.repository;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.sectors.model.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectorRepository extends JpaRepository<Sector, Long>, JpaSpecificationExecutor<Sector> {
    @Query("""
    SELECT s
    FROM Sector s
    WHERE s.type = 'PRODUCCION'
    AND s.phase = :phase
    AND s.isActive = true
    AND (s.actualProduction < s.productionCapacity)
    ORDER BY s.actualProduction ASC
    """)
    List<Sector> findAvailableProductionSectorsByPhase(@Param("phase") Phase phase);
}
