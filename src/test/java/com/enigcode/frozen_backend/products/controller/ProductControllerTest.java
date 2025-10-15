package com.enigcode.frozen_backend.products.controller;

import com.enigcode.frozen_backend.products.DTO.ProductCreateDTO;
import com.enigcode.frozen_backend.products.DTO.ProductResponseDTO;
import com.enigcode.frozen_backend.products.DTO.ProductUpdateDTO;
import com.enigcode.frozen_backend.products.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.*;

@WebMvcTest(controllers = ProductController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    void testCreateProduct() throws Exception {
        var request = ProductCreateDTO.builder()
                .name("IPA")
                .packagingStandardId(1L)
                .isAlcoholic(true)
                .build();

        Mockito.when(productService.createProduct(any(ProductCreateDTO.class)))
                .thenReturn(ProductResponseDTO.builder().id(1L).name("IPA").isActive(true).isReady(false).isAlcoholic(true).build());

        var json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void testMarkAsReady() throws Exception {
        Mockito.when(productService.markAsReady(eq(1L)))
                .thenReturn(ProductResponseDTO.builder().id(1L).name("IPA").isReady(true).build());

        mockMvc.perform(patch("/products/1/mark-ready"))
                .andExpect(status().isOk());
    }

    @Test
    void testToggleActive() throws Exception {
        Mockito.when(productService.toggleActive(eq(1L)))
                .thenReturn(ProductResponseDTO.builder().id(1L).name("IPA").isActive(false).build());

        mockMvc.perform(patch("/products/1/toggle-active"))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateProduct() throws Exception {
        var update = ProductUpdateDTO.builder()
                .name("IPA Especial")
                .packagingStandardId(2L)
                .isAlcoholic(true)
                .build();

        Mockito.when(productService.updateProduct(eq(1L), any(ProductUpdateDTO.class)))
                .thenReturn(ProductResponseDTO.builder().id(1L).name("IPA Especial").build());

        var updateJson = objectMapper.writeValueAsString(update);

        mockMvc.perform(patch("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isCreated());
    }
}

