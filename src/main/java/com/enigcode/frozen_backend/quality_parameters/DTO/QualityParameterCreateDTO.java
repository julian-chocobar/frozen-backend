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
public class QualityParameterCreateDTO {
    @NotNull
    private Phase phase;

    @NotNull
    private Boolean isCritical;

    @Size(max = 20)
    @NotNull
    private String name;

    @Size(max = 255)
    private String description;
}
