package com.enigcode.frozen_backend.quality_parameters.controller;

import com.enigcode.frozen_backend.common.exceptions_configs.GlobalExceptionHandler;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterCreateDTO;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterResponseDTO;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterUpdateDTO;
import com.enigcode.frozen_backend.quality_parameters.service.QualityParameterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QualityParameterController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class QualityParameterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QualityParameterService qualityParameterService;

    @MockBean
    private com.enigcode.frozen_backend.common.SecurityProperties securityProperties;

    @Autowired
    private ObjectMapper objectMapper;

    private QualityParameterResponseDTO responseDTO;
    private String createJson;

    @BeforeEach
    void setup() throws Exception {
        responseDTO = QualityParameterResponseDTO.builder()
                .phase(Phase.MOLIENDA)
                .isCritical(true)
                .name("pH")
                .description("Medición de acidez")
                .isActive(true)
                .build();

        QualityParameterCreateDTO createDTO = QualityParameterCreateDTO.builder()
                .phase(Phase.MOLIENDA)
                .isCritical(true)
                .name("pH")
                .description("Medición de acidez")
                .build();

        createJson = objectMapper.writeValueAsString(createDTO);

        when(qualityParameterService.createQualityParameter(any())).thenReturn(responseDTO);
        when(qualityParameterService.getQualityParameter(1L)).thenReturn(responseDTO);
        when(qualityParameterService.updateQualityParameter(eq(1L), any())).thenReturn(responseDTO);
        when(qualityParameterService.toggleActive(1L)).thenReturn(responseDTO);
        when(qualityParameterService.getQualityParameters()).thenReturn(Arrays.asList(responseDTO));
    }

    @Test
    void testCreateQualityParameter_validRequest_returns201() throws Exception {
        mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("pH"))
                .andExpect(jsonPath("$.phase").value("MOLIENDA"))
                .andExpect(jsonPath("$.isCritical").value(true))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void testCreateQualityParameter_missingName_returns400() throws Exception {
        String invalidJson = """
        {
            "phase": "MOLIENDA",
            "isCritical": true,
            "description": "Sin nombre"
        }
        """;

        mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateQualityParameter_missingPhase_returns400() throws Exception {
        String invalidJson = """
        {
            "isCritical": true,
            "name": "pH",
            "description": "Sin fase"
        }
        """;

        mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateQualityParameter_missingIsCritical_returns400() throws Exception {
        String invalidJson = """
        {
            "phase": "MOLIENDA",
            "name": "pH",
            "description": "Sin campo crítico"
        }
        """;

        mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateQualityParameter_invalidPhase_returns500_currentBehavior() throws Exception {
        String invalidJson = """
        {
            "phase": "INVALID_PHASE",
            "isCritical": true,
            "name": "pH"
        }
        """;

        mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testCreateQualityParameter_nameTooLong_returns400() throws Exception {
        String invalidJson = """
        {
            "phase": "MOLIENDA",
            "isCritical": true,
            "name": "Este nombre es demasiado largo para el campo permitido que es de 20 caracteres"
        }
        """;

        mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateQualityParameter_validRequest_returns200() throws Exception {
        QualityParameterUpdateDTO updateDTO = QualityParameterUpdateDTO.builder()
                .description("Nueva descripción actualizada")
                .build();

        String updateJson = objectMapper.writeValueAsString(updateDTO);

        mockMvc.perform(patch("/quality-parameters/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateQualityParameter_descriptionTooLong_returns400() throws Exception {
        String invalidJson = """
        {
            "description": "Esta descripción es extremadamente larga y supera los 255 caracteres permitidos en el campo de descripción. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."
        }
        """;

        mockMvc.perform(patch("/quality-parameters/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetQualityParameter_exists_returns200() throws Exception {
        mockMvc.perform(get("/quality-parameters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("pH"))
                .andExpect(jsonPath("$.phase").value("MOLIENDA"))
                .andExpect(jsonPath("$.isCritical").value(true));
    }

    @Test
    void testGetQualityParameter_notFound_returns404() throws Exception {
        when(qualityParameterService.getQualityParameter(999L))
                .thenThrow(new ResourceNotFoundException("No se encontró parámetro de calidad con id 999"));

        mockMvc.perform(get("/quality-parameters/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testToggleActive_success_returns200() throws Exception {
        QualityParameterResponseDTO inactiveResponse = QualityParameterResponseDTO.builder()
                .phase(Phase.MOLIENDA)
                .isCritical(true)
                .name("pH")
                .description("Medición de acidez")
                .isActive(false)
                .build();

        when(qualityParameterService.toggleActive(1L)).thenReturn(inactiveResponse);

        mockMvc.perform(patch("/quality-parameters/1/toggle-active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void testToggleActive_notFound_returns404() throws Exception {
        when(qualityParameterService.toggleActive(999L))
                .thenThrow(new ResourceNotFoundException("No se encontró parámetro de calidad con id 999"));

        mockMvc.perform(patch("/quality-parameters/999/toggle-active"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testListQualityParameters_returns200() throws Exception {
        QualityParameterResponseDTO response1 = QualityParameterResponseDTO.builder()
                .phase(Phase.MOLIENDA)
                .isCritical(true)
                .name("pH")
                .isActive(true)
                .build();

        QualityParameterResponseDTO response2 = QualityParameterResponseDTO.builder()
                .phase(Phase.FERMENTACION)
                .isCritical(false)
                .name("Temperatura")
                .isActive(true)
                .build();

        List<QualityParameterResponseDTO> responses = Arrays.asList(response1, response2);
        when(qualityParameterService.getQualityParameters()).thenReturn(responses);

        mockMvc.perform(get("/quality-parameters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("pH"))
                .andExpect(jsonPath("$[1].name").value("Temperatura"));
    }

    @Test
    void testListQualityParameters_emptyList_returns200() throws Exception {
        when(qualityParameterService.getQualityParameters()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/quality-parameters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testCreateQualityParameter_withNullDescription_success() throws Exception {
        String jsonWithoutDescription = """
        {
            "phase": "FERMENTACION",
            "isCritical": false,
            "name": "Temperatura"
        }
        """;

        mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithoutDescription))
                .andExpect(status().isCreated());
    }
}
