package com.enigcode.frozen_backend.products.DTO;

import com.enigcode.frozen_backend.materials.model.UnitMeasurement;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateDTO {
    private String name;
    private Boolean isAlcoholic;
    private Double standardQuantity;
    private UnitMeasurement unitMeasurement; 
}
