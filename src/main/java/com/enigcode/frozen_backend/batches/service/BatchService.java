package com.enigcode.frozen_backend.batches.service;

import com.enigcode.frozen_backend.batches.DTO.BatchFilterDTO;
import com.enigcode.frozen_backend.batches.DTO.BatchResponseDTO;
import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import com.enigcode.frozen_backend.products.model.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BatchService {
    Batch createBatch(ProductionOrderCreateDTO createDTO, Product product);

    @Transactional
    void cancelBatch(Batch batch);

    Page<BatchResponseDTO> findAll(BatchFilterDTO filterDTO, Pageable pageable);

    BatchResponseDTO getBatch(Long id);

    BatchResponseDTO cancelBatch(Long id);

    void suspendProductionPhases(List<ProductionPhase> remainingProductionPhases);

    void processBatchesForToday();

    void startNextPhase(Batch batch);

    void completeBatch(Batch batch);
}
