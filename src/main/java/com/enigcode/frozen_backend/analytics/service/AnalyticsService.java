package com.enigcode.frozen_backend.analytics.service;

import com.enigcode.frozen_backend.analytics.DTO.MonthlyTotalDTO;
import com.enigcode.frozen_backend.product_phases.model.Phase;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {
    List<MonthlyTotalDTO> getMonthlyProduction(LocalDate startDate, LocalDate endDate, Long productId);
    List<MonthlyTotalDTO> getMonthlyMaterialConsumption(LocalDate startDate, LocalDate endDate, Long materialId);
    List<MonthlyTotalDTO> getMonthlyWaste(LocalDate startDate, LocalDate endDate, Phase phase, boolean transferOnly);
}
