package com.enigcode.frozen_backend.movements.DTO;

import com.enigcode.frozen_backend.movements.model.MovementType;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovementFilterDTO {
    private MovementType type;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private Long materialId;
}