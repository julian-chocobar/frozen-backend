package com.enigcode.frozen_backend.products.DTO;

import com.enigcode.frozen_backend.materials.model.UnitMeasurement;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductFilterDTO {
    private String name;
    private Boolean isActive;
    private Boolean isAlcoholic;
    private Boolean isReady;
    private Double standardQuantity;
    private UnitMeasurement unitMeasurement;
}
