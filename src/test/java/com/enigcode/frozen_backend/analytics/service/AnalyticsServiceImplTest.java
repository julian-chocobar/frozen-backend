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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private ProductionPhaseRepository productionPhaseRepository;

    @Mock
    private ProductionMaterialRepository productionMaterialRepository;

    @Mock
    private AnalyticsRepository analyticsRepository;

    @Mock
    private AnalyticsMapper analyticsMapper;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private static final ZoneOffset BA_OFFSET = ZoneOffset.of("-03:00");

    // ==================== Monthly Production Tests ====================

    @Test
    void getMonthlyProductionWithDatesAndProductId() {
        MonthlyTotalProjectionDTO projection = mock(MonthlyTotalProjectionDTO.class);
        MonthlyTotalDTO dto = new MonthlyTotalDTO();
        dto.setMonth("2024-11");
        dto.setTotal(100.0);

        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        Long productId = 1L;

        OffsetDateTime startODT = startDate.atStartOfDay().atOffset(BA_OFFSET);
        OffsetDateTime endODT = endDate.atTime(LocalTime.MAX).atOffset(BA_OFFSET);

        when(productionPhaseRepository.getFinalProductionByMonth(startODT, endODT, productId))
                .thenReturn(List.of(projection));
        when(analyticsMapper.toMonthlyTotalDTOList(List.of(projection)))
                .thenReturn(List.of(dto));

        List<MonthlyTotalDTO> result = analyticsService.getMonthlyProduction(startDate, endDate, productId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("2024-11", result.get(0).getMonth());
        assertEquals(100.0, result.get(0).getTotal());

        verify(productionPhaseRepository).getFinalProductionByMonth(startODT, endODT, productId);
        verify(analyticsMapper).toMonthlyTotalDTOList(List.of(projection));
    }

    @Test
    void getMonthlyProductionDefaultsToLastYearWhenDatesNull() {
        MonthlyTotalProjectionDTO projection = mock(MonthlyTotalProjectionDTO.class);
        MonthlyTotalDTO dto = new MonthlyTotalDTO();
        dto.setMonth("2024-11");
        dto.setTotal(100.0);

        when(productionPhaseRepository.getFinalProductionByMonth(any(), any(), eq(null)))
                .thenReturn(List.of(projection));
        when(analyticsMapper.toMonthlyTotalDTOList(List.of(projection)))
                .thenReturn(List.of(dto));

        List<MonthlyTotalDTO> result = analyticsService.getMonthlyProduction(null, null, null);

        assertNotNull(result);
        verify(productionPhaseRepository).getFinalProductionByMonth(any(OffsetDateTime.class), any(OffsetDateTime.class), eq(null));
    }

    // ==================== Monthly Material Consumption Tests ====================

    @Test
    void getMonthlyMaterialConsumptionWithDatesAndMaterialId() {
        MonthlyTotalProjectionDTO projection = mock(MonthlyTotalProjectionDTO.class);
        MonthlyTotalDTO dto = new MonthlyTotalDTO();
        dto.setMonth("2024-10");
        dto.setTotal(200.0);

        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        Long materialId = 2L;

        OffsetDateTime startODT = startDate.atStartOfDay().atOffset(BA_OFFSET);
        OffsetDateTime endODT = endDate.atTime(LocalTime.MAX).atOffset(BA_OFFSET);

        when(productionMaterialRepository.getMonthlyMaterialConsumption(startODT, endODT, materialId))
                .thenReturn(List.of(projection));
        when(analyticsMapper.toMonthlyTotalDTOList(List.of(projection)))
                .thenReturn(List.of(dto));

        List<MonthlyTotalDTO> result = analyticsService.getMonthlyMaterialConsumption(startDate, endDate, materialId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productionMaterialRepository).getMonthlyMaterialConsumption(startODT, endODT, materialId);
        verify(analyticsMapper).toMonthlyTotalDTOList(List.of(projection));
    }

    @Test
    void getMonthlyMaterialConsumptionDefaultsToLastYearWhenDatesNull() {
        MonthlyTotalProjectionDTO projection = mock(MonthlyTotalProjectionDTO.class);
        MonthlyTotalDTO dto = new MonthlyTotalDTO();

        when(productionMaterialRepository.getMonthlyMaterialConsumption(any(), any(), eq(null)))
                .thenReturn(List.of(projection));
        when(analyticsMapper.toMonthlyTotalDTOList(List.of(projection)))
                .thenReturn(List.of(dto));

        List<MonthlyTotalDTO> result = analyticsService.getMonthlyMaterialConsumption(null, null, null);

        assertNotNull(result);
        verify(productionMaterialRepository).getMonthlyMaterialConsumption(any(OffsetDateTime.class), any(OffsetDateTime.class), eq(null));
    }

    // ==================== Monthly Waste Tests ====================

    @Test
    void getMonthlyWasteWithPhase() {
        MonthlyTotalProjectionDTO projection = mock(MonthlyTotalProjectionDTO.class);
        MonthlyTotalDTO dto = new MonthlyTotalDTO();

        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        Phase phase = Phase.MOLIENDA;

        OffsetDateTime startODT = startDate.atStartOfDay().atOffset(BA_OFFSET);
        OffsetDateTime endODT = endDate.atTime(LocalTime.MAX).atOffset(BA_OFFSET);

        when(productionPhaseRepository.getMonthlyWaste(startODT, endODT, phase))
                .thenReturn(List.of(projection));
        when(analyticsMapper.toMonthlyTotalDTOList(List.of(projection)))
                .thenReturn(List.of(dto));

        List<MonthlyTotalDTO> result = analyticsService.getMonthlyWaste(startDate, endDate, phase, false);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productionPhaseRepository).getMonthlyWaste(startODT, endODT, phase);
        verify(productionPhaseRepository, never()).getMonthlyMovementWaste(any(), any());
    }

    @Test
    void getMonthlyWasteMovementOnly() {
        MonthlyTotalProjectionDTO projection = mock(MonthlyTotalProjectionDTO.class);
        MonthlyTotalDTO dto = new MonthlyTotalDTO();

        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        OffsetDateTime startODT = startDate.atStartOfDay().atOffset(BA_OFFSET);
        OffsetDateTime endODT = endDate.atTime(LocalTime.MAX).atOffset(BA_OFFSET);

        when(productionPhaseRepository.getMonthlyMovementWaste(startODT, endODT))
                .thenReturn(List.of(projection));
        when(analyticsMapper.toMonthlyTotalDTOList(List.of(projection)))
                .thenReturn(List.of(dto));

        List<MonthlyTotalDTO> result = analyticsService.getMonthlyWaste(startDate, endDate, null, true);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productionPhaseRepository).getMonthlyMovementWaste(startODT, endODT);
        verify(productionPhaseRepository, never()).getMonthlyWaste(any(), any(), any());
    }

    // ==================== Dashboard Stats Tests ====================

    @Test
    void getDashboardStats() {
        GeneralDashboardProjectionDTO projection = mock(GeneralDashboardProjectionDTO.class);
        DashboardStatsDTO dashboardDTO = new DashboardStatsDTO();
        dashboardDTO.setTotalProduced(500.0);
        dashboardDTO.setTotalWaste(50.0);
        dashboardDTO.setBatchesInProgress(3L);

        when(analyticsRepository.getDashboardData(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(projection);
        when(analyticsMapper.toDashboardStatsDTO(projection))
                .thenReturn(dashboardDTO);

        DashboardStatsDTO result = analyticsService.getDashboardStats();

        assertNotNull(result);
        assertEquals(500.0, result.getTotalProduced());
        assertEquals(50.0, result.getTotalWaste());
        assertEquals(3L, result.getBatchesInProgress());

        verify(analyticsRepository).getDashboardData(any(OffsetDateTime.class), any(OffsetDateTime.class));
        verify(analyticsMapper).toDashboardStatsDTO(projection);
    }
}
