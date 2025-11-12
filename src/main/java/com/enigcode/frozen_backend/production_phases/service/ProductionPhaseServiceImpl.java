package com.enigcode.frozen_backend.production_phases.service;

import com.enigcode.frozen_backend.batches.service.BatchService;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.production_phases.DTO.ProductionPhaseResponseDTO;
import com.enigcode.frozen_backend.production_phases.DTO.ProductionPhaseUnderReviewDTO;
import com.enigcode.frozen_backend.production_phases.mapper.ProductionPhaseMapper;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhaseStatus;
import com.enigcode.frozen_backend.production_phases.repository.ProductionPhaseRepository;
import com.enigcode.frozen_backend.production_phases_qualities.model.ProductionPhaseQuality;
import com.enigcode.frozen_backend.production_phases_qualities.repository.ProductionPhaseQualityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionPhaseServiceImpl implements ProductionPhaseService {
    private final ProductionPhaseRepository productionPhaseRepository;
    private final ProductionPhaseMapper productionPhaseMapper;
    private final BatchService batchService;
    private final ProductionPhaseQualityRepository productionPhaseQualityRepository;
    private final com.enigcode.frozen_backend.production_phases_qualities.service.ProductionPhaseQualityService productionPhaseQualityService;

    @Override
    @Transactional
    public ProductionPhaseResponseDTO setUnderReview(Long id, ProductionPhaseUnderReviewDTO dto) {
        ProductionPhase productionPhase = productionPhaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró production phase de id " + id));

        if (!productionPhase.getStatus().equals(ProductionPhaseStatus.EN_PROCESO) &&
                !productionPhase.getStatus().equals(ProductionPhaseStatus.SIENDO_AJUSTADA))
            throw new BadRequestException("La fase no se encuentra en estado " + ProductionPhaseStatus.EN_PROCESO
                    + " o " + ProductionPhaseStatus.SIENDO_AJUSTADA);

        ProductionPhase updatedProductionPhase = productionPhaseMapper.partialUpdate(dto, productionPhase);
        updatedProductionPhase.setStatus(ProductionPhaseStatus.BAJO_REVISION);

        ProductionPhase savedProductionPhase = productionPhaseRepository.save(updatedProductionPhase);

        return productionPhaseMapper.toResponseDTO(savedProductionPhase);
    }

    @Override
    @Transactional
    public ProductionPhaseResponseDTO getProductionPhase(Long id) {
        ProductionPhase productionPhase = productionPhaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró production phase de id " + id));
        return productionPhaseMapper.toResponseDTO(productionPhase);
    }

    @Override
    @Transactional
    public List<ProductionPhaseResponseDTO> getProductionPhasesByBatch(Long id) {
        List<ProductionPhase> productionPhases = productionPhaseRepository.findAllByBatchIdOrderByPhaseOrderAsc(id);
        if (productionPhases.isEmpty())
            throw new ResourceNotFoundException("No se encontraron production phases asociados al batch " + id);

        return productionPhases.stream().map(productionPhaseMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional
    public ProductionPhaseResponseDTO reviewProductionPhase(Long id) {
        ProductionPhase productionPhase = productionPhaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró production phase de id " + id));

        if (!productionPhase.getStatus().equals(ProductionPhaseStatus.BAJO_REVISION))
            throw new BadRequestException("La fase no se encuentra en estado " + ProductionPhaseStatus.BAJO_REVISION);

        // Solo evaluar parámetros activos (no históricos)
        List<ProductionPhaseQuality> productionPhaseQualities = productionPhaseQualityRepository
                .findAllByProductionPhaseIdAndIsActiveTrue(id);

        if (productionPhaseQualities.isEmpty())
            throw new BadRequestException("Debe haber al menos un parámetro de calidad asignado a la fase");

        List<ProductionPhaseQuality> notApprovedPhaseQualities = productionPhaseQualities.stream()
                .filter(productionPhaseQuality -> !productionPhaseQuality.getIsApproved()).toList();

        if (notApprovedPhaseQualities.isEmpty())
            completeProductionPhase(productionPhase);
        else {
            boolean isNotCriticalError = notApprovedPhaseQualities.stream()
                    .allMatch(productionPhaseQuality -> {
                        return productionPhaseQuality.getQualityParameter().getIsCritical().equals(Boolean.FALSE);
                    });

            if (isNotCriticalError)
                adjustProductionPhase(productionPhase);
            else
                rejectProductionPhase(productionPhase);
        }

        ProductionPhase savedProductionPhase = productionPhaseRepository.save(productionPhase);

        return productionPhaseMapper.toResponseDTO(savedProductionPhase);
    }

    // TODO: enviar notificacion a supervisor de produccion del sector que se
    // requiere ajuste de phase
    private void adjustProductionPhase(ProductionPhase productionPhase) {
        productionPhase.setStatus(ProductionPhaseStatus.SIENDO_AJUSTADA);

        // Crear nueva versión de parámetros para permitir nuevas mediciones
        productionPhaseQualityService.createNewVersionForPhase(productionPhase.getId());
    }

    // TODO: enviar notificacion a supervisor de produccion del sector que se
    // rechazo la fase y cancelo el lote
    private void rejectProductionPhase(ProductionPhase productionPhase) {
        productionPhase.setStatus(ProductionPhaseStatus.RECHAZADA);
        batchService.cancelBatch(productionPhase.getBatch());
    }

    private void completeProductionPhase(ProductionPhase productionPhase) {
        productionPhase.setStatus(ProductionPhaseStatus.COMPLETADA);
        batchService.startNextPhase(productionPhase.getBatch());
    }
}
