package com.enigcode.frozen_backend.system_configurations.controller;

import com.enigcode.frozen_backend.system_configurations.DTO.SystemConfigurationResponseDTO;
import com.enigcode.frozen_backend.system_configurations.DTO.WorkingDayUpdateDTO;
import com.enigcode.frozen_backend.system_configurations.service.SystemConfigurationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.DayOfWeek;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SystemConfigurationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SystemConfigurationService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        SystemConfigurationController controller = new SystemConfigurationController(service);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void get_delegatesToService() throws Exception {
        SystemConfigurationResponseDTO dto = SystemConfigurationResponseDTO.builder().isActive(true).build();
        Mockito.when(service.getSystemConfiguration()).thenReturn(dto);

        mockMvc.perform(get("/system-configurations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void patch_delegatesToService() throws Exception {
        WorkingDayUpdateDTO dto = WorkingDayUpdateDTO.builder().dayOfWeek(DayOfWeek.MONDAY).isWorkingDay(true).build();
        SystemConfigurationResponseDTO resp = SystemConfigurationResponseDTO.builder().isActive(true).build();

        Mockito.when(service.updateWorkingDays(Mockito.anyList())).thenReturn(resp);

        String payload = "[ { \"dayOfWeek\": \"MONDAY\", \"isWorkingDay\": true } ]";

        mockMvc.perform(patch("/system-configurations/working-days")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));
    }
}
