package com.enigcode.frozen_backend.materials.DTO;

import com.enigcode.frozen_backend.materials.model.WarehouseCoordinates;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationValidationDTO {
    private Boolean isValid;
    private WarehouseCoordinates coordinates;
    private String message;
}