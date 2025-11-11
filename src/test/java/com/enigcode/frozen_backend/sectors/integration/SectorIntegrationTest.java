package com.enigcode.frozen_backend.sectors.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "ADMIN")
class SectorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "SUPERVISOR_DE_PRODUCCION")
    void getSector_asAuthenticatedUser_returns404NotFound() throws Exception {
        // Validar que cualquier usuario autenticado puede acceder al endpoint GET /sectors/{id}
        // Como no hay sectores en DB de test, esperamos 404 (no 401 ni 403)
        mockMvc.perform(get("/sectors/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createSector_withNonExistentSupervisor_returns404() throws Exception {
        // Validar que el endpoint valida la existencia del supervisor
        String requestBody = """
        {
            "name": "Sector Test",
            "supervisorId": 9999,
            "type": "ALMACEN"
        }
        """;

        mockMvc.perform(post("/sectors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR_DE_ALMACEN")
    void createSector_withUnauthorizedRole_returns403() throws Exception {
        // Verificar que roles no autorizados no pueden crear sectores
        // Solo ADMIN y GERENTE_DE_PLANTA pueden crear sectores
        String requestBody = """
        {
            "name": "Sector No Permitido",
            "supervisorId": 1,
            "type": "ALMACEN"
        }
        """;

        mockMvc.perform(post("/sectors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR_DE_PRODUCCION")
    void updateSector_withUnauthorizedRole_returns403() throws Exception {
        // Verificar que roles no autorizados no pueden actualizar sectores
        // Solo ADMIN y GERENTE_DE_PLANTA pueden actualizar sectores
        mockMvc.perform(patch("/sectors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\"}"))
                .andExpect(status().isForbidden());
    }

    // --- Nuevas pruebas enfocadas en capacidad de producción ---

    @Test
    void createSector_produccion_missingRequiredFields_returns400() throws Exception {
        // Falta productionCapacity (y/o otros), debe responder 400 por validación de negocio
        String requestBody = """
        {
            "name": "Sector Producción Incompleto",
            "supervisorId": 1,
            "type": "PRODUCCION",
            "phase": "MOLIENDA",
            "isTimeActive": true
        }
        """;

        mockMvc.perform(post("/sectors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSector_produccion_withNonExistentSupervisor_returns404() throws Exception {
        String requestBody = """
        {
            "name": "Sector Producción",
            "supervisorId": 999999,
            "type": "PRODUCCION",
            "phase": "MOLIENDA",
            "productionCapacity": 500.0,
            "isTimeActive": true
        }
        """;

        mockMvc.perform(post("/sectors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSector_nonExistentId_returns404() throws Exception {
        String patchBody = "{\"productionCapacity\": 250.0}";

        mockMvc.perform(patch("/sectors/{id}", 987654)
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchBody))
                .andExpect(status().isNotFound());
    }


}
