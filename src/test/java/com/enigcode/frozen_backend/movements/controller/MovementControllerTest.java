package com.enigcode.frozen_backend.movements.controller;

import com.enigcode.frozen_backend.common.exceptions_configs.GlobalExceptionHandler;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.movements.DTO.MovementCreateDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementResponseDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementDetailDTO;
import com.enigcode.frozen_backend.movements.service.MovementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MovementControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MovementService movementService;

    @InjectMocks
    private MovementController movementController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(movementController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testCreateMovement() throws Exception {
        MovementResponseDTO responseDTO = new MovementResponseDTO();
        responseDTO.setId(1L);

        when(movementService.createMovement(any(MovementCreateDTO.class))).thenReturn(responseDTO);

        String validJson = "{\"materialId\":1,\"stock\":5,\"type\":\"INGRESO\",\"location\":\"A1\"}";
        mockMvc.perform(post("/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

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
                .andExpect(jsonPath("$.id").value(1));

        verify(movementService, times(1)).getMovement(1L);
    }

    @Test
    void testGetMovement_NotFound_ShouldReturn404() throws Exception {
        when(movementService.getMovement(999L)).thenThrow(new ResourceNotFoundException("no encontrado"));

        mockMvc.perform(get("/movements/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testToggleInProgress_Patch_Success() throws Exception {
        MovementResponseDTO dto = new MovementResponseDTO();
        dto.setId(2L);
        when(movementService.toggleInProgressPending(2L)).thenReturn(dto);

        mockMvc.perform(patch("/movements/2/in-progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void testToggleInProgress_Patch_BadRequest() throws Exception {
        when(movementService.toggleInProgressPending(3L)).thenThrow(new BadRequestException("invalid"));

        mockMvc.perform(patch("/movements/3/in-progress"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCompleteMovement_Patch_Success() throws Exception {
        MovementResponseDTO dto = new MovementResponseDTO();
        dto.setId(5L);
        when(movementService.completeMovement(5L)).thenReturn(dto);

        mockMvc.perform(patch("/movements/5/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void testCompleteMovement_Patch_NotFound() throws Exception {
        when(movementService.completeMovement(999L)).thenThrow(new ResourceNotFoundException("no"));

        mockMvc.perform(patch("/movements/999/complete"))
                .andExpect(status().isNotFound());
    }
}
