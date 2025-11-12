package com.enigcode.frozen_backend.production_phases_qualities.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionPhaseQualityUpdateDTO {
    private String value;
    private Boolean isApproved;
    private Boolean isActive;
}
