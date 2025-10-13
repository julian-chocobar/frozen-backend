package com.enigcode.frozen_backend.product_phases.service;

import com.enigcode.frozen_backend.product_phases.mapper.ProductPhaseMapper;
import com.enigcode.frozen_backend.product_phases.repository.ProductPhaseRepository;
import com.enigcode.frozen_backend.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductPhaseServiceImpl {

    final ProductPhaseRepository productPhaseRepository;
    final ProductRepository productRepository;
    final ProductPhaseMapper productPhaseMapper;
}
