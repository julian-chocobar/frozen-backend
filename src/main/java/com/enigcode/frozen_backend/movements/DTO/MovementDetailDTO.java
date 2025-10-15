package com.enigcode.frozen_backend.movements.DTO;

import com.enigcode.frozen_backend.materials.model.MeasurementUnit;
import com.enigcode.frozen_backend.movements.model.MovementType;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovementDetailDTO {
    private Long id;

    private MovementType type;

    private OffsetDateTime realizationDate;

    private Double stock;

    private MeasurementUnit unitMeasurement;

    private String materialType;

    private String materialCode;

    private String materialId;

    private String materialName;

    private String reason;
}
