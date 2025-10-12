package com.enigcode.frozen_backend.materials.DTO;

import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.MeasurementUnit;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialDetailDTO {
    private String code;
    private String name;
    private MaterialType type;
    private String supplier;
    private Double value;
    private Double stock;
    private MeasurementUnit unitMeasurement;
    private Double threshold;
    private boolean is_below_threshold;
    private OffsetDateTime creationDate;
    private OffsetDateTime lastUpdateDate;
}
