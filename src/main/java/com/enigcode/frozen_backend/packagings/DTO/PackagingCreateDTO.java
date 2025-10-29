package com.enigcode.frozen_backend.packagings.DTO;

import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackagingCreateDTO {
    @NotNull
    private String name;

    @NotNull
    private Long packagingMaterialId;

    @NotNull
    private Long labelingMaterialId;

    @NotNull
    private UnitMeasurement unitMeasurement;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "La cantidad debe ser mayor a 0")
    private Double quantity;

}
