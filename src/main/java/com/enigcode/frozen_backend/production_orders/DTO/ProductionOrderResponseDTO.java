package com.enigcode.frozen_backend.production_orders.DTO;

import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.production_orders.Model.OrderStatus;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionOrderResponseDTO {
    private Long id;
    private Long batchId;
    private String batchCode;
    private String packagingName;
    private String productName;
    private OrderStatus status;
    private OffsetDateTime validationDate;
    private Double quantity;
    private UnitMeasurement unitMeasurement;
    private OffsetDateTime plannedDate;
    private OffsetDateTime startDate;
    private OffsetDateTime estimatedCompletedDate;
    private OffsetDateTime completedDate;
    private OffsetDateTime creationDate;
    private String createdByUserName;
    private Long createdByUserId;
    private String approvedByUserName;
    private Long approvedByUserId;
}
