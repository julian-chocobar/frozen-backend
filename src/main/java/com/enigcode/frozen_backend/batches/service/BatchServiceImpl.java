package com.enigcode.frozen_backend.batches.service;

import com.enigcode.frozen_backend.batches.mapper.BatchMapper;
import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.model.BatchStatus;
import com.enigcode.frozen_backend.batches.repository.BatchRepository;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.packagings.repository.PackagingRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService{

    final BatchRepository batchRepository;
    final BatchMapper batchMapper;
    final PackagingRepository packagingRepository;

    @Override
    public Batch createBatch(Long packagingId, OffsetDateTime plannedDate, Double orderQuantity) {
        Packaging packaging = packagingRepository.findById(packagingId)
            .orElseThrow(() -> new ResourceNotFoundException("No se encontro un paquete con el id " + packagingId));

        Integer quantityInteger = (int) Math.floor(orderQuantity/packaging.getQuantity());

        Batch batch = Batch.builder()
                        .packaging(packaging)
                        .status(BatchStatus.PENDIENTE)
                        .quantity(quantityInteger)
                        .plannedDate(plannedDate)
                        .creationDate(OffsetDateTime.now())
                        .startDate(null)
                        .completedDate(null)
                        .build();

        Batch savedBatch = batchRepository.saveAndFlush(batch);
        savedBatch.setCode(generateCode(savedBatch));
        return savedBatch;
    }
     private String generateCode(Batch batch) {
        return "LOT-" + batch.getId();
    }
}
