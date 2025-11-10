package com.enigcode.frozen_backend.materials.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.materials.model.WarehouseZone;

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

    // Ubicación opcional al crear - el sistema asignará valores por defecto si no
    // se proporciona
    private WarehouseZone warehouseZone;

    @Size(max = 10, message = "La sección del almacén no puede tener más de 10 caracteres")
    private String warehouseSection;

    @Min(value = 1, message = "El nivel debe estar entre 1 y 3")
    @DecimalMin(value = "1", message = "El nivel debe estar entre 1 y 3")
    private Integer warehouseLevel;
}