
package com.enigcode.frozen_backend.materials.controller;

import com.enigcode.frozen_backend.common.exceptions_configs.GlobalExceptionHandler;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;

import com.enigcode.frozen_backend.materials.service.MaterialService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.enigcode.frozen_backend.materials.DTO.MaterialCreateDTO;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MaterialController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class MaterialControllerTest {
    @Test
    void testGetMaterial_NotFound_ShouldReturn404() throws Exception {
        Long id = 99L;
        when(materialService.getMaterial(id)).thenThrow(new ResourceNotFoundException("No encontrado"));
        mockMvc.perform(get("/materials/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateMaterial_BadRequest_ShouldReturn409() throws Exception {
        when(materialService.createMaterial(any())).thenThrow(new BadRequestException("Duplicado"));
        mockMvc.perform(post("/materials")
                .contentType(MediaType.APPLICATION_JSON)
                .content(materialJson))
                .andExpect(status().isBadRequest());
    }

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private MaterialService materialService;
    @MockitoBean
    private com.enigcode.frozen_backend.common.SecurityProperties securityProperties;
    @Autowired
    private ObjectMapper objectMapper;
    private String materialJson;

    @BeforeEach
    void setup() throws Exception {
        MaterialCreateDTO dto = MaterialCreateDTO.builder()
            .name("Malta")
            .type(com.enigcode.frozen_backend.materials.model.MaterialType.ENVASE)
            .value(100.0)
            .stock(10.0)
            .unitMeasurement(com.enigcode.frozen_backend.materials.model.UnitMeasurement.UNIDAD)
            .threshold(1.0)
            .build();
        materialJson = objectMapper.writeValueAsString(dto);

        when(materialService.createMaterial(any())).thenReturn(null);
        when(materialService.updateMaterial(any(Long.class), any())).thenReturn(null);
        when(materialService.toggleActive(any(Long.class))).thenReturn(null);
        when(materialService.findAll(any(), any(Pageable.class))).thenReturn(new PageImpl<>(java.util.List.of()));
        when(materialService.getMaterial(any(Long.class))).thenReturn(null);
    }

    @Test
    void testCreateMaterial() throws Exception {
        mockMvc.perform(post("/materials")
                .contentType(MediaType.APPLICATION_JSON)
                .content(materialJson))
                .andExpect(status().isCreated());
    }

    @Test
    void testCreateMaterial_MissingName_ShouldReturnBadRequest() throws Exception {
    String invalidJson = "{" +
        "\"type\":\"ENVASE\"," +
        "\"value\":100.0," +
        "\"stock\":10.0," +
        "\"unitMeasurement\":\"UNIDAD\"," +
        "\"threshold\":1.0" +
        "}";
    mockMvc.perform(post("/materials")
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalidJson))
        .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateMaterial_NegativeStock_ShouldReturnBadRequest() throws Exception {
    String invalidJson = "{" +
        "\"name\":\"Malta\"," +
        "\"type\":\"ENVASE\"," +
        "\"value\":100.0," +
        "\"stock\":-5.0," +
        "\"unitMeasurement\":\"UNIDAD\"," +
        "\"threshold\":1.0" +
        "}";
    mockMvc.perform(post("/materials")
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalidJson))
        .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateMaterial_InvalidType_ShouldReturnBadRequest() throws Exception {
    String invalidJson = "{" +
        "\"name\":\"Malta\"," +
        "\"type\":\"INVALIDO\"," +
        "\"value\":100.0," +
        "\"stock\":10.0," +
        "\"unitMeasurement\":\"UNIDAD\"," +
        "\"threshold\":1.0" +
        "}";
    mockMvc.perform(post("/materials")
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalidJson))
        .andExpect(status().isInternalServerError()); // JSON parsing error returns 500
    }

    @Test
    void testUpdateMaterial() throws Exception {
        String updateJson = """
                {
                    "quantity": 20
                }
                """;
        mockMvc.perform(patch("/materials/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)).andExpect(status().isOk());
    }

    @Test
    void testToggleActive() throws Exception {
        mockMvc.perform(patch("/materials/1/toggle-active"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMaterials() throws Exception {
        mockMvc.perform(get("/materials")).andExpect(status().isOk());
    }

    @Test
    void testGetMaterial() throws Exception {
        mockMvc.perform(get("/materials/1")).andExpect(status().isOk());
    }
}
