package com.enigcode.frozen_backend.production_phases.service;

import com.enigcode.frozen_backend.batches.service.BatchService;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.movements.DTO.MovementCreateDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementInternalCreateDTO;
import com.enigcode.frozen_backend.movements.model.MovementType;
import com.enigcode.frozen_backend.movements.service.MovementService;
import com.enigcode.frozen_backend.production_materials.model.ProductionMaterial;
import com.enigcode.frozen_backend.production_materials.repository.ProductionMaterialRepository;
import com.enigcode.frozen_backend.production_materials.service.ProductionMaterialService;
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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionPhaseServiceImpl implements ProductionPhaseService{
    private final ProductionPhaseRepository productionPhaseRepository;
    private final ProductionPhaseMapper productionPhaseMapper;
    private final ProductionMaterialRepository productionMaterialRepository;
    private final MovementService movementService;
    private final BatchService batchService;
    private final ProductionPhaseQualityRepository productionPhaseQualityRepository;

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
        List<ProductionPhase> productionPhases = productionPhaseRepository.findAllByBatchId(id);
        if(productionPhases.isEmpty())
            throw new ResourceNotFoundException("No se encontraron production phases asociados al batch " + id);

        return productionPhases.stream().map(productionPhaseMapper::toResponseDTO).toList();
    }

    /**
     * Marca como suspendida las production fases y devuelve los materiales de las mismas
     * @param remainingProductionPhases
     */
    @Override
    @Transactional
    public void suspendProductionPhases(List<ProductionPhase> remainingProductionPhases) {
        List <MovementInternalCreateDTO> materialsMovements = new ArrayList<>();

        remainingProductionPhases.forEach(productionPhase -> {
            productionPhase.setStatus(ProductionPhaseStatus.SUSPENDIDA);
            List<ProductionMaterial> materials =
                    productionMaterialRepository.findAllByProductionPhaseId(productionPhase.getId());

            if(!materials.isEmpty()) {
                materialsMovements.addAll(materials.stream().map(productionMaterial -> {
                    return MovementInternalCreateDTO.builder()
                            .type(MovementType.INGRESO)
                            .stock(productionMaterial.getQuantity())
                            .material(productionMaterial.getMaterial())
                            .reason("Cancelación de lote, material devuelto")
                            .location("Almacen de materias primas")
                            .build();
                }).toList());
            }
            productionPhaseRepository.save(productionPhase);
        });
        movementService.createMovements(materialsMovements);
    }

    @Override
    @Transactional
    public ProductionPhaseResponseDTO reviewProductionPhase(Long id) {
        ProductionPhase productionPhase = productionPhaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró production phase de id " + id));

        if(!productionPhase.getStatus().equals(ProductionPhaseStatus.BAJO_REVISION))
            throw new BadRequestException("La fase no se encuentra en estado " + ProductionPhaseStatus.BAJO_REVISION);

        List<ProductionPhaseQuality> productionPhaseQualities = productionPhaseQualityRepository.findAllByProductionPhaseId(id);

        if(productionPhaseQualities.isEmpty())
            throw new BadRequestException("Debe haber al menos un parámetro de calidad asignado a la fase");

        List<ProductionPhaseQuality> notApprovedPhaseQualities = 
                productionPhaseQualities.stream()
                .filter(productionPhaseQuality -> !productionPhaseQuality.getIsApproved()).toList();
        
        if(notApprovedPhaseQualities.isEmpty()) completeProductionPhase(productionPhase);

        boolean isCriticalError =
                notApprovedPhaseQualities.stream()
                        .allMatch(productionPhaseQuality -> {
                            return productionPhaseQuality.getQualityParameter().getIsCritical().equals(Boolean.FALSE);
                        });

        if(isCriticalError) rejectProductionPhase(productionPhase);

        adjustProductionPhase(productionPhase);

        ProductionPhase savedProductionPhase = productionPhaseRepository.save(productionPhase);

        return productionPhaseMapper.toResponseDTO(savedProductionPhase);
    }

    //TODO: enviar notificacion a supervisor de produccion del sector que se requiere ajuste de phase
    private void adjustProductionPhase(ProductionPhase productionPhase) {
        productionPhase.setStatus(ProductionPhaseStatus.SIENDO_AJUSTADA);
    }

    //TODO: enviar notificacion a supervisor de produccion del sector que se rechazo la fase y cancelo el lote
    private void rejectProductionPhase(ProductionPhase productionPhase) {
        productionPhase.setStatus(ProductionPhaseStatus.RECHAZADA);
        batchService.cancelBatch(productionPhase.getBatch());
    }

    private void completeProductionPhase(ProductionPhase productionPhase) {
        productionPhase.setStatus(ProductionPhaseStatus.COMPLETADA);
        batchService.startNextPhase(productionPhase.getBatch());
    }
}
