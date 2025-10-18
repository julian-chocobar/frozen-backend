package com.enigcode.frozen_backend.batches.service;

import com.enigcode.frozen_backend.batches.DTO.BatchResponseDTO;
import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import com.enigcode.frozen_backend.products.model.Product;

public interface BatchService {
    Batch createBatch(ProductionOrderCreateDTO createDTO, Product product);

    BatchResponseDTO getBatch(Long id);
}
