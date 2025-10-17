package com.enigcode.frozen_backend.materials.DTO;

import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialDetailDTO {
    private Long id;
    private String code;
    private String name;
    private MaterialType type;
    private String supplier;
    private Double value;
    private Double stock;
    private UnitMeasurement unitMeasurement;
    private Double threshold;
    private Boolean isBelowThreshold;
    private OffsetDateTime creationDate;
    private OffsetDateTime lastUpdateDate;
}