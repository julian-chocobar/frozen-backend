package com.enigcode.frozen_backend.materials.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationValidationRequestDTO {
    @NotBlank(message = "La zona es requerida")
    private String zone;

    @NotBlank(message = "La sección es requerida")
    private String section;
}