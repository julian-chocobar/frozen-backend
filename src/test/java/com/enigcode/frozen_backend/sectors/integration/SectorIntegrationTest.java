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

/**
 * Tests de integración para el módulo de Sectors.
 * 
 * IMPORTANTE: Los tests de creación exitosa y validación de roles de supervisores
 * están completamente cubiertos en SectorServiceImplTest (tests unitarios).
 * 
 * Estos tests de integración se enfocan ÚNICAMENTE en:
 * - Validación de autorización a nivel de endpoint (403 Forbidden)
 * - Validación de que endpoints existen y están configurados correctamente
 * - Validación de errores básicos (404 Not Found)
 * 
 * La validación de lógica de negocio (roles de supervisores, campos requeridos, etc.)
 * se realiza en los tests unitarios del servicio.
 */
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

    // NOTA IMPORTANTE:
    // Los siguientes escenarios están cubiertos en SectorServiceImplTest:
    // - createSector con datos válidos (PRODUCCION, ALMACEN, CALIDAD)
    // - createSector sin campos requeridos para PRODUCCION (phase, capacity, isTimeActive)
    // - createSector con supervisor que no tiene el rol correcto
    // - updateSector cambiando supervisor a uno con rol incorrecto
    // - getSector by ID
    // - Validación de que supervisor tiene rol correcto para tipo de sector
}
