package com.enigcode.frozen_backend.packagings.DTO;

import com.enigcode.frozen_backend.materials.model.MeasurementUnit;
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
    private MeasurementUnit measurementUnit;
    private Double quantity;
    private Boolean isActive;
}
