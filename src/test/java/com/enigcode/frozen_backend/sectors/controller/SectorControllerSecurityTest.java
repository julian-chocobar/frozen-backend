package com.enigcode.frozen_backend.sectors.controller;

import com.enigcode.frozen_backend.sectors.DTO.SectorResponseDTO;
import com.enigcode.frozen_backend.sectors.service.SectorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SectorController.class)
class SectorControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SectorService sectorService;
    
    @MockBean
    private com.enigcode.frozen_backend.common.SecurityProperties securityProperties;

    @Test
    @DisplayName("GET /sectors/{id} without auth -> 401")
    void getSector_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/sectors/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /sectors/{id} with auth -> 200")
    void getSector_authenticated_returns200() throws Exception {
        SectorResponseDTO responseDTO = new SectorResponseDTO();
        responseDTO.setName("Test Sector");
        
        Mockito.when(sectorService.getSector(Mockito.anyLong())).thenReturn(responseDTO);
        
        mockMvc.perform(get("/sectors/1"))
                .andExpect(status().isOk());
    }
}
