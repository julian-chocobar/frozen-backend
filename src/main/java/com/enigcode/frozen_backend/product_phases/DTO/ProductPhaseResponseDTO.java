package com.enigcode.frozen_backend.product_phases.DTO;

import com.enigcode.frozen_backend.materials.model.MeasurementUnit;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPhaseResponseDTO {
    private Long id;
    private Phase phase;
    private Double input;
    private Double output;
    private MeasurementUnit outputUnit;
    private Double estimatedHours;
    private OffsetDateTime creationDate;
}
