package com.enigcode.frozen_backend.production_phases.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.production_phases.DTO.ProductionPhaseResponseDTO;
import com.enigcode.frozen_backend.production_phases.DTO.ProductionPhaseUnderReviewDTO;
import com.enigcode.frozen_backend.production_phases.mapper.ProductionPhaseMapper;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhaseStatus;
import com.enigcode.frozen_backend.production_phases.repository.ProductionPhaseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductionPhaseServiceImpl implements ProductionPhaseService{
    private final ProductionPhaseRepository productionPhaseRepository;
    private final ProductionPhaseMapper productionPhaseMapper;

    @Override
    @Transactional
    public ProductionPhaseResponseDTO setUnderReview(Long id, ProductionPhaseUnderReviewDTO dto) {
        ProductionPhase productionPhase = productionPhaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró production phase de id " + id));

        if(!productionPhase.getStatus().equals(ProductionPhaseStatus.EN_PROCESO) &&
                !productionPhase.getStatus().equals(ProductionPhaseStatus.SIENDO_AJUSTADA))
            throw new BadRequestException("La fase no se encuentra en estado " + ProductionPhaseStatus.EN_PROCESO
            + " o " + ProductionPhaseStatus.SIENDO_AJUSTADA);

        ProductionPhase updatedProductionPhase = productionPhaseMapper.partialUpdate(dto, productionPhase);
        updatedProductionPhase.setStatus(ProductionPhaseStatus.BAJO_REVISION);

        ProductionPhase savedProductionPhase = productionPhaseRepository.save(updatedProductionPhase);

        return productionPhaseMapper.toResponseDTO(savedProductionPhase);
    }
}
