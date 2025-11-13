package com.enigcode.frozen_backend.quality_parameters.service;

import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterCreateDTO;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterResponseDTO;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterUpdateDTO;

import java.util.List;

public interface QualityParameterService {
    QualityParameterResponseDTO createQualityParameter(QualityParameterCreateDTO createDto);

    QualityParameterResponseDTO updateQualityParameter(Long id, QualityParameterUpdateDTO updateDto);

    QualityParameterResponseDTO toggleActive(Long id);

    QualityParameterResponseDTO getQualityParameter(Long id);

    List<QualityParameterResponseDTO> getQualityParameters();

    List<QualityParameterResponseDTO> getActiveQualityParameters();

    List<QualityParameterResponseDTO> getActiveQualityParametersByPhase(
            com.enigcode.frozen_backend.product_phases.model.Phase phase);
}
