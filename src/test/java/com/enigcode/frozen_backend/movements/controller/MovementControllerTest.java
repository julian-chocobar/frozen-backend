package com.enigcode.frozen_backend.movements.controller;

import com.enigcode.frozen_backend.movements.DTO.MovementCreateDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementResponseDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementDetailDTO;
import com.enigcode.frozen_backend.movements.service.MovementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovementController.class)
class MovementControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private MovementService movementService;

    @Test
    void testCreateMovement() throws Exception {
        // inicializar campos necesarios
        MovementResponseDTO responseDTO = new MovementResponseDTO();
        responseDTO.setId(1L);

        when(movementService.createMovement(any(MovementCreateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"materialId\":1,\"quantity\":5}")) // JSON de ejemplo
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"));

        verify(movementService, times(1)).createMovement(any(MovementCreateDTO.class));
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
}
