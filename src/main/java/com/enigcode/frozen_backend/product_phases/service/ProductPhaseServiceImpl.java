package com.enigcode.frozen_backend.product_phases.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseResponseDTO;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseUpdateDTO;
import com.enigcode.frozen_backend.product_phases.mapper.ProductPhaseMapper;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import com.enigcode.frozen_backend.product_phases.repository.ProductPhaseRepository;
import com.enigcode.frozen_backend.products.model.Product;
import com.enigcode.frozen_backend.products.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductPhaseServiceImpl implements ProductPhaseService {

    final ProductPhaseRepository productPhaseRepository;
    final ProductRepository productRepository;
    final ProductPhaseMapper productPhaseMapper;

    @Override
    @Transactional
    public ProductPhaseResponseDTO updateProductPhase(Long id, ProductPhaseUpdateDTO productPhaseUpdateDTO) {
        ProductPhase productPhase = productPhaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductPhase no encontrado con ID: " + id));

        productPhaseMapper.partialUpdate(productPhaseUpdateDTO, productPhase);

        ProductPhase savedProductPhase = productPhaseRepository.save(productPhase);
        return productPhaseMapper.toResponseDto(savedProductPhase);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<ProductPhaseResponseDTO> findAll(Pageable pageable) {
        Page<ProductPhase> productPhases = productPhaseRepository.findAll(pageable);
        return productPhases.map(productPhaseMapper::toResponseDto);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public ProductPhaseResponseDTO getProductPhase(Long id) {
        ProductPhase productPhase = productPhaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductPhase no encontrado con ID: " + id));

        return productPhaseMapper.toResponseDto(productPhase);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ProductPhaseResponseDTO> getByProduct(Long productId) {
        List<ProductPhase> phases = productPhaseRepository.findByProductIdOrderByCreationDateAsc(productId);

        if (phases.isEmpty() && !productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product no encontrado con ID: " + productId);
        }

        return phases.stream()
                .map(productPhaseMapper::toResponseDto)
                .collect(Collectors.toList());
    }
}
