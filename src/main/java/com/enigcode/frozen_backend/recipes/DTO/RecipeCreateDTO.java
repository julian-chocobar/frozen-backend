package com.enigcode.frozen_backend.recipes.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeCreateDTO {
    @NotNull(message = "Se requiere un id de product phase")
    private Long productPhaseId;

    @NotNull(message = "Se requiere un id de material")
    private Long materialId;

    @NotNull(message = "Se requiere asignar una cantidad de material")
    @DecimalMin(value = "0.0")
    private Double quantity;
}
