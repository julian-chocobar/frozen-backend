package com.enigcode.frozen_backend.materials.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialCreateDTO {
    @NotNull(message = "Se debe ingresar un nombre al material")
    private String name;

    @NotNull(message = "El tipo para el material no puede ser null")
    private MaterialType type;

    private String supplier;

    @DecimalMin(value = "0.0", message = "El valor no puede ser menor a 0")
    private Double value;

    @DecimalMin(value = "0.0", message = "El stock no puede ser menor a 0")
    private Double stock;

    @NotNull(message = "Se debe asignar una unidad de medida al material")
    private UnitMeasurement unitMeasurement;

    @NotNull(message = "Se debe asignar un umbral minimo")
    @DecimalMin(value = "0.0", message = "El umbral no puede ser menor a 0")
    private Double threshold;
}