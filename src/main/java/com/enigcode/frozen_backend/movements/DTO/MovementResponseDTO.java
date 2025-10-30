package com.enigcode.frozen_backend.movements.DTO;

import java.time.OffsetDateTime;
import lombok.*;

import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.movements.model.MovementType;
import com.enigcode.frozen_backend.movements.model.MovementStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovementResponseDTO {
    private Long id;
    private MovementType type;
    private MovementStatus status;
    private OffsetDateTime creationDate;
    private OffsetDateTime realizationDate;
    private Long createdByUserId;
    private Long completedByUserId;
    private Double stock;
    private String reason;
    private UnitMeasurement unitMeasurement;
    private String materialType;
    private String materialName;
}