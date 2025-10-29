package com.enigcode.frozen_backend.packagings.DTO;

import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackagingUpdateDTO {
    private String name;
    private Long packagingMaterialId;
    private Long labelingMaterialId;
    private UnitMeasurement unitMeasurement;

    @DecimalMin(value = "0.0", inclusive = false, message = "La cantidad debe ser mayor a 0")
    private Double quantity;
}
