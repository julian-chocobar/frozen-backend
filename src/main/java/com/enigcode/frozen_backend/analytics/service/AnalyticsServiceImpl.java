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
}
