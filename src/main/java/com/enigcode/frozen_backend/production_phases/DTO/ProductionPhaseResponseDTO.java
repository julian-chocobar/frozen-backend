package com.enigcode.frozen_backend.production_phases.DTO;

import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhaseStatus;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionPhaseResponseDTO {
    private Long id;
    private Long sectorId;
    private Long batchId;
    private String batchCode;
    private ProductionPhaseStatus status;
    private Phase phase;
    private Double input;
    private Double standardInput;
    private Double output;
    private Double standardOutput;
    private UnitMeasurement outputUnit;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
}
