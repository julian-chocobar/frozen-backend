package com.enigcode.frozen_backend.production_phases_qualities.service;

import com.enigcode.frozen_backend.production_phases_qualities.DTO.ProductionPhaseQualityCreateDTO;
import com.enigcode.frozen_backend.production_phases_qualities.DTO.ProductionPhaseQualityResponseDTO;
import com.enigcode.frozen_backend.production_phases_qualities.DTO.ProductionPhaseQualityUpdateDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface ProductionPhaseQualityService {
    ProductionPhaseQualityResponseDTO createProductionPhaseQuality(@Valid ProductionPhaseQualityCreateDTO dto);
    ProductionPhaseQualityResponseDTO updateProductionPhaseQuality(Long id, @Valid ProductionPhaseQualityUpdateDTO dto);
    List<ProductionPhaseQualityResponseDTO> getProductionPhaseQualityByPhase(Long id);
    List<ProductionPhaseQualityResponseDTO> getProductionPhaseQualityByBatch(Long id);
    ProductionPhaseQualityResponseDTO getProductionPhaseQuality(Long id);
}
