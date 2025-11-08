package com.enigcode.frozen_backend.production_phases_qualities.DTO;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionPhaseQualityResponseDTO {
    private Long id;
    private String qualityParameterName;
    private Long productionPhaseId;
    private Phase productionPhase;
    private String value;
    private Boolean isApproved;
    private OffsetDateTime realizationDate;
}
