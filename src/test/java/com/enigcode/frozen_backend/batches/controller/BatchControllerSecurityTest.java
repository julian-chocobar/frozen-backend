package com.enigcode.frozen_backend.batches.controller;

import com.enigcode.frozen_backend.batches.DTO.BatchResponseDTO;
import com.enigcode.frozen_backend.batches.service.BatchService;
import com.enigcode.frozen_backend.batches.service.BatchTraceabilityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(BatchController.class)
class BatchControllerSecurityTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BatchService batchService;
    @MockitoBean
    private BatchTraceabilityService batchTraceabilityService;
    @MockitoBean
    private com.enigcode.frozen_backend.common.SecurityProperties securityProperties;

    @Test
    void getAllBatchesRequiresAuth() throws Exception {
        mockMvc.perform(get("/batches"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getAllBatchesWithValidAuthReturnsOk() throws Exception {
        Page<BatchResponseDTO> page = new PageImpl<>(Collections.emptyList());
        when(batchService.findAll(any(), any())).thenReturn(page);
        
        mockMvc.perform(get("/batches"))
                .andExpect(status().isOk());
    }
}