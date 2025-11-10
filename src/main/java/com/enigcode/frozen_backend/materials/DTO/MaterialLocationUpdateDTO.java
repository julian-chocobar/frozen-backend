package com.enigcode.frozen_backend.materials.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialLocationUpdateDTO {
    @NotNull(message = "La zona del almacén es obligatoria")
    private com.enigcode.frozen_backend.materials.model.WarehouseZone warehouseZone;

    @NotBlank(message = "La sección del almacén es obligatoria")
    @Size(max = 10, message = "La sección del almacén no puede tener más de 10 caracteres")
    private String warehouseSection;

    @NotNull(message = "El nivel es obligatorio")
    @Min(value = 1, message = "El nivel debe estar entre 1 y 3")
    @Max(value = 3, message = "El nivel debe estar entre 1 y 3")
    private Integer warehouseLevel;
}