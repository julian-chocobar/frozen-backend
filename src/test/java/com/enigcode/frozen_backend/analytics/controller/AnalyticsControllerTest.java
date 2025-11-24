package com.enigcode.frozen_backend.analytics.controller;

import com.enigcode.frozen_backend.analytics.DTO.DashboardStatsDTO;
import com.enigcode.frozen_backend.analytics.DTO.MonthlyTotalDTO;
import com.enigcode.frozen_backend.analytics.service.AnalyticsService;
import com.enigcode.frozen_backend.common.exceptions_configs.GlobalExceptionHandler;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@WithMockUser
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    @MockitoBean
    private com.enigcode.frozen_backend.common.SecurityProperties securityProperties;

    @Test
    void getMonthlyProductionReturnsOk() throws Exception {
        MonthlyTotalDTO dto = new MonthlyTotalDTO();
        dto.setMonth("2024-11");
        dto.setTotal(150.0);

        when(analyticsService.getMonthlyProduction(any(), any(), anyLong()))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/analytics/monthly-production")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31")
                        .param("productId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].month").value("2024-11"))
                .andExpect(jsonPath("$[0].total").value(150.0));
    }

    @Test
    void getMonthlyMaterialConsumptionReturnsOk() throws Exception {
        MonthlyTotalDTO dto = new MonthlyTotalDTO();
        dto.setMonth("2024-10");
        dto.setTotal(200.0);

        when(analyticsService.getMonthlyMaterialConsumption(any(), any(), anyLong()))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/analytics/monthly-material-consumption")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31")
                        .param("materialId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].month").value("2024-10"))
                .andExpect(jsonPath("$[0].total").value(200.0));
    }

    @Test
    void getMonthlyWasteReturnsOk() throws Exception {
        MonthlyTotalDTO dto = new MonthlyTotalDTO();
        dto.setMonth("2024-09");
        dto.setTotal(50.0);

        when(analyticsService.getMonthlyWaste(any(), any(), any(Phase.class), anyBoolean()))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/analytics/monthly-waste")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31")
                        .param("phase", "MOLIENDA")
                        .param("transferOnly", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].month").value("2024-09"))
                .andExpect(jsonPath("$[0].total").value(50.0));
    }

    @Test
    void getMonthlyDashboardReturnsOk() throws Exception {
        DashboardStatsDTO dto = new DashboardStatsDTO();
        dto.setTotalProduced(500.0);
        dto.setTotalWaste(60.0);
        dto.setTotalMaterialsUsed(300.0);
        dto.setBatchesInProgress(3L);
        dto.setBatchesCompleted(10L);
        dto.setBatchesCancelled(2L);
        dto.setOrdersRejected(1L);

        when(analyticsService.getDashboardStats()).thenReturn(dto);

        mockMvc.perform(get("/analytics/dashboard/monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProduced").value(500.0))
                .andExpect(jsonPath("$.totalWaste").value(60.0))
                .andExpect(jsonPath("$.totalMaterialsUsed").value(300.0))
                .andExpect(jsonPath("$.batchesInProgress").value(3))
                .andExpect(jsonPath("$.batchesCompleted").value(10))
                .andExpect(jsonPath("$.batchesCancelled").value(2))
                .andExpect(jsonPath("$.ordersRejected").value(1));
    }
}
