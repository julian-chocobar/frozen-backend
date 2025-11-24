package com.enigcode.frozen_backend.batches.specification;

import com.enigcode.frozen_backend.batches.DTO.BatchFilterDTO;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BatchSpecificationTest {

    @Test
    void createFilter_withNulls_returnsSpecification() {
        BatchFilterDTO dto = new BatchFilterDTO();
        var spec = BatchSpecification.createFilter(dto);
        assertThat(spec).isNotNull();
    }

    @Test
    void createFilter_withProductIdAndStatus_returnsSpecification() {
        BatchFilterDTO dto = new BatchFilterDTO();
        dto.setProductId(5L);
        dto.setStatus(null);
        var spec = BatchSpecification.createFilter(dto);
        assertThat(spec).isNotNull();
    }
}
