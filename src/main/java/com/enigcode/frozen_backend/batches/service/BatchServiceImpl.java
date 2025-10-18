package com.enigcode.frozen_backend.batches.service;

import com.enigcode.frozen_backend.batches.mapper.BatchMapper;
import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.repository.BatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService{

    final BatchRepository batchRepository;
    final BatchMapper batchMapper;

    @Override
    public Batch createBatch(Long packagingId, OffsetDateTime plannedDate) {
        return null;
    }
}
