package com.enigcode.frozen_backend.materials.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationValidationRequestDTO {
    @jakarta.validation.constraints.NotNull(message = "La zona es requerida")
    private com.enigcode.frozen_backend.materials.model.WarehouseZone zone;

    @NotBlank(message = "La sección es requerida")
    private String section;

    @jakarta.validation.constraints.NotNull(message = "El nivel es requerido")
    @jakarta.validation.constraints.Min(value = 1, message = "El nivel debe estar entre 1 y 3")
    @jakarta.validation.constraints.Max(value = 3, message = "El nivel debe estar entre 1 y 3")
    private Integer level;
}