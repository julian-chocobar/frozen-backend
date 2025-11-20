package com.enigcode.frozen_backend.production_orders.controller;

import com.enigcode.frozen_backend.common.exceptions_configs.GlobalExceptionHandler;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.production_orders.Controller.ProductionOrderController;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderResponseDTO;
import com.enigcode.frozen_backend.production_orders.Model.OrderStatus;
import com.enigcode.frozen_backend.production_orders.Service.ProductionOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductionOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ProductionOrderControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private ProductionOrderService productionOrderService;
        @MockitoBean
        private com.enigcode.frozen_backend.common.SecurityProperties securityProperties;

        @Autowired
        private ObjectMapper objectMapper;

        private String productionOrderJson;
        private ProductionOrderResponseDTO responseDTO;

        @BeforeEach
        void setup() throws Exception {
                ProductionOrderCreateDTO dto = ProductionOrderCreateDTO.builder()
                                .productId(1L)
                                .quantity(100.0)
                                .packagingId(1L)
                                .plannedDate(OffsetDateTime.now().plusDays(7))
                                .build();
                productionOrderJson = objectMapper.writeValueAsString(dto);

                responseDTO = ProductionOrderResponseDTO.builder()
                                .id(1L)
                                .batchId(1L)
                                .batchCode("BATCH-001")
                                .packagingName("Botella 500ml")
                                .productName("Cerveza IPA")
                                .status(OrderStatus.PENDIENTE)
                                .quantity(100.0)
                                .validationDate(null)
                                .build();

                when(productionOrderService.createProductionOrder(any())).thenReturn(responseDTO);
                when(productionOrderService.approveOrder(any(Long.class))).thenReturn(responseDTO);
                when(productionOrderService.returnOrder(any(Long.class), any(OrderStatus.class)))
                                .thenReturn(responseDTO);
                when(productionOrderService.getProductionOrder(any(Long.class))).thenReturn(responseDTO);
                when(productionOrderService.findAll(any(), any(Pageable.class)))
                                .thenReturn(new PageImpl<>(java.util.List.of()));
        }

        @Test
        void testCreateProductionOrder_Success() throws Exception {
                mockMvc.perform(post("/production-orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(productionOrderJson))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1L))
                                .andExpect(jsonPath("$.status").value("PENDIENTE"));
        }

        @Test
        void testCreateProductionOrder_MissingProductId_ShouldReturnBadRequest() throws Exception {
                String invalidJson = """
                                {
                                    "quantity": 100.0,
                                    "packagingId": 1,
                                    "plannedDate": "2025-10-25T10:00:00Z"
                                }
                                """;
                mockMvc.perform(post("/production-orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testCreateProductionOrder_MissingQuantity_ShouldReturnBadRequest() throws Exception {
                String invalidJson = """
                                {
                                    "productId": 1,
                                    "packagingId": 1,
                                    "plannedDate": "2025-10-25T10:00:00Z"
                                }
                                """;
                mockMvc.perform(post("/production-orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testCreateProductionOrder_NegativeQuantity_ShouldReturnBadRequest() throws Exception {
                String invalidJson = """
                                {
                                    "productId": 1,
                                    "quantity": -10.0,
                                    "packagingId": 1,
                                    "plannedDate": "2025-10-25T10:00:00Z"
                                }
                                """;
                mockMvc.perform(post("/production-orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testCreateProductionOrder_MissingPackagingId_ShouldReturnBadRequest() throws Exception {
                String invalidJson = """
                                {
                                    "productId": 1,
                                    "quantity": 100.0,
                                    "plannedDate": "2025-10-25T10:00:00Z"
                                }
                                """;
                mockMvc.perform(post("/production-orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testCreateProductionOrder_MissingPlannedDate_ShouldReturnBadRequest() throws Exception {
                String invalidJson = """
                                {
                                    "productId": 1,
                                    "quantity": 100.0,
                                    "packagingId": 1
                                }
                                """;
                mockMvc.perform(post("/production-orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testCreateProductionOrder_ProductNotReady_ShouldReturn400() throws Exception {
                when(productionOrderService.createProductionOrder(any()))
                                .thenThrow(new BadRequestException("El producto no esta listo para produccion"));

                mockMvc.perform(post("/production-orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(productionOrderJson))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testCreateProductionOrder_ProductNotFound_ShouldReturn404() throws Exception {
                when(productionOrderService.createProductionOrder(any()))
                                .thenThrow(new ResourceNotFoundException("No se encontró producto"));

                mockMvc.perform(post("/production-orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(productionOrderJson))
                                .andExpect(status().isNotFound());
        }

        @Test
        void testApproveOrder_Success() throws Exception {
                ProductionOrderResponseDTO approvedDTO = ProductionOrderResponseDTO.builder()
                                .id(1L)
                                .status(OrderStatus.APROBADA)
                                .build();

                when(productionOrderService.approveOrder(1L)).thenReturn(approvedDTO);

                mockMvc.perform(patch("/production-orders/1/approve"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("APROBADA"));
        }

        @Test
        void testApproveOrder_NotFound_ShouldReturn400() throws Exception {
                when(productionOrderService.approveOrder(999L))
                                .thenThrow(new BadRequestException("No se encontró orden de produccion"));

                mockMvc.perform(patch("/production-orders/999/approve"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testApproveOrder_NotPending_ShouldReturn400() throws Exception {
                when(productionOrderService.approveOrder(1L))
                                .thenThrow(new BadRequestException("La orden esta en estado APROBADO"));

                mockMvc.perform(patch("/production-orders/1/approve"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testCancelOrder_Success() throws Exception {
                ProductionOrderResponseDTO cancelledDTO = ProductionOrderResponseDTO.builder()
                                .id(1L)
                                .status(OrderStatus.CANCELADA)
                                .build();

                when(productionOrderService.returnOrder(eq(1L), eq(OrderStatus.CANCELADA))).thenReturn(cancelledDTO);

                mockMvc.perform(patch("/production-orders/1/cancel"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("CANCELADA"));
        }

        @Test
        void testCancelOrder_NotFound_ShouldReturn400() throws Exception {
                when(productionOrderService.returnOrder(eq(999L), eq(OrderStatus.CANCELADA)))
                                .thenThrow(new BadRequestException("No se encontró orden de produccion"));

                mockMvc.perform(patch("/production-orders/999/cancel"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testCancelOrder_NotPending_ShouldReturn400() throws Exception {
                when(productionOrderService.returnOrder(eq(1L), eq(OrderStatus.CANCELADA)))
                                .thenThrow(new BadRequestException("La orden esta en estado APROBADO"));

                mockMvc.perform(patch("/production-orders/1/cancel"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testRejectOrder_Success() throws Exception {
                ProductionOrderResponseDTO rejectedDTO = ProductionOrderResponseDTO.builder()
                                .id(1L)
                                .status(OrderStatus.RECHAZADA)
                                .build();

                when(productionOrderService.returnOrder(eq(1L), eq(OrderStatus.RECHAZADA))).thenReturn(rejectedDTO);

                mockMvc.perform(patch("/production-orders/1/reject"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("RECHAZADA"));
        }

        @Test
        void testRejectOrder_NotFound_ShouldReturn400() throws Exception {
                when(productionOrderService.returnOrder(eq(999L), eq(OrderStatus.RECHAZADA)))
                                .thenThrow(new BadRequestException("No se encontró orden de produccion"));

                mockMvc.perform(patch("/production-orders/999/reject"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testRejectOrder_NotPending_ShouldReturn400() throws Exception {
                when(productionOrderService.returnOrder(eq(1L), eq(OrderStatus.RECHAZADA)))
                                .thenThrow(new BadRequestException("La orden esta en estado APROBADO"));

                mockMvc.perform(patch("/production-orders/1/reject"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testGetProductionOrders_Success() throws Exception {
                mockMvc.perform(get("/production-orders"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        void testGetProductionOrders_WithPagination() throws Exception {
                mockMvc.perform(get("/production-orders")
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "creationDate,desc"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.currentPage").exists())
                                .andExpect(jsonPath("$.totalPages").exists())
                                .andExpect(jsonPath("$.totalItems").exists());
        }

        @Test
        void testGetProductionOrder_Success() throws Exception {
                mockMvc.perform(get("/production-orders/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1L))
                                .andExpect(jsonPath("$.status").value("PENDIENTE"));
        }

        @Test
        void testGetProductionOrder_NotFound_ShouldReturn404() throws Exception {
                when(productionOrderService.getProductionOrder(999L))
                                .thenThrow(new ResourceNotFoundException("No se encontro Orden de id 999"));

                mockMvc.perform(get("/production-orders/999"))
                                .andExpect(status().isNotFound());
        }
}
