package com.enigcode.frozen_backend.packagings.DTO;

import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackagingResponseDTO {
    private Long id;
    private String name;
    private String materialName;
    private UnitMeasurement unitMeasurement;
    private Double quantity;
    private Boolean isActive;
}
