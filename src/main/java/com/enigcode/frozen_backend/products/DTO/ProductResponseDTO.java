package com.enigcode.frozen_backend.products.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

import com.enigcode.frozen_backend.materials.model.UnitMeasurement;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private Long id;
    private String name;
    private Boolean isAlcoholic;
    private Boolean isActive;
    private Boolean isReady;
    private OffsetDateTime creationDate;
    private Double standardQuantity;
    private UnitMeasurement unitMeasurement;
}
