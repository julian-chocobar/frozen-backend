package com.enigcode.frozen_backend.recipes.DTO;

import jakarta.validation.constraints.DecimalMin;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeUpdateDTO {

    private Long materialId;

    @DecimalMin(value = "0.0")
    private Double quantity;
}
