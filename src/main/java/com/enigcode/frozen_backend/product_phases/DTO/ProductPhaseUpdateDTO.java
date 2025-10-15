package com.enigcode.frozen_backend.product_phases.DTO;

import com.enigcode.frozen_backend.materials.model.MeasurementUnit;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPhaseUpdateDTO {

    @DecimalMin(value = "0.0")
    private Double input;

    @DecimalMin(value = "0.0")
    private Double output;

    private MeasurementUnit outputUnit;

    @DecimalMin(value = "0.0")
    @JsonAlias({"estimatedTime"})
    private Double estimatedHours;
}
