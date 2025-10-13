package com.enigcode.frozen_backend.product_phases.DTO;

import com.enigcode.frozen_backend.materials.model.MeasurementUnit;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPhaseCreateDTO {

    @NotNull(message = "Se requiere un id de producto")
    private Long productId;

    @NotNull(message = "Se requiere asignar una fase")
    private Phase phase;

    @NotNull(message = "Se requiere asignar una cantidad de output esperado")
    @DecimalMin(value = "0.0")
    private Double output;

    @NotNull(message = "Se requiere una unidad de medida para el output")
    private MeasurementUnit outputUnit;

    @DecimalMin(value = "0.0")
    private Double estimatedHours;
}
