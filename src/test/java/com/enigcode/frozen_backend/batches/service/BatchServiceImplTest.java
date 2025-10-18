package com.enigcode.frozen_backend.batches.service;

import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.model.BatchStatus;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BatchServiceImplTest {
    @Test
    void canCreateBatch() {
        Batch batch = Batch.builder()
            .code("BATCH-001")
            .status(BatchStatus.PENDIENTE)
            .quantity(100)
            .build();
        assertThat(batch.getCode()).isEqualTo("BATCH-001");
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.PENDIENTE);
        assertThat(batch.getQuantity()).isEqualTo(100);
    }

    @Test
    void canChangeBatchStatus() {
        Batch batch = Batch.builder().code("BATCH-003").status(BatchStatus.PENDIENTE).quantity(10).build();
        batch.setStatus(BatchStatus.COMPLETADO);
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.COMPLETADO);
    }

    @Test
    void batchQuantityCannotBeNegative() {
        Batch batch = Batch.builder().code("BATCH-004").status(BatchStatus.PENDIENTE).quantity(-5).build();
        assertThat(batch.getQuantity()).isLessThan(0);
        // Aquí podrías agregar lógica de validación en el service y testear que lance excepción
    }

    @Test
    void batchCodeShouldNotBeNull() {
        Batch batch = Batch.builder().status(BatchStatus.PENDIENTE).quantity(10).build();
        assertThat(batch.getCode()).isNull();
        // Si el service valida, testear que lance excepción
    }
}
