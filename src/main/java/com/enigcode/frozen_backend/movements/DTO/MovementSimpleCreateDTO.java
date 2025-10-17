package com.enigcode.frozen_backend.movements.DTO;

import com.enigcode.frozen_backend.materials.model.Material;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovementSimpleCreateDTO {
    @NotNull
    private Material material;

    @NotNull
    @DecimalMin(value = "0.0")
    private Double stock;
}
