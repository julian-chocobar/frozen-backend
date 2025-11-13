package com.enigcode.frozen_backend.quality_parameters.DTO;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityParameterUpdateDTO {
    @Size(max = 255)
    private String description;

    @Size(max = 50)
    private String unit;

    @Size(max = 500)
    private String information;
}
