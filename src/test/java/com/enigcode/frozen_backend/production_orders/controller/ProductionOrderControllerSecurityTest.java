package com.enigcode.frozen_backend.production_orders.controller;

import com.enigcode.frozen_backend.production_orders.Controller.ProductionOrderController;
import com.enigcode.frozen_backend.production_orders.Service.ProductionOrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductionOrderController.class)
class ProductionOrderControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductionOrderService productionOrderService;
    @MockBean
    private com.enigcode.frozen_backend.common.SecurityProperties securityProperties;

    @Test
    @DisplayName("GET /production-orders without auth -> 401")
    void getProductionOrders_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/production-orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /production-orders with auth -> 200")
    void getProductionOrders_authenticated_returns200() throws Exception {
        Mockito.when(productionOrderService.findAll(Mockito.any(), Mockito.any())).thenReturn(Page.empty());
        mockMvc.perform(get("/production-orders"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /production-orders/{id} without auth -> 401")
    void getProductionOrder_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/production-orders/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /production-orders/{id} with auth -> 200")
    void getProductionOrder_authenticated_returns200() throws Exception {
        Mockito.when(productionOrderService.getProductionOrder(Mockito.any())).thenReturn(null);
        mockMvc.perform(get("/production-orders/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /production-orders/{id}/approve without auth -> 401")
    void approveOrder_unauthenticated_returns401() throws Exception {
        mockMvc.perform(patch("/production-orders/1/approve").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /production-orders/{id}/approve with auth -> 200")
    void approveOrder_authenticated_returns200() throws Exception {
        Mockito.when(productionOrderService.approveOrder(Mockito.any())).thenReturn(null);
        mockMvc.perform(patch("/production-orders/1/approve").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /production-orders/{id}/cancel without auth -> 401")
    void cancelOrder_unauthenticated_returns401() throws Exception {
        mockMvc.perform(patch("/production-orders/1/cancel").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /production-orders/{id}/cancel with auth -> 200")
    void cancelOrder_authenticated_returns200() throws Exception {
        Mockito.when(productionOrderService.returnOrder(Mockito.any(), Mockito.any())).thenReturn(null);
        mockMvc.perform(patch("/production-orders/1/cancel").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /production-orders/{id}/reject without auth -> 401")
    void rejectOrder_unauthenticated_returns401() throws Exception {
        mockMvc.perform(patch("/production-orders/1/reject").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /production-orders/{id}/reject with auth -> 200")
    void rejectOrder_authenticated_returns200() throws Exception {
        Mockito.when(productionOrderService.returnOrder(Mockito.any(), Mockito.any())).thenReturn(null);
        mockMvc.perform(patch("/production-orders/1/reject").with(csrf()))
                .andExpect(status().isOk());
    }
}
