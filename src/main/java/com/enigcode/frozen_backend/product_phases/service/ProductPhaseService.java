package com.enigcode.frozen_backend.product_phases.service;

import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseCreateDTO;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductPhaseService {

    ProductPhaseResponseDTO createProductPhase(ProductPhaseCreateDTO productPhaseCreateDTO);

    Page<ProductPhaseResponseDTO> findAll(Pageable pageable);

    ProductPhaseResponseDTO getProductPhase(Long id);

    List<ProductPhaseResponseDTO> getByProduct(Long productId);
}
