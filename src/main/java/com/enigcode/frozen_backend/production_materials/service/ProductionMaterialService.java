package com.enigcode.frozen_backend.production_materials.service;

import com.enigcode.frozen_backend.production_materials.DTO.ProductionMaterialResponseDTO;

import java.util.List;

public interface ProductionMaterialService {
    ProductionMaterialResponseDTO getProductionMaterial(Long id);
    List<ProductionMaterialResponseDTO> getProductionMaterialByPhase(Long id);
    List<ProductionMaterialResponseDTO> getProductionMaterialByBatch(Long id);
}
