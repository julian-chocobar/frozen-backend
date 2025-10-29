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
public class SectorUpdateDTO {
    private String name;
    private Long supervisorId;
    private SectorType type;
    private Phase phase;
    private Double productionCapacity;
    private Boolean isTimeActive;
}
