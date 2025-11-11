package com.enigcode.frozen_backend.quality_parameters.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterCreateDTO;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterResponseDTO;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterSimpleDTO;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterUpdateDTO;
import com.enigcode.frozen_backend.quality_parameters.mapper.QualityParameterMapper;
import com.enigcode.frozen_backend.quality_parameters.model.QualityParameter;
import com.enigcode.frozen_backend.quality_parameters.repository.QualityParameterRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QualityParameterServiceImpl implements QualityParameterService {
    private final QualityParameterMapper qualityParameterMapper;
    private final QualityParameterRepository qualityParameterRepository;

    @Override
    @Transactional
    public QualityParameterResponseDTO createQualityParameter(QualityParameterCreateDTO createDto) {
        QualityParameter qualityParameter = qualityParameterMapper.toEntity(createDto);

        QualityParameter savedQualityParameter = qualityParameterRepository.saveAndFlush(qualityParameter);

        return qualityParameterMapper.toResponseDTO(savedQualityParameter);
    }

    @Override
    @Transactional
    public QualityParameterResponseDTO updateQualityParameter(Long id, QualityParameterUpdateDTO updateDto) {
        QualityParameter qualityParameter = qualityParameterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró parámetro de calidad con id " + id));
        qualityParameter.setDescription(updateDto.getDescription());

        QualityParameter savedParameter = qualityParameterRepository.save(qualityParameter);
        return qualityParameterMapper.toResponseDTO(savedParameter);
    }

    @Override
    @Transactional
    public QualityParameterResponseDTO toggleActive(Long id) {
        QualityParameter qualityParameter = qualityParameterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró parámetro de calidad con id " + id));
        qualityParameter.toggleActive();

        QualityParameter savedParameter = qualityParameterRepository.save(qualityParameter);
        return qualityParameterMapper.toResponseDTO(savedParameter);
    }

    @Override
    @Transactional
    public QualityParameterResponseDTO getQualityParameter(Long id) {
        QualityParameter qualityParameter = qualityParameterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró parámetro de calidad con id " + id));
        return qualityParameterMapper.toResponseDTO(qualityParameter);
    }

    @Override
    @Transactional
    public List<QualityParameterResponseDTO> getQualityParameters() {
        List<QualityParameter> qualityParameters = qualityParameterRepository.findAll();

        return qualityParameters.stream().map(qualityParameterMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional
    public List<QualityParameterSimpleDTO> getActiveQualityParameters() {
        List<QualityParameter> activeParameters = qualityParameterRepository.findByIsActiveTrueOrderByNameAsc();
        return activeParameters.stream()
                .map(qualityParameterMapper::toSimpleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<QualityParameterSimpleDTO> getActiveQualityParametersByPhase(
            com.enigcode.frozen_backend.product_phases.model.Phase phase) {
        List<QualityParameter> activeParameters = qualityParameterRepository
                .findByPhaseAndIsActiveTrueOrderByNameAsc(phase);
        return activeParameters.stream()
                .map(qualityParameterMapper::toSimpleDTO)
                .collect(Collectors.toList());
    }

}
