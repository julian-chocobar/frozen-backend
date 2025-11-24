package com.enigcode.frozen_backend.analytics.controller;

import com.enigcode.frozen_backend.analytics.DTO.DashboardStatsDTO;
import com.enigcode.frozen_backend.analytics.DTO.MonthlyTotalDTO;
import com.enigcode.frozen_backend.analytics.service.AnalyticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    @MockitoBean
    private com.enigcode.frozen_backend.common.SecurityProperties securityProperties;

    @Test
    @DisplayName("GET /analytics/monthly-production without auth -> 401")
    void getMonthlyProduction_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/analytics/monthly-production"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /analytics/monthly-production with auth -> 200")
    void getMonthlyProduction_authenticated_returns200() throws Exception {
        Mockito.when(analyticsService.getMonthlyProduction(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Collections.emptyList());
        mockMvc.perform(get("/analytics/monthly-production"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /analytics/monthly-material-consumption without auth -> 401")
    void getMonthlyMaterialConsumption_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/analytics/monthly-material-consumption"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /analytics/monthly-material-consumption with auth -> 200")
    void getMonthlyMaterialConsumption_authenticated_returns200() throws Exception {
        Mockito.when(analyticsService.getMonthlyMaterialConsumption(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Collections.emptyList());
        mockMvc.perform(get("/analytics/monthly-material-consumption"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /analytics/monthly-waste without auth -> 401")
    void getMonthlyWaste_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/analytics/monthly-waste"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /analytics/monthly-waste with auth -> 200")
    void getMonthlyWaste_authenticated_returns200() throws Exception {
        Mockito.when(analyticsService.getMonthlyWaste(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean()))
                .thenReturn(Collections.emptyList());
        mockMvc.perform(get("/analytics/monthly-waste"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /analytics/dashboard/monthly without auth -> 401")
    void getDashboardMonthly_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/analytics/dashboard/monthly"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /analytics/dashboard/monthly with auth -> 200")
    void getDashboardMonthly_authenticated_returns200() throws Exception {
        Mockito.when(analyticsService.getDashboardStats())
                .thenReturn(new DashboardStatsDTO());
        mockMvc.perform(get("/analytics/dashboard/monthly"))
                .andExpect(status().isOk());
    }
}
