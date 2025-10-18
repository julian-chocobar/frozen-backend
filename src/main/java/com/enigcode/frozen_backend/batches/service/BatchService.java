package com.enigcode.frozen_backend.batches.service;

import com.enigcode.frozen_backend.batches.model.Batch;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public interface BatchService {
    Batch createBatch(Long packagingId, OffsetDateTime plannedDate);
}
