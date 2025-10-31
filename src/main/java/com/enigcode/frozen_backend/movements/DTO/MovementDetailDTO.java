package com.enigcode.frozen_backend.movements.DTO;

import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.movements.model.MovementType;
import com.enigcode.frozen_backend.movements.model.MovementStatus;
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
    private MovementStatus status;
    private OffsetDateTime creationDate;
    private OffsetDateTime realizationDate;
    private Long createdByUserId;
    private Long completedByUserId;
    private Double stock;
    private UnitMeasurement unitMeasurement;
    private String materialType;
    private String materialCode;
    private String materialId;
    private String materialName;
    private String reason;
    private String location;
}