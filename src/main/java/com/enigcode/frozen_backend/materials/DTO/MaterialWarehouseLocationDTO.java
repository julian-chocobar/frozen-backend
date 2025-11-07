package com.enigcode.frozen_backend.materials.DTO;

import com.enigcode.frozen_backend.materials.model.MaterialType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialWarehouseLocationDTO {
    private Long id;
    private String code;
    private String name;
    private MaterialType type;
    private Double stock;
    private Double reservedStock;
    private Double threshold;
    private Boolean isBelowThreshold;

    // Ubicación en el almacén
    private Double warehouseX;
    private Double warehouseY;
    private String warehouseZone;
    private String warehouseSection;
    private Integer warehouseLevel;
}