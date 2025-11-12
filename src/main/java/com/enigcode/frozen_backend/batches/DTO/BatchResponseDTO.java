package com.enigcode.frozen_backend.batches.DTO;

import com.enigcode.frozen_backend.batches.model.BatchStatus;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchResponseDTO {
    private Long id;
    private String code;
    private String packagingName;
    private String productName;
    private String orderId;
    private BatchStatus status;
    private Integer quantity;
    private OffsetDateTime creationDate;
    private OffsetDateTime plannedDate;
    private OffsetDateTime startDate;
    private OffsetDateTime estimatedCompletedDate;
    private OffsetDateTime completedDate;
    private String assignedUserName;
    private Long assignedUserId;
}
