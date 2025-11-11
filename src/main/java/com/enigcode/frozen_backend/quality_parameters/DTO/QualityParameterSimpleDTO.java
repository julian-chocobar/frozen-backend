package com.enigcode.frozen_backend.quality_parameters.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityParameterSimpleDTO {
    private Long id;
    private String name;
}