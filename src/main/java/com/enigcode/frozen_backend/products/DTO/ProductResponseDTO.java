package com.enigcode.frozen_backend.products.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.enigcode.frozen_backend.materials.model.MeasurementUnit;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private Long id;
    private String name;
    private MeasurementUnit measurementUnit;
    private Boolean isAlcoholic;
    private Boolean isActive;
    private OffsetDateTime creationDate;
    private String packagingStandardName;
    private Double packagingStandardQuantity;
}
