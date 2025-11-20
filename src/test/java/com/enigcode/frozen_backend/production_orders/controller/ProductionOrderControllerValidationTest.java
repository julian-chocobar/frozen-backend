package com.enigcode.frozen_backend.production_orders.controller;

import com.enigcode.frozen_backend.common.exceptions_configs.GlobalExceptionHandler;
import com.enigcode.frozen_backend.production_orders.Controller.ProductionOrderController;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderResponseDTO;
import com.enigcode.frozen_backend.production_orders.Model.OrderStatus;
import com.enigcode.frozen_backend.production_orders.Service.ProductionOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductionOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ProductionOrderControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductionOrderService productionOrderService;

    // SecurityProperties is required by the test slices in this project
    @MockitoBean
    private com.enigcode.frozen_backend.common.SecurityProperties securityProperties;

    private String buildCreateJson(OffsetDateTime plannedDate) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("productId", 1L);
        body.put("quantity", 10.0);
        body.put("packagingId", 2L);
        body.put("plannedDate", plannedDate);
        return objectMapper.writeValueAsString(body);
    }

    private void mockSuccessfulCreateResponse() {
        ProductionOrderResponseDTO response = ProductionOrderResponseDTO.builder()
                .id(1L)
                .status(OrderStatus.PENDIENTE)
                .plannedDate(OffsetDateTime.now())
                .build();
        Mockito.when(productionOrderService.createProductionOrder(Mockito.any()))
                .thenReturn(response);
    }

    @Test
    @DisplayName("POST /production-orders with past plannedDate -> 400 with validation message")
    void createProductionOrder_pastDate_returns400() throws Exception {
        OffsetDateTime yesterday = OffsetDateTime.now().minusDays(1);
        String json = buildCreateJson(yesterday);

        mockMvc.perform(post("/production-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.plannedDate").value("La fecha de planificación debe ser una fecha futura o presente"));
    }

    @Test
    @DisplayName("POST /production-orders with today plannedDate -> 201")
    void createProductionOrder_todayDate_returns201() throws Exception {
        mockSuccessfulCreateResponse();
        OffsetDateTime today = OffsetDateTime.now();
        String json = buildCreateJson(today);

        mockMvc.perform(post("/production-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /production-orders with future plannedDate -> 201")
    void createProductionOrder_futureDate_returns201() throws Exception {
        mockSuccessfulCreateResponse();
        OffsetDateTime future = OffsetDateTime.now().plusDays(3);
        String json = buildCreateJson(future);

        mockMvc.perform(post("/production-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /production-orders with null plannedDate -> 400 contains field error")
    void createProductionOrder_nullPlannedDate_returns400() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("productId", 1L);
        body.put("quantity", 10.0);
        body.put("packagingId", 2L);
        body.put("plannedDate", null);
        String json = objectMapper.writeValueAsString(body);

        mockMvc.perform(post("/production-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.plannedDate").exists());
    }
}
