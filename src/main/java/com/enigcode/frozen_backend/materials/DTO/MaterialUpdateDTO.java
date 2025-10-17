package com.enigcode.frozen_backend.materials.DTO;

import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialUpdateDTO {
    private String name;
    private MaterialType type;
    private String supplier;
    @DecimalMin(value = "0.0", message = "El valor no puede ser menor a 0")
    private Double value;
    private UnitMeasurement unitMeasurement;
    @DecimalMin(value = "0.0", message = "El umbral no puede ser menor a 0")
    private Double threshold;
}