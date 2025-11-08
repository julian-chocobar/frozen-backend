package com.enigcode.frozen_backend.production_phases_qualities.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionPhaseQualityUpdateDTO {
    private String value;
    private Boolean isApproved;
}
