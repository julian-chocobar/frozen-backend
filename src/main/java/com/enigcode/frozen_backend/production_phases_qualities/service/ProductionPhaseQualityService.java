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

    /**
     * Obtiene solo los parámetros de calidad activos de una fase
     */
    List<ProductionPhaseQualityResponseDTO> getActiveProductionPhaseQualityByPhase(Long phaseId);

    /**
     * Obtiene solo los parámetros de calidad activos de un lote
     */
    List<ProductionPhaseQualityResponseDTO> getActiveProductionPhaseQualityByBatch(Long batchId);

    /**
     * Crea una nueva versión de parámetros para una fase cuando se requiere ajuste.
     * Marca los parámetros anteriores como históricos y permite crear nuevas
     * mediciones.
     */
    void createNewVersionForPhase(Long phaseId);

    /**
     * Obtiene la versión actual (más alta) de parámetros para una fase
     */
    Integer getCurrentVersionForPhase(Long phaseId);

    /**
     * Aprueba un parámetro de calidad de fase
     */
    ProductionPhaseQualityResponseDTO approveProductionPhaseQuality(Long id);

    /**
     * Desaprueba un parámetro de calidad de fase
     */
    ProductionPhaseQualityResponseDTO disapproveProductionPhaseQuality(Long id);
}
