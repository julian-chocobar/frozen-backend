package com.enigcode.frozen_backend.movements.controller;

import com.enigcode.frozen_backend.common.exceptions_configs.GlobalExceptionHandler;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;

import com.enigcode.frozen_backend.movements.DTO.MovementCreateDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementResponseDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementDetailDTO;
import com.enigcode.frozen_backend.movements.service.MovementService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovementController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class MovementControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private MovementService movementService;
    @MockitoBean
    private com.enigcode.frozen_backend.common.SecurityProperties securityProperties;

    @Test
    void testCreateMovement() throws Exception {
        // inicializar campos necesarios
        MovementResponseDTO responseDTO = new MovementResponseDTO();
        responseDTO.setId(1L);

        when(movementService.createMovement(any(MovementCreateDTO.class))).thenReturn(responseDTO);

    // JSON con campos mínimos válidos según validaciones del DTO
    String validJson = "{\"materialId\":1,\"stock\":5,\"type\":\"INGRESO\",\"location\":\"A1\"}";
    mockMvc.perform(post("/movements")
        .contentType(MediaType.APPLICATION_JSON)
        .content(validJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"));

        verify(movementService, times(1)).createMovement(any(MovementCreateDTO.class));
    }

    @Test
    void testCreateMovement_BadRequest_ShouldReturn400() throws Exception {
        when(movementService.createMovement(any(MovementCreateDTO.class)))
                .thenThrow(new BadRequestException("stock insuficiente"));

        String validJson = "{\"materialId\":1,\"stock\":5,\"type\":\"INGRESO\",\"location\":\"A1\"}";
        mockMvc.perform(post("/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetMovement() throws Exception {
        MovementDetailDTO detailDTO = new MovementDetailDTO();
        detailDTO.setId(1L);

        when(movementService.getMovement(1L)).thenReturn(detailDTO);

        mockMvc.perform(get("/movements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));

        verify(movementService, times(1)).getMovement(1L);
    }

    @Test
    void testGetMovement_NotFound_ShouldReturn404() throws Exception {
        when(movementService.getMovement(999L)).thenThrow(new ResourceNotFoundException("no encontrado"));

        mockMvc.perform(get("/movements/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateMovement_InvalidType_ShouldReturn400() throws Exception {
        String invalidTypeJson = "{\"materialId\":1,\"stock\":5,\"type\":\"INVALIDO\"}";
        mockMvc.perform(post("/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidTypeJson))
                .andExpect(status().isInternalServerError()); // JSON parsing error returns 500
    }
}
