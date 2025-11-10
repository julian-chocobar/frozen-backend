package com.enigcode.frozen_backend.materials.DTO;

import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.WarehouseZone;
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
    private WarehouseZone warehouseZone;
    private String warehouseSection;
    private Integer warehouseLevel;

    // Coordenadas calculadas para el frontend
    private Double warehouseX; // Coordenada X en el plano SVG
    private Double warehouseY; // Coordenada Y en el plano SVG
    private String levelDisplay; // Ej: "Nivel 2" para mostrar en tooltip

}