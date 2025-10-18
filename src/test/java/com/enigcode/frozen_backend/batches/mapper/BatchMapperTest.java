package com.enigcode.frozen_backend.batches.mapper;

import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.DTO.BatchResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BatchMapperTest {
    
    @Autowired
    private BatchMapper batchMapper;
    
    @Test
    void mapsBatchToResponseDTO() {
        Batch batch = Batch.builder().code("BATCH-002").quantity(50).build();
        BatchResponseDTO dto = batchMapper.toResponseDTO(batch);
        assertThat(dto.getCode()).isEqualTo("BATCH-002");
        assertThat(dto.getQuantity()).isEqualTo(50);
    }
}
