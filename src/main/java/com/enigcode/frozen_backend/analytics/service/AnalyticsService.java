package com.enigcode.frozen_backend.analytics.service;

import com.enigcode.frozen_backend.analytics.DTO.MonthlyTotalDTO;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {
    List<MonthlyTotalDTO> getMonthlyProduction(LocalDate startDate, LocalDate endDate, Long productId);
}
