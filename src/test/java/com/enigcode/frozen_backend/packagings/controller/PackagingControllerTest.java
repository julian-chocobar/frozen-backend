package com.enigcode.frozen_backend.packagings.controller;
import com.enigcode.frozen_backend.common.exceptions_configs.GlobalExceptionHandler;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.packagings.service.PackagingService;
import com.enigcode.frozen_backend.packagings.DTO.PackagingCreateDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PackagingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PackagingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PackagingService packagingService;
    @Autowired
    private ObjectMapper objectMapper;
    private String packagingJson;

    @BeforeEach
    void setup() throws Exception {
        PackagingCreateDTO dto = PackagingCreateDTO.builder()
            .name("Caja de cartón")
            .materialId(1L)
            .unitMeasurement(com.enigcode.frozen_backend.materials.model.UnitMeasurement.UNIDAD)
            .quantity(20.0)
            .build();
        packagingJson = objectMapper.writeValueAsString(dto);

        when(packagingService.createPackaging(any())).thenReturn(null);
        when(packagingService.updatePackaging(any(Long.class), any())).thenReturn(null);
        when(packagingService.toggleActive(any(Long.class))).thenReturn(null);
        when(packagingService.getPackaging(any(Long.class))).thenReturn(null);
        when(packagingService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(java.util.List.of()));
    }

    @Test
    void testCreatePackaging() throws Exception {
        mockMvc.perform(post("/packagings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(packagingJson))
                .andExpect(status().isCreated());
    }

    @Test
    void testCreatePackaging_BadRequest_ShouldReturn400() throws Exception {
        when(packagingService.createPackaging(any())).thenThrow(new BadRequestException("Duplicado"));
        mockMvc.perform(post("/packagings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(packagingJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdatePackaging() throws Exception {
        String updateJson = """
                {
                    "capacity": 25
                }
                """;

        mockMvc.perform(patch("/packagings/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());
    }

    @Test
    void testToggleActive() throws Exception {
        mockMvc.perform(patch("/packagings/1/toggle-active"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetPackaging() throws Exception {
        mockMvc.perform(get("/packagings/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetPackaging_NotFound_ShouldReturn404() throws Exception {
        when(packagingService.getPackaging(any(Long.class))).thenThrow(new ResourceNotFoundException("No encontrado"));
        mockMvc.perform(get("/packagings/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetPackagings() throws Exception {
        mockMvc.perform(get("/packagings"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetPackagings_PaginationMetadata_ShouldReturnFields() throws Exception {
        when(packagingService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(java.util.List.of(), PageRequest.of(1, 5), 0));

        mockMvc.perform(get("/packagings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(true))
                .andExpect(jsonPath("$.isFirst").value(false))
                .andExpect(jsonPath("$.isLast").value(true));
    }
}
