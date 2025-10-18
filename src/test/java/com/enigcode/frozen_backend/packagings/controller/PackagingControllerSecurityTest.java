package com.enigcode.frozen_backend.packagings.controller;

import com.enigcode.frozen_backend.packagings.service.PackagingService;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PackagingController.class)
class PackagingControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PackagingService packagingService;

    @Test
    @DisplayName("GET /packagings without auth -> 401")
    void getPackagings_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/packagings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /packagings with auth -> 200")
    void getPackagings_authenticated_returns200() throws Exception {
        Mockito.when(packagingService.findAll(Mockito.any())).thenReturn(Page.empty());
        mockMvc.perform(get("/packagings"))
                .andExpect(status().isOk());
    }
}
