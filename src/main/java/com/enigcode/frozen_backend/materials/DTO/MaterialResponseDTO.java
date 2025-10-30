package com.enigcode.frozen_backend.materials.DTO;

import lombok.*;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialResponseDTO {
    private Long id;
    private String code;
    private String name;
    private MaterialType type;
    private String supplier;
    private Double value;
    private Double totalStock;
    private UnitMeasurement unitMeasurement;
    private Double threshold;
    private Boolean isBelowThreshold;
    private Double stock;
    private Double reservedStock;
    private Boolean isActive;
}