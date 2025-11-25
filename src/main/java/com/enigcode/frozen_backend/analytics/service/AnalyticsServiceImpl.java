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
    @Override
    public DashboardStatsDTO getDashboardStats() {
        OffsetDateTime end = OffsetDateTime.now();
        OffsetDateTime start = end.minusMonths(1);

        GeneralDashboardProjectionDTO dto = analyticsRepository.getDashboardData(start, end);

        return  analyticsMapper.toDashboardStatsDTO(dto);
    }

    @Transactional
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

        // Obtener datos mensuales con filtros
        List<MonthlyTotalProjectionDTO> production = analyticsRepository.getMonthlyProduction(startODT, endODT, productId);
        List<MonthlyTotalProjectionDTO> waste = analyticsRepository.getMonthlyWasteTotal(startODT, endODT, productId, phase);
        List<MonthlyTotalProjectionDTO> materials = analyticsRepository.getMonthlyMaterialsTotal(startODT, endODT, productId, phase);

        // Crear mapas por mes para facilitar el cálculo
        var wasteByMonth = waste.stream()
                .collect(java.util.stream.Collectors.toMap(
                        w -> w.getYear() + "-" + w.getMonth(),
                        MonthlyTotalProjectionDTO::getTotal
                ));
        
        var materialsByMonth = materials.stream()
                .collect(java.util.stream.Collectors.toMap(
                        m -> m.getYear() + "-" + m.getMonth(),
                        MonthlyTotalProjectionDTO::getTotal
                ));

        // Calcular eficiencia neta por mes
        // Eficiencia Neta = ((producción - desperdicio) / materiales) × 100
        // Representa el % de material que se convierte en producto útil
        return production.stream()
                .map(prod -> {
                    String key = prod.getYear() + "-" + prod.getMonth();
                    Double wasteAmount = wasteByMonth.getOrDefault(key, 0.0);
                    Double materialsUsed = materialsByMonth.getOrDefault(key, 0.0);
                    
                    // Calcular eficiencia neta
                    Double netEfficiency = 0.0;
                    if (materialsUsed > 0) {
                        Double usefulProduction = prod.getTotal() - wasteAmount;
                        netEfficiency = (usefulProduction / materialsUsed) * 100.0;
                    }
                    
                    return MonthlyTotalDTO.builder()
                            .month(prod.getYear() + "-" + String.format("%02d", prod.getMonth()))
                            .total(netEfficiency)
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
