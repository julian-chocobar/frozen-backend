package com.enigcode.frozen_backend.production_phases.service;

import com.enigcode.frozen_backend.batches.service.BatchService;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.notifications.service.NotificationService;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.production_phases.DTO.ProductionPhaseResponseDTO;
import com.enigcode.frozen_backend.production_phases.DTO.ProductionPhaseUnderReviewDTO;
import com.enigcode.frozen_backend.production_phases.mapper.ProductionPhaseMapper;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhaseStatus;
import com.enigcode.frozen_backend.production_phases.repository.ProductionPhaseRepository;
import com.enigcode.frozen_backend.production_materials.model.ProductionMaterial;
import com.enigcode.frozen_backend.production_materials.repository.ProductionMaterialRepository;
import com.enigcode.frozen_backend.production_phases_qualities.model.ProductionPhaseQuality;
import com.enigcode.frozen_backend.production_phases_qualities.repository.ProductionPhaseQualityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionPhaseServiceImpl implements ProductionPhaseService {
    private final ProductionPhaseRepository productionPhaseRepository;
    private final ProductionPhaseMapper productionPhaseMapper;
    private final BatchService batchService;
    private final ProductionPhaseQualityRepository productionPhaseQualityRepository;
    private final com.enigcode.frozen_backend.production_phases_qualities.service.ProductionPhaseQualityService productionPhaseQualityService;
    private final NotificationService notificationService;
    private final ProductionMaterialRepository productionMaterialRepository;

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

        // Validar que en MOLIENDA (primera fase) el input no puede ser menor que el
        // output
        if (updatedProductionPhase.getPhase().equals(Phase.MOLIENDA)) {
            if (updatedProductionPhase.getInput() != null && updatedProductionPhase.getOutput() != null
                    && updatedProductionPhase.getInput() < updatedProductionPhase.getOutput()) {
                throw new BadRequestException(
                        "En la fase MOLIENDA (primera fase), el input no puede ser menor que el output");
            }
        }

        ProductionPhase previousPhase = productionPhaseRepository.findPreviousPhase(
                productionPhase.getBatch(),
                productionPhase.getPhaseOrder());

        double movementWaste = 0.0;
        if (previousPhase != null) {
            double expectedInput = (previousPhase.getOutput() * previousPhase.getStandardInput())
                    / previousPhase.getStandardOutput();
            movementWaste = expectedInput - updatedProductionPhase.getInput();
        }

        if (movementWaste < 0)
            movementWaste = 0.0;
        updatedProductionPhase.setMovementWaste(movementWaste);
        
        // Validar que el output no sea mayor que input + ingredientes
        validateOutputNotGreaterThanInputPlusIngredients(updatedProductionPhase);

        ProductionPhase savedProductionPhase = productionPhaseRepository.save(updatedProductionPhase);

        // Notificar a operarios de calidad que hay una fase bajo revisión
        notificationService.createPhaseUnderReviewNotification(
                savedProductionPhase.getId(),
                savedProductionPhase.getBatch().getCode(),
                savedProductionPhase.getPhase().toString());

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

    private void adjustProductionPhase(ProductionPhase productionPhase) {
        productionPhase.setStatus(ProductionPhaseStatus.SIENDO_AJUSTADA);

        // Crear nueva versión de parámetros para permitir nuevas mediciones
        productionPhaseQualityService.createNewVersionForPhase(productionPhase.getId());

        // Notificar al supervisor de producción del sector que se requiere ajuste
        notificationService.createPhaseAdjustmentRequiredNotification(
                productionPhase.getId(),
                productionPhase.getBatch().getCode(),
                productionPhase.getPhase().toString(),
                productionPhase.getSector().getId());
    }

    private void rejectProductionPhase(ProductionPhase productionPhase) {
        productionPhase.setStatus(ProductionPhaseStatus.RECHAZADA);
        batchService.cancelBatch(productionPhase.getBatch());

        // Notificar al supervisor de producción del sector que la fase fue rechazada y
        // el lote cancelado
        notificationService.createPhaseRejectedBatchCancelledNotification(
                productionPhase.getBatch().getId(),
                productionPhase.getBatch().getCode(),
                productionPhase.getPhase().toString(),
                productionPhase.getSector().getId());
    }

    private void completeProductionPhase(ProductionPhase productionPhase) {
        productionPhase.setStatus(ProductionPhaseStatus.COMPLETADA);
        productionPhase.setEndDate(OffsetDateTime.now());
        if (productionPhase.getPhase().equals(Phase.ENVASADO)) {
            batchService.completeBatch(productionPhase.getBatch());
        } else {
            batchService.startNextPhase(productionPhase.getBatch());
        }
    }
    
    /**
     * Valida que el output de una fase no sea mayor que el input + total de ingredientes.
     * El output puede ser menor o igual debido a posibles mermas.
     * 
     * @param productionPhase Fase a validar
     */
    private void validateOutputNotGreaterThanInputPlusIngredients(ProductionPhase productionPhase) {
        // Si output es null o 0, no validar (aún no está definido)
        if (productionPhase.getOutput() == null || productionPhase.getOutput() == 0.0) {
            return;
        }
        
        // Obtener el input (puede ser null o 0 para la primera fase)
        Double input = productionPhase.getInput() != null ? productionPhase.getInput() : 0.0;
        
        // Calcular el total de ingredientes de esta fase
        List<ProductionMaterial> materials = productionMaterialRepository.findAllByProductionPhaseId(productionPhase.getId());
        Double totalIngredients = materials.stream()
                .map(ProductionMaterial::getQuantity)
                .filter(qty -> qty != null && qty > 0)
                .reduce(0.0, Double::sum);
        
        // Calcular el máximo posible: input + ingredientes
        Double maxPossible = input + totalIngredients;
        
        // Validar que output <= input + ingredientes
        if (productionPhase.getOutput() > maxPossible) {
            throw new BadRequestException(
                String.format("El output de la fase %s (%.2f) no puede ser mayor que el input (%.2f) más los ingredientes (%.2f) = %.2f. " +
                        "El output puede ser menor o igual debido a posibles mermas.",
                    productionPhase.getPhase(), productionPhase.getOutput(), input, totalIngredients, maxPossible));
        }
    }
}
