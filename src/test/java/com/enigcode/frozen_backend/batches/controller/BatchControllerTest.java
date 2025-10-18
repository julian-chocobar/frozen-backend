package com.enigcode.frozen_backend.batches.controller;

import com.enigcode.frozen_backend.batches.DTO.BatchResponseDTO;
import com.enigcode.frozen_backend.batches.service.BatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BatchController.class)
class BatchControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BatchService batchService;

    @Test
    void getAllBatchesReturnsOk() throws Exception {
        Page<BatchResponseDTO> page = new PageImpl<>(Collections.emptyList());
        when(batchService.findAll(any(), any())).thenReturn(page);
        
        mockMvc.perform(get("/batches")
                .with(httpBasic("test", "test")))
                .andExpect(status().isOk());
    }

    @Test
    void getBatchByIdReturnsOk() throws Exception {
        BatchResponseDTO dto = new BatchResponseDTO();
        dto.setCode("BATCH-001");
        when(batchService.getBatch(anyLong())).thenReturn(dto);
        
        mockMvc.perform(get("/batches/1")
                .with(httpBasic("test", "test")))
                .andExpect(status().isOk());
    }
}
