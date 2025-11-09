package com.enigcode.frozen_backend.quality_parameters.DTO;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    private Boolean isActive;
}
