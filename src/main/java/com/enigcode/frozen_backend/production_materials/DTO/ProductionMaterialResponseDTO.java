package com.enigcode.frozen_backend.production_materials.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionMaterialResponseDTO {
    private Long id;
    private Long materialId;
    private String materialCode;
    private Long productionPhaseId;
    private Double quantity;
}
