package com.enigcode.frozen_backend.quality_parameters.controller;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterResponseDTO;
import com.enigcode.frozen_backend.quality_parameters.service.QualityParameterService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import com.enigcode.frozen_backend.common.SecurityProperties;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(QualityParameterController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({QualityParameterControllerActiveTest.TestConfig.class, com.enigcode.frozen_backend.common.exceptions_configs.GlobalExceptionHandler.class})
class QualityParameterControllerActiveTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QualityParameterService qualityParameterService; // provided by TestConfig as a Mockito mock

    @TestConfiguration
    static class TestConfig {
        @Bean
        public QualityParameterService qualityParameterService() {
            return Mockito.mock(QualityParameterService.class);
        }

        @Bean
        public SecurityProperties securityProperties() {
            SecurityProperties props = new SecurityProperties();
            props.setEnableHttpsRedirect(false);
            props.setMaxLoginAttempts(3);
            props.setLoginTimeoutMinutes(30);
            return props;
        }
    }

    @Test
    void getActiveWithoutPhase_returnsList() throws Exception {
        QualityParameterResponseDTO dto = QualityParameterResponseDTO.builder().id(1L).name("pH").build();
        when(qualityParameterService.getActiveQualityParameters()).thenReturn(List.of(dto));

        mockMvc.perform(get("/quality-parameters/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("pH"));
    }

    @Test
    void getActiveWithPhase_callsFilteredService() throws Exception {
        QualityParameterResponseDTO dto = QualityParameterResponseDTO.builder().id(2L).name("Temp").build();
        when(qualityParameterService.getActiveQualityParametersByPhase(Phase.FERMENTACION)).thenReturn(List.of(dto));

        mockMvc.perform(get("/quality-parameters/active").param("phase","FERMENTACION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Temp"));
    }
}
