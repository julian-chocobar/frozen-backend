package com.enigcode.frozen_backend.production_phases.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionPhaseUnderReviewDTO {
    @DecimalMin(value = "0.0")
    @NotNull
    private Double input;

    @DecimalMin(value = "0.0")
    @NotNull
    private Double output;
}
