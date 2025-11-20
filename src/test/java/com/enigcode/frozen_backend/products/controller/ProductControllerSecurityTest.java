package com.enigcode.frozen_backend.products.controller;

import com.enigcode.frozen_backend.products.service.ProductService;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;
    @MockitoBean
    private com.enigcode.frozen_backend.common.SecurityProperties securityProperties;

    @Test
    @DisplayName("GET /products without auth -> 401")
    void getProducts_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /products with auth -> 200")
    void getProducts_authenticated_returns200() throws Exception {
        Mockito.when(productService.findAll(Mockito.any(), Mockito.any())).thenReturn(Page.empty());
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());
    }
}
