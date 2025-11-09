package com.enigcode.frozen_backend.quality_parameters.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "SUPERVISOR_DE_CALIDAD")
class QualityParameterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createQualityParameter_andGetById_happyPath() throws Exception {
        // Crear parámetro de calidad válido
        String requestBody = """
        {
            "phase": "MOLIENDA",
            "isCritical": true,
            "name": "pH",
            "description": "Medición de acidez del mosto"
        }
        """;

        MvcResult createResult = mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("pH"))
                .andExpect(jsonPath("$.phase").value("MOLIENDA"))
                .andExpect(jsonPath("$.isCritical").value(true))
                .andExpect(jsonPath("$.isActive").value(true))
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long parameterId = objectMapper.readTree(createResponse).get("id").asLong();
        assertThat(parameterId).isNotNull();

        // Obtener el parámetro por id
    mockMvc.perform(get("/quality-parameters/" + parameterId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("pH"))
        .andExpect(jsonPath("$.phase").value("MOLIENDA"))
        .andExpect(jsonPath("$.isCritical").value(true))
        .andExpect(jsonPath("$.description").value("Medición de acidez del mosto"))
        .andExpect(jsonPath("$.id").value(parameterId));
    }

    @Test
    void createMultipleParameters_andListAll_happyPath() throws Exception {
        // Crear primer parámetro
        String requestBody1 = """
        {
            "phase": "MOLIENDA",
            "isCritical": true,
            "name": "Granulometría",
            "description": "Control del tamaño de grano"
        }
        """;

        // Crear segundo parámetro
        String requestBody2 = """
        {
            "phase": "FERMENTACION",
            "isCritical": false,
            "name": "Temperatura",
            "description": "Control de temperatura de fermentación"
        }
        """;

        // Crear tercer parámetro
        String requestBody3 = """
        {
            "phase": "COCCION",
            "isCritical": true,
            "name": "Densidad",
            "description": "Densidad del mosto"
        }
        """;

        mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody1))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody2))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody3))
                .andExpect(status().isCreated());

        // Listar todos los parámetros
        MvcResult listResult = mockMvc.perform(get("/quality-parameters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        String listResponse = listResult.getResponse().getContentAsString();
        assertThat(listResponse).contains("Granulometría");
        assertThat(listResponse).contains("Temperatura");
        assertThat(listResponse).contains("Densidad");
        assertThat(listResponse).contains("id");
    }

    @Test
    void createParameter_toggleActive_andVerify() throws Exception {
        // Crear parámetro
        String requestBody = """
        {
            "phase": "ENVASADO",
            "isCritical": false,
            "name": "Inspección Visual",
            "description": "Control visual del producto"
        }
        """;

        MvcResult createResult = mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isActive").value(true))
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long parameterId = objectMapper.readTree(createResponse).get("id").asLong();

        // Toggle active (de true a false)
        mockMvc.perform(patch("/quality-parameters/" + parameterId + "/toggle-active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        // Verificar que el cambio persiste
        mockMvc.perform(get("/quality-parameters/" + parameterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        // Toggle active nuevamente (de false a true)
    mockMvc.perform(patch("/quality-parameters/" + parameterId + "/toggle-active"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isActive").value(true))
        .andExpect(jsonPath("$.id").value(parameterId));
    }

    @Test
    void updateParameter_description_success() throws Exception {
        // Crear parámetro
        String requestBody = """
        {
            "phase": "COCCION",
            "isCritical": true,
            "name": "pH Inicial",
            "description": "Descripción original"
        }
        """;

        MvcResult createResult = mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long parameterId = objectMapper.readTree(createResponse).get("id").asLong();

        // Actualizar descripción
        String updateBody = """
        {
            "description": "Descripción actualizada con más detalle"
        }
        """;

    mockMvc.perform(patch("/quality-parameters/" + parameterId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(updateBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").value("Descripción actualizada con más detalle"))
        .andExpect(jsonPath("$.name").value("pH Inicial"))
        .andExpect(jsonPath("$.phase").value("COCCION"))
        .andExpect(jsonPath("$.id").value(parameterId));
    }

    @Test
    void createParameter_withMinimalData_success() throws Exception {
        // Crear parámetro sin descripción
        String requestBody = """
        {
            "phase": "FERMENTACION",
            "isCritical": false,
            "name": "Presión"
        }
        """;

        mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Presión"))
                .andExpect(jsonPath("$.phase").value("FERMENTACION"))
                .andExpect(jsonPath("$.isCritical").value(false))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void createParameter_withSameName_differentPhase_success() throws Exception {
        // Crear primer parámetro con nombre "pH" en MOLIENDA
        String requestBody1 = """
        {
            "phase": "MOLIENDA",
            "isCritical": true,
            "name": "pH"
        }
        """;

        mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody1))
                .andExpect(status().isCreated());

        // Crear segundo parámetro con el mismo nombre "pH" pero en FERMENTACION
        String requestBody2 = """
        {
            "phase": "FERMENTACION",
            "isCritical": true,
            "name": "pH"
        }
        """;

        mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody2))
                .andExpect(status().isCreated());

        // Verificar que ambos existen
        MvcResult listResult = mockMvc.perform(get("/quality-parameters"))
                .andExpect(status().isOk())
                .andReturn();

        String listResponse = listResult.getResponse().getContentAsString();
        assertThat(listResponse).contains("MOLIENDA");
        assertThat(listResponse).contains("FERMENTACION");
    }

    @Test
    void getParameter_notFound_returns404() throws Exception {
        mockMvc.perform(get("/quality-parameters/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateParameter_notFound_returns404() throws Exception {
        String updateBody = """
        {
            "description": "Nueva descripción"
        }
        """;

        mockMvc.perform(patch("/quality-parameters/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void toggleActive_notFound_returns404() throws Exception {
        mockMvc.perform(patch("/quality-parameters/999999/toggle-active"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCriticalParameter_andVerify() throws Exception {
        String requestBody = """
        {
            "phase": "COCCION",
            "isCritical": true,
            "name": "Temperatura Crítica",
            "description": "Este es un parámetro crítico que no puede fallar"
        }
        """;

        MvcResult createResult = mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isCritical").value(true))
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long parameterId = objectMapper.readTree(createResponse).get("id").asLong();

        // Verificar que el parámetro crítico se mantiene
        mockMvc.perform(get("/quality-parameters/" + parameterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCritical").value(true))
                .andExpect(jsonPath("$.name").value("Temperatura Crítica"));
    }
}
