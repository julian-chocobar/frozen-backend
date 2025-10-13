package com.enigcode.frozen_backend.products.service;

import com.enigcode.frozen_backend.products.DTO.ProductCreateDTO;
import com.enigcode.frozen_backend.products.DTO.ProductFilterDTO;
import com.enigcode.frozen_backend.products.DTO.ProductResponseDTO;
import com.enigcode.frozen_backend.products.DTO.ProductUpdateDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponseDTO createProduct(ProductCreateDTO productCreateDTO);
    ProductResponseDTO markAsReady(Long id);
    ProductResponseDTO updateProduct(Long id, ProductUpdateDTO productUpdateDTO);
    ProductResponseDTO toggleActive(Long id);
    Page<ProductResponseDTO> findAll(ProductFilterDTO filterDTO, Pageable pageable);
    ProductResponseDTO getProduct(Long id);
}
