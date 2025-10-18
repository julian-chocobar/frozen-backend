package com.enigcode.frozen_backend.batches.DTO;

import com.enigcode.frozen_backend.batches.model.BatchStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchFilterDTO {
    private Long productId;
    private BatchStatus status;
}
