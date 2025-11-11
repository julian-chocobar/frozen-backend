package com.enigcode.frozen_backend.materials.controller;

import com.enigcode.frozen_backend.materials.DTO.*;
import com.enigcode.frozen_backend.materials.service.MaterialService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.List;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Cobertura de endpoints abiertos y protegidos de MaterialController.
 * Se verifica acceso anónimo en endpoints sin @PreAuthorize y restricciones en los protegidos.
 */
@WebMvcTest(MaterialController.class)
@Import(com.enigcode.frozen_backend.common.SecurityConfig.class)
class MaterialControllerAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MaterialService materialService;
    @MockBean
    private com.enigcode.frozen_backend.common.SecurityProperties securityProperties; // usado por configuración

        @TestConfiguration
        static class TestSecurityBeans {
                @Bean
                UserDetailsService userDetailsService() {
                        return new InMemoryUserDetailsManager(
                                        User.withUsername("supervisor").password("password").roles("SUPERVISOR_DE_ALMACEN").build(),
                                        User.withUsername("operario").password("password").roles("OPERARIO_DE_ALMACEN").build(),
                                        User.withUsername("user").password("password").roles("USER").build());
                }
        }

    // Endpoints abiertos

    @Test
        @DisplayName("GET /materials sin auth -> 401")
        void getMaterials_unauthenticated_returns401() throws Exception {
                Mockito.when(materialService.findAll(Mockito.any(), Mockito.any())).thenReturn(Page.empty());
                mockMvc.perform(get("/materials"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser
        @DisplayName("GET /materials con auth -> 200 estructura básica")
        void getMaterials_authenticated_returns200() throws Exception {
                Mockito.when(materialService.findAll(Mockito.any(), Mockito.any())).thenReturn(Page.empty());
                mockMvc.perform(get("/materials"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray());
        }

    @Test
    @DisplayName("GET /materials/id-name-list sin auth -> 401")
    void getMaterialIdNameList_unauthenticated_returns401() throws Exception {
        Mockito.when(materialService.getMaterialSimpleList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(List.of());
        mockMvc.perform(get("/materials/id-name-list"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /materials/id-name-list con auth -> 200")
    void getMaterialIdNameList_authenticated_returns200() throws Exception {
        Mockito.when(materialService.getMaterialSimpleList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(List.of());
        mockMvc.perform(get("/materials/id-name-list"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /materials/{id} sin auth -> 401")
    void getMaterial_unauthenticated_returns401() throws Exception {
        Mockito.when(materialService.getMaterial(Mockito.anyLong()))
                .thenReturn(MaterialDetailDTO.builder()
                        .id(1L)
                        .name("Mat")
                        .threshold(0.0)
                        .isBelowThreshold(false)
                        .availableStock(0.0)
                        .reservedStock(0.0)
                        .totalStock(0.0)
                        .build());
        mockMvc.perform(get("/materials/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /materials/{id} con auth -> 200")
    void getMaterial_authenticated_returns200() throws Exception {
        Mockito.when(materialService.getMaterial(Mockito.anyLong()))
                .thenReturn(MaterialDetailDTO.builder().id(1L).name("Mat").totalStock(0.0).availableStock(0.0).reservedStock(0.0).threshold(0.0).build());
        mockMvc.perform(get("/materials/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("GET /materials/warehouse-map sin auth -> 401")
    void getWarehouseMap_unauthenticated_returns401() throws Exception {
        Mockito.when(materialService.getWarehouseLocations(Mockito.any(), Mockito.any()))
                .thenReturn(Collections.emptyList());
        mockMvc.perform(get("/materials/warehouse-map"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /materials/warehouse-map con auth -> 200")
    void getWarehouseMap_authenticated_returns200() throws Exception {
        Mockito.when(materialService.getWarehouseLocations(Mockito.any(), Mockito.any()))
                .thenReturn(Collections.emptyList());
        mockMvc.perform(get("/materials/warehouse-map"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /materials/warehouse-info sin auth -> 401")
    void getWarehouseInfo_unauthenticated_returns401() throws Exception {
        Mockito.when(materialService.getWarehouseInfo(Mockito.any()))
                .thenReturn(WarehouseInfoDTO.builder()
                        .availableZones(List.of())
                        .suggestedLocation(WarehouseInfoDTO.SuggestedLocationDTO.builder()
                                .zone("ZONA_A")
                                .section("1")
                                .level(1)
                                .build())
                        .totalMaterials(0L)
                        .materialsByZone(Collections.emptyMap())
                        .build());
        mockMvc.perform(get("/materials/warehouse-info"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /materials/warehouse-info con auth -> 200")
    void getWarehouseInfo_authenticated_returns200() throws Exception {
        Mockito.when(materialService.getWarehouseInfo(Mockito.any()))
                .thenReturn(WarehouseInfoDTO.builder()
                        .availableZones(List.of())
                        .suggestedLocation(WarehouseInfoDTO.SuggestedLocationDTO.builder()
                                .zone("ZONA_A").section("1").level(1).build())
                        .totalMaterials(0L)
                        .materialsByZone(Collections.emptyMap())
                        .build());
        mockMvc.perform(get("/materials/warehouse-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestedLocation.zone").value("ZONA_A"));
    }

    // Endpoints protegidos - sin autenticación

    @Test
    @DisplayName("POST /materials sin auth -> 401")
    void createMaterial_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/materials").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nuevo\",\"type\":\"MALTA\",\"stock\":10,\"unitMeasurement\":\"KG\",\"threshold\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /materials/{id} sin auth -> 401")
    void updateMaterial_withoutAuth_returns401() throws Exception {
        mockMvc.perform(patch("/materials/1").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Editado\", \"totalStock\":15}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /materials/{id}/toggle-active sin auth -> 401")
    void toggleActive_withoutAuth_returns401() throws Exception {
        mockMvc.perform(patch("/materials/1/toggle-active").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /materials/{id}/location sin auth -> 401")
    void updateLocation_withoutAuth_returns401() throws Exception {
        mockMvc.perform(patch("/materials/1/location").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"warehouseZone\":\"MALTA\",\"warehouseSection\":\"A1\",\"warehouseLevel\":1}"))
                .andExpect(status().isUnauthorized());
    }

    // Endpoints protegidos - con rol incorrecto

        @Test
        @WithMockUser(roles = "OPERARIO_DE_ALMACEN")
        @DisplayName("POST /materials rol incorrecto -> 403 (aunque DTO válido crea 201, se necesita mockear service para evitar ejecución real)")
        void createMaterial_wrongRole_returns403() throws Exception {
                // No debe invocar al service porque el filtro de seguridad debe bloquear antes
                mockMvc.perform(post("/materials").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("{\"name\":\"Nuevo\",\"type\":\"MALTA\",\"stock\":10,\"unitMeasurement\":\"KG\",\"threshold\":1}"))
                                .andExpect(status().isForbidden());
                Mockito.verify(materialService, Mockito.never()).createMaterial(Mockito.any());
        }

    // Endpoints protegidos - con rol correcto

    @Test
    @WithMockUser(roles = "SUPERVISOR_DE_ALMACEN")
    @DisplayName("POST /materials rol correcto -> 201")
    void createMaterial_correctRole_returns201() throws Exception {
        Mockito.when(materialService.createMaterial(Mockito.any()))
                .thenReturn(MaterialResponseDTO.builder()
                        .id(10L)
                        .name("Nuevo")
                        .isActive(true)
                        .threshold(0.0)
                        .availableStock(0.0)
                        .reservedStock(0.0)
                        .totalStock(0.0)
                        .build());
        mockMvc.perform(post("/materials").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nuevo\",\"type\":\"MALTA\",\"stock\":10,\"unitMeasurement\":\"KG\",\"threshold\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR_DE_ALMACEN")
    @DisplayName("PATCH /materials/{id}/toggle-active rol correcto -> 200")
    void toggleActive_correctRole_returns200() throws Exception {
        Mockito.when(materialService.toggleActive(Mockito.anyLong()))
                .thenReturn(MaterialResponseDTO.builder()
                        .id(1L)
                        .name("Mat")
                        .isActive(false)
                        .totalStock(0.0)
                        .availableStock(0.0)
                        .reservedStock(0.0)
                        .threshold(0.0)
                        .build());
        mockMvc.perform(patch("/materials/1/toggle-active").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR_DE_ALMACEN")
    @DisplayName("PATCH /materials/{id} rol correcto -> 200")
    void updateMaterial_correctRole_returns200() throws Exception {
        Mockito.when(materialService.updateMaterial(Mockito.anyLong(), Mockito.any()))
                .thenReturn(MaterialResponseDTO.builder()
                        .id(1L)
                        .name("Editado")
                        .isActive(true)
                        .totalStock(0.0)
                        .availableStock(0.0)
                        .reservedStock(0.0)
                        .threshold(0.0)
                        .build());
        mockMvc.perform(patch("/materials/1").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Editado\", \"threshold\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Editado"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR_DE_ALMACEN")
    @DisplayName("PATCH /materials/{id}/location rol correcto -> 200")
    void updateLocation_correctRole_returns200() throws Exception {
        Mockito.when(materialService.updateMaterialLocation(Mockito.anyLong(), Mockito.any()))
                .thenReturn(MaterialResponseDTO.builder()
                        .id(1L)
                        .name("Mat")
                        .isActive(true)
                        .totalStock(0.0)
                        .availableStock(0.0)
                        .reservedStock(0.0)
                        .threshold(0.0)
                        .build());
        mockMvc.perform(patch("/materials/1/location").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"warehouseZone\":\"MALTA\",\"warehouseSection\":\"A1\",\"warehouseLevel\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }
}
