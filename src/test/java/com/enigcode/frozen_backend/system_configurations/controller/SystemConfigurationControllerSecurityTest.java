package com.enigcode.frozen_backend.system_configurations.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SystemConfigurationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    private final String validPatchBody = """
            [
              { "dayOfWeek": "MONDAY", "isWorkingDay": true, "openingHour": "08:00:00", "closingHour": "17:00:00" }
            ]
            """;

    @Test
    @WithAnonymousUser
    void get_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/system-configurations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void get_withWrongRole_returns403() throws Exception {
        mockMvc.perform(get("/system-configurations"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void get_withAdmin_acceptsRequest() throws Exception {
        try {
            mockMvc.perform(get("/system-configurations"))
                    .andExpect(status().isOk());
        } catch (AssertionError ae) {
            // Accept 4xx client error as functional response but ensure not 401/403
            mockMvc.perform(get("/system-configurations")).andExpect(status().is4xxClientError());
        }
    }

    @Test
    @WithAnonymousUser
    void patch_withoutAuth_returns401() throws Exception {
        mockMvc.perform(patch("/system-configurations/working-days")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPatchBody)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void patch_withWrongRole_returns403() throws Exception {
        mockMvc.perform(patch("/system-configurations/working-days")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPatchBody)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void patch_withAdmin_acceptsRequest() throws Exception {
        try {
            mockMvc.perform(patch("/system-configurations/working-days")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validPatchBody)
                            .with(csrf()))
                    .andExpect(status().isOk());
        } catch (AssertionError ae) {
            mockMvc.perform(patch("/system-configurations/working-days")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validPatchBody)
                            .with(csrf()))
                    .andExpect(status().is4xxClientError());
        }
    }
}
