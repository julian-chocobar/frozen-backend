package com.enigcode.frozen_backend.batches.controller;

import com.enigcode.frozen_backend.batches.DTO.BatchResponseDTO;
import com.enigcode.frozen_backend.batches.model.BatchStatus;
import com.enigcode.frozen_backend.batches.service.BatchService;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.common.exceptions_configs.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(BatchController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@WithMockUser
class BatchControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BatchService batchService;
    @MockBean
    private com.enigcode.frozen_backend.common.SecurityProperties securityProperties;

    // --- Tests originales ---

    @Test
    void getAllBatchesReturnsOk() throws Exception {
        Page<BatchResponseDTO> page = new PageImpl<>(Collections.emptyList());
        when(batchService.findAll(any(), any())).thenReturn(page);
        
        mockMvc.perform(get("/batches"))
                .andExpect(status().isOk());
    }

    @Test
    void getBatchByIdReturnsOk() throws Exception {
        BatchResponseDTO dto = new BatchResponseDTO();
        dto.setCode("BATCH-001");
        when(batchService.getBatch(anyLong())).thenReturn(dto);
        
        mockMvc.perform(get("/batches/1"))
                .andExpect(status().isOk());
    }

    // --- Nuevos tests para cancelación ---

    @Test
    @WithMockUser(authorities = "ROLE_GERENTE_DE_PLANTA")
    void cancelBatch_success_returns200() throws Exception {
        // Given
        BatchResponseDTO dto = new BatchResponseDTO();
        dto.setCode("BATCH-001");
        dto.setStatus(BatchStatus.CANCELADO);

        when(batchService.cancelBatch(1L)).thenReturn(dto);

        // When/Then
        mockMvc.perform(patch("/batches/cancel-batch/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("BATCH-001"))
                .andExpect(jsonPath("$.status").value("CANCELADO"));

        verify(batchService).cancelBatch(1L);
    }

    @Test
    @WithMockUser(authorities = "ROLE_GERENTE_DE_PLANTA")
    void cancelBatch_notFound_returns404() throws Exception {
        // Given
        when(batchService.cancelBatch(999L)).thenThrow(new ResourceNotFoundException("No se encontró lote con id 999"));

        // When/Then
        mockMvc.perform(patch("/batches/cancel-batch/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // NOTA: Tests de autorización (@PreAuthorize) están cubiertos en BatchIntegrationTest
    // ya que @WebMvcTest con addFilters=false no aplica filtros de seguridad

    // --- Nuevos tests para processBatchesForToday ---

    @Test
    @WithMockUser(authorities = "ROLE_GERENTE_DE_PLANTA")
    void processBatchesForToday_success_returns204() throws Exception {
        // Given
        doNothing().when(batchService).processBatchesForToday();

        // When/Then
        mockMvc.perform(post("/batches/process-today")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(batchService).processBatchesForToday();
    }

    // NOTA: Test de autorización para processBatchesForToday cubierto en BatchIntegrationTest

    // --- Test de filtros ---

    @Test
    @WithMockUser(authorities = "ROLE_SUPERVISOR_DE_PRODUCCION")
    void getBatches_withFilters_returns200() throws Exception {
        // Given
        BatchResponseDTO dto1 = new BatchResponseDTO();
        dto1.setCode("BATCH-001");
        dto1.setStatus(BatchStatus.EN_PRODUCCION);

        Page<BatchResponseDTO> page = new PageImpl<>(Collections.singletonList(dto1));
        when(batchService.findAll(any(), any())).thenReturn(page);

        // When/Then
        mockMvc.perform(get("/batches")
                        .param("status", "EN_PRODUCCION")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalItems").exists())
                .andExpect(jsonPath("$.totalPages").exists());

        verify(batchService).findAll(any(), any());
    }
}
