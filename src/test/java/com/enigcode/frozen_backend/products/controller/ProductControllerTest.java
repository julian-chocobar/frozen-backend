package com.enigcode.frozen_backend.products.controller;

import com.enigcode.frozen_backend.common.exceptions_configs.GlobalExceptionHandler;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;

import com.enigcode.frozen_backend.products.DTO.ProductCreateDTO;
import com.enigcode.frozen_backend.products.DTO.ProductResponseDTO;
import com.enigcode.frozen_backend.products.DTO.ProductUpdateDTO;
import com.enigcode.frozen_backend.products.service.ProductService;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.*;

@WebMvcTest(controllers = ProductController.class, excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class })
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
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
                                .isAlcoholic(true)
                                .standardQuantity(1.0)
                                .unitMeasurement(com.enigcode.frozen_backend.materials.model.UnitMeasurement.UNIDAD)
                                .build();

                Mockito.when(productService.createProduct(any(ProductCreateDTO.class)))
                                .thenReturn(ProductResponseDTO.builder().id(1L).name("IPA").isActive(true)
                                                .isReady(false).isAlcoholic(true).build());

                var json = objectMapper.writeValueAsString(request);

                mockMvc.perform(post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isCreated());
        }

        @Test
        void testCreateProduct_BadRequest_ShouldReturn400() throws Exception {
                var request = ProductCreateDTO.builder()
                                .name("IPA")
                                .isAlcoholic(true)
                                .build();

                Mockito.when(productService.createProduct(any(ProductCreateDTO.class)))
                                .thenThrow(new BadRequestException("duplicado"));

                var json = objectMapper.writeValueAsString(request);

                mockMvc.perform(post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testCreateProduct_MissingName_ShouldReturn400() throws Exception {
                var request = ProductCreateDTO.builder()
                                .name(null)
                                .isAlcoholic(true)
                                .build();
                var json = objectMapper.writeValueAsString(request);

                mockMvc.perform(post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testCreateProduct_MissingIsAlcoholic_ShouldReturn400() throws Exception {
                var request = ProductCreateDTO.builder()
                                .name("APA")
                                .isAlcoholic(null)
                                .build();
                var json = objectMapper.writeValueAsString(request);

                mockMvc.perform(post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testMarkAsReady() throws Exception {
                Mockito.when(productService.markAsReady(eq(1L)))
                                .thenReturn(ProductResponseDTO.builder().id(1L).name("IPA").isReady(true).build());

                mockMvc.perform(patch("/products/1/mark-ready"))
                                .andExpect(status().isOk());
        }

        @Test
        void testMarkAsReady_NotFound_ShouldReturn404() throws Exception {
                Mockito.when(productService.markAsReady(eq(999L)))
                                .thenThrow(new ResourceNotFoundException("no encontrado"));

                mockMvc.perform(patch("/products/999/mark-ready"))
                                .andExpect(status().isNotFound());
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

        @Test
        void testGetProducts_PaginationMetadata_ShouldReturnFields() throws Exception {
                Mockito.when(productService.findAll(any(), any(Pageable.class)))
                                .thenReturn(new PageImpl<>(java.util.List.of(), PageRequest.of(2, 3), 0));

                mockMvc.perform(get("/products"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").exists())
                                .andExpect(jsonPath("$.currentPage").value(2))
                                .andExpect(jsonPath("$.size").value(3))
                                .andExpect(jsonPath("$.totalItems").value(0))
                                .andExpect(jsonPath("$.totalPages").value(0))
                                .andExpect(jsonPath("$.hasNext").value(false))
                                .andExpect(jsonPath("$.hasPrevious").value(true))
                                .andExpect(jsonPath("$.isFirst").value(false))
                                .andExpect(jsonPath("$.isLast").value(true));
        }

        @Test
        void testUpdateProduct_NotFound_ShouldReturn404() throws Exception {
                var update = ProductUpdateDTO.builder()
                                .name("IPA Especial")
                                .isAlcoholic(true)
                                .build();

                Mockito.when(productService.updateProduct(eq(999L), any(ProductUpdateDTO.class)))
                                .thenThrow(new ResourceNotFoundException("no encontrado"));

                var updateJson = objectMapper.writeValueAsString(update);

                mockMvc.perform(patch("/products/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson))
                                .andExpect(status().isNotFound());
        }
}
