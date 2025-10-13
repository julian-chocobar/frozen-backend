package com.enigcode.frozen_backend.products.DTO;

import lombok.*;
import com.enigcode.frozen_backend.materials.model.MeasurementUnit;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateDTO {
    private String name;
    private Long packagingStandardID;
    private MeasurementUnit measurementUnit;
    private Boolean isAlcoholic;
}
