package com.enigcode.frozen_backend.analytics.service;

import com.enigcode.frozen_backend.analytics.DTO.DashboardStatsDTO;
import com.enigcode.frozen_backend.analytics.DTO.GeneralDashboardProjectionDTO;
import com.enigcode.frozen_backend.analytics.DTO.MonthlyTotalDTO;
import com.enigcode.frozen_backend.analytics.DTO.MonthlyTotalProjectionDTO;
import com.enigcode.frozen_backend.analytics.mapper.AnalyticsMapper;
import com.enigcode.frozen_backend.analytics.repository.AnalyticsRepository;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.production_materials.repository.ProductionMaterialRepository;
import com.enigcode.frozen_backend.production_phases.repository.ProductionPhaseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService{

    private static final ZoneOffset BA_OFFSET = ZoneOffset.of("-03:00");

    private final ProductionPhaseRepository productionPhaseRepository;
    private final ProductionMaterialRepository productionMaterialRepository;
    private final AnalyticsRepository analyticsRepository;
    private final AnalyticsMapper analyticsMapper;


    @Transactional
    @Cacheable(value = "analytics", key = "'production:' + T(com.enigcode.frozen_backend.analytics.service.AnalyticsServiceImpl).normalizeCacheKey(#startDate, #endDate, #productId)")
    @Override
    public List<MonthlyTotalDTO> getMonthlyProduction(LocalDate startDate, LocalDate endDate, Long productId) {
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusYears(1);
        }

        OffsetDateTime startODT = startDate.atStartOfDay().atOffset(BA_OFFSET);
        OffsetDateTime endODT = endDate.atTime(LocalTime.MAX).atOffset(BA_OFFSET);

        List<MonthlyTotalProjectionDTO> results =
                productionPhaseRepository.getFinalProductionByMonth(startODT, endODT, productId);

        return analyticsMapper.toMonthlyTotalDTOList(results);
    }

    @Transactional
    @Cacheable(value = "analytics", key = "'materials:' + T(com.enigcode.frozen_backend.analytics.service.AnalyticsServiceImpl).normalizeCacheKey(#startDate, #endDate, #materialId)")
    @Override
    public List<MonthlyTotalDTO> getMonthlyMaterialConsumption(LocalDate startDate, LocalDate endDate, Long materialId) {
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusYears(1);
        }

        OffsetDateTime startODT = startDate.atStartOfDay().atOffset(BA_OFFSET);
        OffsetDateTime endODT = endDate.atTime(LocalTime.MAX).atOffset(BA_OFFSET);

        List<MonthlyTotalProjectionDTO> results =
                productionMaterialRepository.getMonthlyMaterialConsumption(startODT, endODT, materialId);

        return analyticsMapper.toMonthlyTotalDTOList(results);
    }

    @Transactional
    @Cacheable(value = "analytics", key = "'waste:' + T(com.enigcode.frozen_backend.analytics.service.AnalyticsServiceImpl).normalizeCacheKey(#startDate, #endDate, null) + ':' + (#phase != null ? #phase.toString() : 'null') + ':' + #movementOnly")
    @Override
    public List<MonthlyTotalDTO> getMonthlyWaste(LocalDate startDate, LocalDate endDate, Phase phase, boolean movementOnly) {
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusYears(1);
        }

        OffsetDateTime startODT = startDate.atStartOfDay().atOffset(BA_OFFSET);
        OffsetDateTime endODT = endDate.atTime(LocalTime.MAX).atOffset(BA_OFFSET);

        if(movementOnly){
            List<MonthlyTotalProjectionDTO> results =
                    productionPhaseRepository.getMonthlyMovementWaste(startODT,endODT);
            return analyticsMapper.toMonthlyTotalDTOList(results);
        }

        List<MonthlyTotalProjectionDTO> results =
                productionPhaseRepository.getMonthlyWaste(startODT,endODT,phase);
        return analyticsMapper.toMonthlyTotalDTOList(results);
    }

    @Transactional
    @Cacheable(value = "analytics", key = "'dashboard:stats'")
    @Override
    public DashboardStatsDTO getDashboardStats() {
        OffsetDateTime end = OffsetDateTime.now();
        OffsetDateTime start = end.minusMonths(1);

        GeneralDashboardProjectionDTO dto = analyticsRepository.getDashboardData(start, end);

        return  analyticsMapper.toDashboardStatsDTO(dto);
    }

    @Transactional
    @Cacheable(value = "analytics", key = "'efficiency:' + T(com.enigcode.frozen_backend.analytics.service.AnalyticsServiceImpl).normalizeCacheKey(#startDate, #endDate, #productId) + ':' + (#phase != null ? #phase.toString() : 'null')")
    @Override
    public List<MonthlyTotalDTO> getMonthlyEfficiency(LocalDate startDate, LocalDate endDate, Long productId, Phase phase) {
        // Establecer fechas por defecto si no se proporcionan (último año)
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusYears(1);
        }

        // Convertir a OffsetDateTime para la consulta
        OffsetDateTime startODT = startDate.atStartOfDay().atOffset(BA_OFFSET);
        OffsetDateTime endODT = endDate.atTime(LocalTime.MAX).atOffset(BA_OFFSET);

        List<MonthlyTotalProjectionDTO> production;
        List<MonthlyTotalProjectionDTO> materials;

        // Si hay filtro de fase, usar input/output de esa fase específica
        if (phase != null) {
            // Obtener output de la fase específica
            production = analyticsRepository.getMonthlyProductionByPhase(startODT, endODT, productId, phase);
            // Obtener input de la fase específica (materiales que entran a esa fase)
            materials = analyticsRepository.getMonthlyInputByPhase(startODT, endODT, productId, phase);
        } else {
            // Sin filtro de fase: usar producción final de ENVASADO y materiales totales
            production = analyticsRepository.getMonthlyProduction(startODT, endODT, productId);
            materials = analyticsRepository.getMonthlyMaterialsTotal(startODT, endODT, productId, null);
        }
        
        var materialsByMonth = materials.stream()
                .collect(java.util.stream.Collectors.toMap(
                        m -> m.getYear() + "-" + m.getMonth(),
                        MonthlyTotalProjectionDTO::getTotal
                ));

        // Calcular eficiencia neta por mes
        // Eficiencia = (Producción Final / Materiales Totales) × 100
        // Cuando hay filtro de fase: Eficiencia = (Output de la fase / Input de la fase) × 100
        // La producción ya es el resultado después de todas las pérdidas
        return production.stream()
                .map(prod -> {
                    String key = prod.getYear() + "-" + prod.getMonth();
                    Double materialsUsed = materialsByMonth.getOrDefault(key, 0.0);
                    
                    // Calcular eficiencia
                    Double efficiency = 0.0;
                    if (materialsUsed > 0) {
                        efficiency = (prod.getTotal() / materialsUsed) * 100.0;
                    }
                    
                    return MonthlyTotalDTO.builder()
                            .month(prod.getYear() + "-" + String.format("%02d", prod.getMonth()))
                            .total(efficiency)
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Normaliza las fechas para generar claves de caché consistentes.
     * Si las fechas son null, usa el rango por defecto (último año).
     * Esto asegura que múltiples llamadas sin parámetros usen la misma clave de caché.
     */
    public static String normalizeCacheKey(LocalDate startDate, LocalDate endDate, Long filterId) {
        if (startDate == null || endDate == null) {
            // Usar "default" para el rango por defecto (último año)
            return "default:" + (filterId != null ? filterId : "null");
        }
        return startDate.toString() + ":" + endDate.toString() + ":" + (filterId != null ? filterId : "null");
    }
}
