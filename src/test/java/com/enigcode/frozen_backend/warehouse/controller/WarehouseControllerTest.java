package com.enigcode.frozen_backend.warehouse.controller;

import com.enigcode.frozen_backend.materials.DTO.LocationValidationRequestDTO;
import com.enigcode.frozen_backend.warehouse.service.WarehouseLayoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


 
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WarehouseControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WarehouseLayoutService warehouseLayoutService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        WarehouseController controller = new WarehouseController(warehouseLayoutService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /warehouse/zones returns zone keys and sections")
    void getZones_returnsZones() throws Exception {
        mockMvc.perform(get("/warehouse/zones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.MALTA").exists())
                .andExpect(jsonPath("$.MALTA[0]").exists());
    }

    @Test
    @DisplayName("POST /warehouse/validate-location delegates to service and returns DTO")
    void validateLocation_delegatesAndReturns() throws Exception {
        LocationValidationRequestDTO req = new LocationValidationRequestDTO();
        req.setZone(null);

        Mockito.when(warehouseLayoutService.isValidLocation(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(true);

        String payload = "{\"zone\":\"MALTA\",\"section\":\"A1\",\"level\":2}";

        mockMvc.perform(post("/warehouse/validate-location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isValid").value(true));
    }
}
