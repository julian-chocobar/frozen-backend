package com.enigcode.frozen_backend.materials.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialLocationUpdateDTO {
    @DecimalMin(value = "0.0", message = "La coordenada X debe ser mayor o igual a 0")
    private Double warehouseX;

    @DecimalMin(value = "0.0", message = "La coordenada Y debe ser mayor o igual a 0")
    private Double warehouseY;

    @Size(max = 50, message = "La zona del almacén no puede tener más de 50 caracteres")
    private String warehouseZone;

    @Size(max = 10, message = "La sección del almacén no puede tener más de 10 caracteres")
    private String warehouseSection;

    @Min(value = 1, message = "El nivel del almacén debe ser mayor a 0")
    private Integer warehouseLevel;
}