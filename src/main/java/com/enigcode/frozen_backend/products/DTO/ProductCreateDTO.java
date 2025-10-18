package com.enigcode.frozen_backend.products.DTO;

import com.enigcode.frozen_backend.materials.model.UnitMeasurement;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateDTO {
    @NotNull(message = "Se debe asignar un nombre al producto")
    private String name;

    @NotNull(message = "Se debe asignar un alcoholismo al producto")
    private Boolean isAlcoholic;

    @NotNull(message = "Se debe asignar una cantidad estandar")
    @DecimalMin(value = "0.0", inclusive = false, message = "La cantidad debe ser mayor a 0")
    private Double standardQuantity;

    @NotNull(message = "Se debe asingar una unidad de medida")
    private UnitMeasurement unitMeasurement;
}
