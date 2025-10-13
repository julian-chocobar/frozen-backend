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

    @NotNull
    private Long productPhaseId;

    @NotNull
    private Long materialID;

    @NotNull
    @DecimalMin(value = "0.0")
    private Double quantity;
}
