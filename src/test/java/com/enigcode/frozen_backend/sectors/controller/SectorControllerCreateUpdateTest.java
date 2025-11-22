package com.enigcode.frozen_backend.sectors.controller;

import com.enigcode.frozen_backend.sectors.DTO.SectorCreateDTO;
import com.enigcode.frozen_backend.sectors.DTO.SectorResponseDTO;
import com.enigcode.frozen_backend.sectors.DTO.SectorUpdateDTO;
import com.enigcode.frozen_backend.sectors.service.SectorService;
import com.enigcode.frozen_backend.sectors.model.SectorType;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = SectorController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SectorControllerCreateUpdateTest.TestConfig.class)
class SectorControllerCreateUpdateTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    SectorService sectorService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public SectorService sectorService() {
            return Mockito.mock(SectorService.class);
        }
                @Bean
                public com.enigcode.frozen_backend.common.SecurityProperties securityProperties() {
                        return Mockito.mock(com.enigcode.frozen_backend.common.SecurityProperties.class);
                }
    }

    @Test
    void createSector_returnsCreated() throws Exception {
        SectorCreateDTO createDTO = SectorCreateDTO.builder()
                .name("Sec A")
                .supervisorId(1L)
                .type(SectorType.PRODUCCION)
                .phase(Phase.MOLIENDA)
                .productionCapacity(10.0)
                .isTimeActive(true)
                .build();

        SectorResponseDTO resp = SectorResponseDTO.builder()
                .id(100L)
                .name(createDTO.getName())
                .supervisorId(createDTO.getSupervisorId())
                .type(createDTO.getType())
                .phase(createDTO.getPhase())
                .productionCapacity(createDTO.getProductionCapacity())
                .isTimeActive(createDTO.getIsTimeActive())
                .build();

        Mockito.when(sectorService.createSector(any())).thenReturn(resp);

        mockMvc.perform(post("/sectors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void updateSector_returnsOk() throws Exception {
        SectorUpdateDTO updateDTO = SectorUpdateDTO.builder()
                .name("Sec A Renamed")
                .build();

        SectorResponseDTO resp = SectorResponseDTO.builder()
                .id(100L)
                .name(updateDTO.getName())
                .build();

        Mockito.when(sectorService.updateDTO(any(), any())).thenReturn(resp);

        mockMvc.perform(patch("/sectors/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.name").value("Sec A Renamed"));
    }
}
