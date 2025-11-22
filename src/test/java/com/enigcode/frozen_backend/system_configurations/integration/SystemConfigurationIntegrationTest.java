package com.enigcode.frozen_backend.system_configurations.integration;

import com.enigcode.frozen_backend.common.service.DataLoaderService;
import com.enigcode.frozen_backend.system_configurations.model.SystemConfiguration;
import com.enigcode.frozen_backend.system_configurations.model.WorkingDay;
import com.enigcode.frozen_backend.system_configurations.repository.SystemConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SystemConfigurationIntegrationTest.TestConfig.class)
@Transactional
class SystemConfigurationIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public DataLoaderService dataLoaderService() {
            return Mockito.mock(DataLoaderService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SystemConfigurationRepository repository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAndPatchEndpoints_flowWorks_whenDataLoaderMocked() throws Exception {
        // Ensure DB empty so GET triggers service default-creation path (original failing flow)
        repository.deleteAll();

        // GET will cause the service to create the default SystemConfiguration (this previously
        // produced an immutable-list for workingDays and led to a persistence error on PATCH)
        mockMvc.perform(get("/system-configurations"))
                .andExpect(status().isOk());

        // Patch: change Monday to isWorkingDay=false
        String patchJson = "[{\"dayOfWeek\":\"MONDAY\",\"isWorkingDay\":false}]";

        mockMvc.perform(patch("/system-configurations/working-days")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());

        // Verify persistence: repository should still have active config with monday present
        Optional<SystemConfiguration> after = repository.findFirstByIsActiveTrueOrderByIdDesc();
        assertTrue(after.isPresent());
        boolean mondayFound = after.get().getWorkingDays().stream()
                .anyMatch(wd -> wd.getDayOfWeek() == java.time.DayOfWeek.MONDAY);
        assertTrue(mondayFound);
    }
}
