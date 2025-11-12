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

    /**
     * Versión del parámetro. Si no se especifica, se tomará la versión actual + 1
     * para la fase correspondiente. Generalmente manejado automáticamente por el
     * servicio.
     */
    private Integer version;

    /**
     * Si el parámetro está activo. Por defecto true.
     * Generalmente manejado automáticamente por el servicio.
     */
    @Builder.Default
    private Boolean isActive = true;
}
