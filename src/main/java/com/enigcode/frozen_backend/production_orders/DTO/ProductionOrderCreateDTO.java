package com.enigcode.frozen_backend.production_orders.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionOrderCreateDTO {
    @NotNull
    private Long productId;

    @NotNull
    @DecimalMin(value = "0.0")
    private Double quantity;

    @NotNull
    private Long packagingId;

    @NotNull
    private OffsetDateTime plannedDate;
}
