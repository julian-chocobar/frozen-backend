package com.enigcode.frozen_backend.production_orders.DTO;

import jakarta.validation.constraints.DecimalMin;
import com.enigcode.frozen_backend.common.validation.FutureOrPresentDate;
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
    @FutureOrPresentDate(message = "La fecha de planificación debe ser una fecha futura o presente")
    private OffsetDateTime plannedDate;
}
