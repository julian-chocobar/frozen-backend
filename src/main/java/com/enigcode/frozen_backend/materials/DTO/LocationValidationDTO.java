package com.enigcode.frozen_backend.materials.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationValidationDTO {
    private Boolean isValid;
    private String message;
}