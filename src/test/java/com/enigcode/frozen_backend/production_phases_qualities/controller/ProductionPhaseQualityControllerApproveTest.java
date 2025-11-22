package com.enigcode.frozen_backend.production_phases_qualities.controller;

import com.enigcode.frozen_backend.common.exceptions_configs.GlobalExceptionHandler;
import com.enigcode.frozen_backend.production_phases_qualities.DTO.ProductionPhaseQualityResponseDTO;
import com.enigcode.frozen_backend.production_phases_qualities.service.ProductionPhaseQualityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductionPhaseQualityControllerApproveTest {

    private MockMvc mockMvc;

    @Mock
    private ProductionPhaseQualityService service;

    @InjectMocks
    private ProductionPhaseQualityController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void approve_existing_returns200() throws Exception {
        ProductionPhaseQualityResponseDTO dto = ProductionPhaseQualityResponseDTO.builder().id(500L).isApproved(true).build();
        when(service.approveProductionPhaseQuality(eq(500L))).thenReturn(dto);

        mockMvc.perform(patch("/production-phases-qualities/500/approve")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(500))
                .andExpect(jsonPath("$.isApproved").value(true));
    }

    @Test
    void disapprove_existing_returns200() throws Exception {
        ProductionPhaseQualityResponseDTO dto = ProductionPhaseQualityResponseDTO.builder().id(501L).isApproved(false).build();
        when(service.disapproveProductionPhaseQuality(eq(501L))).thenReturn(dto);

        mockMvc.perform(patch("/production-phases-qualities/501/disapprove")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(501))
                .andExpect(jsonPath("$.isApproved").value(false));
    }
}
