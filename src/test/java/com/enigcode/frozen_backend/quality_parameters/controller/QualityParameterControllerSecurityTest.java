package com.enigcode.frozen_backend.quality_parameters.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class QualityParameterControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String validRequestBody = """
        {
            "phase": "MOLIENDA",
            "isCritical": true,
            "name": "pH",
            "description": "Medición de acidez"
        }
        """;

    @Test
    @WithAnonymousUser
    void testCreate_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "OPERARIO_DE_PRODUCCION")
    void testCreate_withWrongRole_returns403() throws Exception {
        mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR_DE_CALIDAD")
    void testCreate_withCorrectRole_success() throws Exception {
        mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody))
                .andExpect(status().isCreated());
    }

    @Test
    @WithAnonymousUser
    void testUpdate_withoutAuth_returns401() throws Exception {
        String updateBody = """
        {
            "description": "Nueva descripción"
        }
        """;

        mockMvc.perform(patch("/quality-parameters/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "OPERARIO_DE_PRODUCCION")
    void testUpdate_withWrongRole_returns403() throws Exception {
        String updateBody = """
        {
            "description": "Nueva descripción"
        }
        """;

        mockMvc.perform(patch("/quality-parameters/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR_DE_CALIDAD")
    void testUpdate_withCorrectRole_successOrNotFound() throws Exception {
        // Primero crear un parámetro
        MvcResult createResult = mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long parameterId = objectMapper.readTree(createResponse).get("id").asLong();

        String updateBody = """
        {
            "description": "Nueva descripción actualizada"
        }
        """;

        mockMvc.perform(patch("/quality-parameters/" + parameterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void testToggleActive_withoutAuth_returns401() throws Exception {
        mockMvc.perform(patch("/quality-parameters/1/toggle-active"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "OPERARIO_DE_PRODUCCION")
    void testToggleActive_withWrongRole_returns403() throws Exception {
        mockMvc.perform(patch("/quality-parameters/1/toggle-active"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR_DE_CALIDAD")
    void testToggleActive_withCorrectRole_success() throws Exception {
        // Primero crear un parámetro
        MvcResult createResult = mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long parameterId = objectMapper.readTree(createResponse).get("id").asLong();

        // Toggle active debe ser exitoso
        mockMvc.perform(patch("/quality-parameters/" + parameterId + "/toggle-active"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OPERARIO_DE_PRODUCCION")
    void testGet_withAnyRole_success() throws Exception {
        // GET no requiere rol específico, solo autenticación
        mockMvc.perform(get("/quality-parameters"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR_DE_PRODUCCION")
    void testGet_withDifferentRole_success() throws Exception {
        // GET debe ser accesible por cualquier usuario autenticado
        mockMvc.perform(get("/quality-parameters"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void testGet_withoutAuth_returns401() throws Exception {
        // Endpoint requiere autenticación según configuración de seguridad actual
        mockMvc.perform(get("/quality-parameters"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR_DE_CALIDAD", "ADMIN"})
    void testCreate_withMultipleRoles_success() throws Exception {
        mockMvc.perform(post("/quality-parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody))
                .andExpect(status().isCreated());
    }
}
