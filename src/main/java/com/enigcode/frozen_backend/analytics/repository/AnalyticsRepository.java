package com.enigcode.frozen_backend.analytics.repository;

import com.enigcode.frozen_backend.analytics.DTO.GeneralDashboardProjectionDTO;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.Repository;

import java.time.OffsetDateTime;

public interface AnalyticsRepository extends Repository<ProductionPhase, Long> {

    @Query("""
    SELECT
        /* TOTAL PRODUCIDO → fase ENVASADO COMPLETADA */
        (SELECT COALESCE(SUM(pp.output), 0)
         FROM ProductionPhase pp
         WHERE pp.endDate BETWEEN :start AND :end
           AND pp.phase = 'ENVASADO'
           AND pp.status = 'COMPLETADA'
        ) AS totalProduced,
    
        /* TOTAL DESPERDICIO (productWaste + movementWaste) 
           Si la fase está RECHAZADA → se desperdicia todo el output */
        (SELECT COALESCE(SUM(
            CASE 
                WHEN pp.status = 'COMPLETADA'
                    THEN COALESCE(pp.productWaste, 0) + COALESCE(pp.movementWaste, 0)
    
                WHEN pp.status = 'RECHAZADA'
                    THEN COALESCE(pp.output, 0) + COALESCE(pp.movementWaste, 0)
    
                ELSE 0
            END
        ), 0)
         FROM ProductionPhase pp
         WHERE pp.endDate BETWEEN :start AND :end
        ) AS totalWaste,
    
        /* TOTAL MATERIALES USADOS → solo fases completadas o rechazadas */
        (SELECT COALESCE(SUM(pm.quantity), 0)
         FROM ProductionMaterial pm
         JOIN pm.productionPhase pp
         WHERE pp.endDate BETWEEN :start AND :end
           AND pp.status IN ('COMPLETADA', 'RECHAZADA')
        ) AS totalMaterialsUsed,
    
        /* LOTES EN PRODUCCIÓN */
        (SELECT COUNT(b)
         FROM Batch b
         WHERE b.startDate BETWEEN :start AND :end
           AND b.status = 'EN_PRODUCCION'
        ) AS batchesInProgress,
    
        /* LOTES COMPLETADOS */
        (SELECT COUNT(b)
         FROM Batch b
         WHERE b.completedDate BETWEEN :start AND :end
           AND b.status = 'COMPLETADO'
        ) AS batchesCompleted,
    
        /* LOTES CANCELADOS */
        (SELECT COUNT(b)
         FROM Batch b
         WHERE b.completedDate BETWEEN :start AND :end
           AND b.status = 'CANCELADO'
        ) AS batchesCancelled,
        
        /* ORDENES RECHAZADOS */
        (SELECT COUNT(b)
         FROM ProductionOrder b
         WHERE b.creationDate BETWEEN :start AND :end
           AND b.status = 'RECHAZADA'
        ) AS ordersRejected
        
    FROM ProductionPhase pp2
    WHERE pp2.id = (SELECT MIN(pp3.id) FROM ProductionPhase pp3)
    """)
    GeneralDashboardProjectionDTO getDashboardData(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );
}
