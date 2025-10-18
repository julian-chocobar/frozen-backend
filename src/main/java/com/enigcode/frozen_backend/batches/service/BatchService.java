package com.enigcode.frozen_backend.batches.service;

import com.enigcode.frozen_backend.batches.DTO.BatchFilterDTO;
import com.enigcode.frozen_backend.batches.DTO.BatchResponseDTO;
import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import com.enigcode.frozen_backend.products.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BatchService {
    Batch createBatch(ProductionOrderCreateDTO createDTO, Product product);
    Page<BatchResponseDTO> findAll(BatchFilterDTO filterDTO, Pageable pageable);
    BatchResponseDTO getBatch(Long id);
}
