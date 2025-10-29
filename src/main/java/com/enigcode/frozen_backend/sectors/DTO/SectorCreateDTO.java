package com.enigcode.frozen_backend.sectors.DTO;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.sectors.model.SectorType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectorCreateDTO {

    @NotNull
    private String name;

    @NotNull
    private Long supervisorId;

    @NotNull
    private SectorType type;

    private Phase phase;
    private Double productionCapacity;
    private Boolean isTimeActive;
}
