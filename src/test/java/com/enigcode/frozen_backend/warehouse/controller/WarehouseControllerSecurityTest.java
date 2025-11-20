package com.enigcode.frozen_backend.warehouse.controller;

import com.enigcode.frozen_backend.warehouse.service.WarehouseLayoutService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verificación de acceso público y restricciones por rol en WarehouseController.
 */
@WebMvcTest(WarehouseController.class)
@Import(com.enigcode.frozen_backend.common.SecurityConfig.class)
class WarehouseControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WarehouseLayoutService warehouseLayoutService;
    @MockitoBean
    private com.enigcode.frozen_backend.common.SecurityProperties securityProperties;

    @TestConfiguration
    static class TestSecurityBeans {
        @Bean
        UserDetailsService userDetailsService() {
            return new InMemoryUserDetailsManager(
                    User.withUsername("supervisor").password("password").roles("SUPERVISOR_DE_ALMACEN").build(),
                    User.withUsername("operario").password("password").roles("OPERARIO_DE_ALMACEN").build());
        }
    }

    @Test
    @DisplayName("GET /warehouse/layout sin auth -> 401")
    void getLayout_unauthenticated_returns401() throws Exception {
        Mockito.when(warehouseLayoutService.getWarehouseSvg()).thenReturn("<svg></svg>");
        mockMvc.perform(get("/warehouse/layout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /warehouse/layout con auth -> 200 con tipo SVG")
    void getLayout_authenticated_returns200() throws Exception {
        Mockito.when(warehouseLayoutService.getWarehouseSvg()).thenReturn("<svg></svg>");
        mockMvc.perform(get("/warehouse/layout"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/svg+xml"));
    }

    @Test
    @DisplayName("POST /warehouse/validate-location sin auth -> 401")
    void validateLocation_withoutAuth_returns401() throws Exception {
    mockMvc.perform(post("/warehouse/validate-location").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"zone\":\"A\",\"section\":\"1\",\"level\":\"L1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "OPERARIO_DE_ALMACEN")
    @DisplayName("POST /warehouse/validate-location rol permitido -> 200 (payload válido)")
    void validateLocation_allowedRole_returns200() throws Exception {
        Mockito.when(warehouseLayoutService.isValidLocation(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        // Payload debe usar enum válido y tipos correctos: zone=ENVASE, section=A1, level=1
        mockMvc.perform(post("/warehouse/validate-location").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"zone\":\"MALTA\",\"section\":\"A1\",\"level\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isValid").value(true));
    }

    @Test
    @DisplayName("GET /warehouse/zones sin auth -> 401")
    void getZones_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/warehouse/zones"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "OPERARIO_DE_ALMACEN")
    @DisplayName("GET /warehouse/zones rol OPERARIO -> 200")
    void getZones_operario_returns200() throws Exception {
        mockMvc.perform(get("/warehouse/zones"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR_DE_ALMACEN")
    @DisplayName("GET /warehouse/zones rol SUPERVISOR -> 200")
    void getZones_supervisor_returns200() throws Exception {
        mockMvc.perform(get("/warehouse/zones"))
                .andExpect(status().isOk());
    }
}
