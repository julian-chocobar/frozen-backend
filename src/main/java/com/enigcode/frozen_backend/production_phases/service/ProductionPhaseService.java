package com.enigcode.frozen_backend.production_phases.service;

import com.enigcode.frozen_backend.production_phases.DTO.ProductionPhaseResponseDTO;
import com.enigcode.frozen_backend.production_phases.DTO.ProductionPhaseUnderReviewDTO;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;

import java.util.List;

public interface ProductionPhaseService {
    ProductionPhaseResponseDTO setUnderReview(Long id, ProductionPhaseUnderReviewDTO dto);
    ProductionPhaseResponseDTO getProductionPhase(Long id);
    List<ProductionPhaseResponseDTO> getProductionPhasesByBatch(Long id);
    void suspendProductionPhases(List<ProductionPhase> remainingProductionPhases);
}
