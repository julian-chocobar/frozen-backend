package com.enigcode.frozen_backend.production_phases_qualities.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionPhaseQualityCreateDTO {

    @NotNull
    private Long qualityParameterId;

    @NotNull
    private Long productionPhaseId;

    @Size(max = 55)
    @NotNull
    private String value;

    @NotNull
    private Boolean isApproved;
}
