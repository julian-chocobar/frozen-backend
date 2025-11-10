package com.enigcode.frozen_backend.materials.DTO;

import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.materials.model.WarehouseZone;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
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

    // Campos de ubicación del almacén (opcionales)
    private WarehouseZone warehouseZone;

    @Pattern(regexp = "^[A-C][1-5]$", message = "La sección debe tener formato válido (ej: A1, B2, C3)")
    private String warehouseSection;

    @Min(value = 1, message = "El nivel debe ser entre 1 y 3")
    @Max(value = 3, message = "El nivel debe ser entre 1 y 3")
    private Integer warehouseLevel;
}