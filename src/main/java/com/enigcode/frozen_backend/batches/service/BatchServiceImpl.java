package com.enigcode.frozen_backend.batches.service;

import com.enigcode.frozen_backend.batches.DTO.BatchFilterDTO;
import com.enigcode.frozen_backend.batches.DTO.BatchResponseDTO;
import com.enigcode.frozen_backend.batches.mapper.BatchMapper;
import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.model.BatchStatus;
import com.enigcode.frozen_backend.batches.repository.BatchRepository;
import com.enigcode.frozen_backend.batches.specification.BatchSpecification;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.packagings.repository.PackagingRepository;

import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhaseStatus;
import com.enigcode.frozen_backend.production_phases.repository.ProductionPhaseRepository;
import com.enigcode.frozen_backend.production_phases.service.ProductionPhaseService;
import com.enigcode.frozen_backend.products.model.Product;
import com.enigcode.frozen_backend.system_configurations.model.WorkingDay;
import com.enigcode.frozen_backend.system_configurations.service.SystemConfigurationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.enigcode.frozen_backend.common.Utils.DateUtil.estimateEndDate;

@Service
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService{

    final BatchRepository batchRepository;
    final BatchMapper batchMapper;
    final PackagingRepository packagingRepository;
    final SystemConfigurationService systemConfigurationService;
    private final ProductionPhaseService productionPhaseService;

    /**
     * Crea un Lote cuando se crea una orden de produccion, la misma tiene que ser transactional
     * TODO: Cuando se cree el lote se deben crear automaticamente sus fases
     * @param createDTO
     * @return
     */
    @Override
    @Transactional
    public Batch createBatch(ProductionOrderCreateDTO createDTO, Product product) {
        Packaging packaging = packagingRepository.findById(createDTO.getPackagingId())
            .orElseThrow(() -> new ResourceNotFoundException(
                    "No se encontró un paquete con el id " + createDTO.getPackagingId()));

        Integer quantityInteger = (int) Math.floor(createDTO.getQuantity()/packaging.getQuantity());

        Map<DayOfWeek, WorkingDay> workingDays = systemConfigurationService.getWorkingDays();

        OffsetDateTime estimatedEndDate = estimateEndDate(product, createDTO.getPlannedDate(), workingDays);

        List<ProductionPhase> batchPhases = product.getPhases().stream().map(productPhase -> {
            return ProductionPhase.builder()
                    .status(ProductionPhaseStatus.PENDIENTE)
                    .phase(productPhase.getPhase())
                    .standardInput(productPhase.getInput())
                    .standardOutput(productPhase.getOutput())
                    .outputUnit(productPhase.getOutputUnit())
                    .build();
        }).toList();

        Batch batch = Batch.builder()
                .packaging(packaging)
                .status(BatchStatus.PENDIENTE)
                .phases(batchPhases)
                .quantity(quantityInteger)
                .plannedDate(createDTO.getPlannedDate())
                .creationDate(OffsetDateTime.now())
                .startDate(null)
                .completedDate(null)
                .estimatedCompletedDate(estimatedEndDate)
                .build();

        Batch savedBatch = batchRepository.saveAndFlush(batch);
        savedBatch.setCode(generateCode(savedBatch));
        return savedBatch;
    }

    /**
     * Funcion que genera codigo para lotes
     * @param batch
     * @return
     */
    private String generateCode(Batch batch) {
        return "LOT-" + batch.getId();
    }

    /**
     * Devuelve un lote con un id especifico
     * @param id
     * @return
     */
    @Override
    public BatchResponseDTO getBatch(Long id) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("No se encontró lote con id " + id));

        return batchMapper.toResponseDTO(batch);
    }

    /**
     * Dado un id cancela el lote, suspende las fases no completas y devuelve la materia prima restante
     * @param id
     * @return
     */
    @Override
    @Transactional
    public BatchResponseDTO cancelBatch(Long id) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("No se encontró lote con id " + id));

        batch.setStatus(BatchStatus.CANCELADO);

        List<ProductionPhase> remainingProductionPhases = batch.getPhases().stream()
                .filter(productionPhase ->
                        productionPhase.getStatus().equals(ProductionPhaseStatus.PENDIENTE))
                .toList();

        productionPhaseService.suspendProductionPhases(remainingProductionPhases);

        Batch savedBatch = batchRepository.save(batch);
        return batchMapper.toResponseDTO(savedBatch);
    }

    @Override
    public Page<BatchResponseDTO> findAll(BatchFilterDTO filterDTO, Pageable pageable) {
        Pageable pageRequest = PageRequest.of(
                                pageable.getPageNumber(),
                                pageable.getPageSize(),
                                pageable.getSort());
        Page<Batch> batches = batchRepository.findAll(BatchSpecification.createFilter(filterDTO), pageRequest);
        return batches.map(batchMapper::toResponseDTO);
    }
}
