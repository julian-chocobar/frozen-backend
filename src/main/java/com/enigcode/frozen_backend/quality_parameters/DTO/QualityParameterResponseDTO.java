package com.enigcode.frozen_backend.quality_parameters.DTO;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityParameterResponseDTO {
    private Long id;
    private Phase phase;
    private Boolean isCritical;
    private String name;
    private String description;
    private String unit;
    private String information;
    private Boolean isActive;
}
