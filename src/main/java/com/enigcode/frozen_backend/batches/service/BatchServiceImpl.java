package com.enigcode.frozen_backend.batches.service;

import com.enigcode.frozen_backend.batches.DTO.BatchFilterDTO;
import com.enigcode.frozen_backend.batches.DTO.BatchResponseDTO;
import com.enigcode.frozen_backend.batches.mapper.BatchMapper;
import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.model.BatchStatus;
import com.enigcode.frozen_backend.batches.repository.BatchRepository;
import com.enigcode.frozen_backend.batches.specification.BatchSpecification;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.packagings.repository.PackagingRepository;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhaseStatus;
import com.enigcode.frozen_backend.production_phases.repository.ProductionPhaseRepository;
import com.enigcode.frozen_backend.production_phases.service.ProductionPhaseService;
import com.enigcode.frozen_backend.products.model.Product;
import com.enigcode.frozen_backend.sectors.model.Sector;
import com.enigcode.frozen_backend.sectors.service.SectorService;
import com.enigcode.frozen_backend.system_configurations.model.WorkingDay;
import com.enigcode.frozen_backend.system_configurations.service.SystemConfigurationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.enigcode.frozen_backend.common.Utils.DateUtil.estimateEndDate;

@Service
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService {

    private final BatchRepository batchRepository;
    private final BatchMapper batchMapper;
    private final PackagingRepository packagingRepository;
    private final SystemConfigurationService systemConfigurationService;
    private final ProductionPhaseService productionPhaseService;
    private final SectorService sectorService;
    private final ProductionPhaseRepository productionPhaseRepository;

    /**
     * Crea un Lote cuando se crea una orden de produccion, la misma tiene que ser
     * transactional
     * TODO: Cuando se cree el lote se deben crear automaticamente sus fases
     * 
     * @param createDTO
     * @return
     */
    @Override
    @Transactional
    public Batch createBatch(ProductionOrderCreateDTO createDTO, Product product) {
        Packaging packaging = packagingRepository.findById(createDTO.getPackagingId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un paquete con el id " + createDTO.getPackagingId()));

        Integer quantityInteger = (int) Math.floor(createDTO.getQuantity() / packaging.getQuantity());

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

        batchPhases.forEach(phase -> phase.setBatch(batch));

        Batch savedBatch = batchRepository.saveAndFlush(batch);
        savedBatch.setCode(generateCode(savedBatch));
        return savedBatch;
    }

    /**
     * Funcion que genera codigo para lotes
     * 
     * @param batch
     * @return
     */
    private String generateCode(Batch batch) {
        return "LOT-" + batch.getId();
    }

    /**
     * Devuelve un lote con un id especifico
     * 
     * @param id
     * @return
     */
    @Override
    public BatchResponseDTO getBatch(Long id) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró lote con id " + id));

        return batchMapper.toResponseDTO(batch);
    }

    /**
     * Dado un id cancela el lote, suspende las fases no completas y devuelve la
     * materia prima restante
     * 
     * @param id
     * @return
     */
    @Override
    @Transactional
    public BatchResponseDTO cancelBatch(Long id) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró lote con id " + id));

        batch.setStatus(BatchStatus.CANCELADO);

        List<ProductionPhase> remainingProductionPhases = batch.getPhases().stream()
                .filter(productionPhase -> productionPhase.getStatus().equals(ProductionPhaseStatus.PENDIENTE))
                .toList();

        productionPhaseService.suspendProductionPhases(remainingProductionPhases);

        Batch savedBatch = batchRepository.save(batch);
        return batchMapper.toResponseDTO(savedBatch);
    }

    /**
     * Dado un lote, suspende las fases no completas y devuelve la materia prima
     * restante
     *
     * @param id
     */
    @Transactional
    @Override
    public void cancelBatch(Batch batch) {
        batch.setStatus(BatchStatus.CANCELADO);

        List<ProductionPhase> remainingProductionPhases = batch.getPhases().stream()
                .filter(productionPhase -> productionPhase.getStatus().equals(ProductionPhaseStatus.PENDIENTE))
                .toList();

        productionPhaseService.suspendProductionPhases(remainingProductionPhases);

        Batch savedBatch = batchRepository.save(batch);
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

    /**
     * Automatizacion de inicializacion de lotes de la fecha programado para las 8
     * am
     * En caso de estar llena la producción se postergan hacia el proximo dia
     * TODO: AGREGAR TAMBIEN EN ESPERA EN CASO DE SER POSTERGADO Y AÑADIRLO A LA
     * CREACION?
     */
    @Scheduled(cron = "0 0 8 * * *", zone = "America/Argentina/Buenos_Aires")
    @Transactional
    @Override
    public void processBatchesForToday() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime startOfDay = now.toLocalDate().atStartOfDay().atOffset(now.getOffset());
        OffsetDateTime endOfDay = startOfDay.plusDays(1);

        List<Batch> scheduledBatches = batchRepository.findAllStartingToday(startOfDay, endOfDay);

        if (scheduledBatches.isEmpty())
            return;

        // Se fija de que sea un dia laborable para asignar el lote
        boolean isWorkingDay = systemConfigurationService.getWorkingDays().get(now.getDayOfWeek()).getIsWorkingDay();
        if (isWorkingDay)
            processAllBatches(scheduledBatches);

        List<Batch> remainingBatches = scheduledBatches.stream()
                .filter(batch -> batch.getStatus().equals(BatchStatus.PENDIENTE))
                .toList();

        postPoneBatches(remainingBatches);

        batchRepository.saveAll(scheduledBatches);
    }

    /**
     * Agarra los lotes que estan pendientes que se deberian haber inicializado el
     * dia de la fecha y los aplaza 1 dia
     * 
     * @param remainingBatches
     */
    private void postPoneBatches(List<Batch> remainingBatches) {
        if (remainingBatches.isEmpty())
            return;

        OffsetDateTime tomorrowStart = OffsetDateTime.now()
                .toLocalDate()
                .plusDays(1)
                .atStartOfDay()
                .atOffset(OffsetDateTime.now().getOffset());

        remainingBatches.forEach(batch -> batch.setStartDate(tomorrowStart));
    }

    /**
     * Recibe los lotes que se deberian iniciar en la fecha y uno a uno los
     * inicializa asignandole un sector hasta
     * que se termine la lista o el limite de produccion sea alcanzado
     * 
     * @param scheduledBatches
     * @return
     */
    private void processAllBatches(List<Batch> scheduledBatches) {
        List<Sector> sectors = sectorService.getAllSectorsAvailableByPhase(Phase.MOLIENDA);
        if (sectors.isEmpty())
            return;

        for (Batch batch : scheduledBatches) {
            Double batchQuantity = batch.getProductionOrder().getQuantity();

            sectors.stream()
                    .filter(sector -> (sector.getActualProduction() + batchQuantity) <= sector.getProductionCapacity())
                    .findFirst()
                    .ifPresent(sector -> {
                        startBatch(batch, sector);
                        sector.increaseActualProduction(batchQuantity);
                    });
        }

        sectorService.saveAll(sectors);
    }

    // TODO: ENVIAR NOTIFICACION AL SUPERVISOR DEL SECTOR
    private void startBatch(Batch batch, Sector sector) {
        if (batch.getPhases() == null || batch.getPhases().isEmpty())
            return;
        ProductionPhase firstPhase = batch.getPhases().get(0);
        batch.setStatus(BatchStatus.EN_PRODUCCION);
        batch.setStartDate(OffsetDateTime.now());
        firstPhase.setSector(sector);
        firstPhase.setStatus(ProductionPhaseStatus.EN_PROCESO);
        firstPhase.setStartDate(OffsetDateTime.now());
    }

    /**
     * Funcion que agarra la proxima fase y la pone en produccion o termina el lote
     * en caso de ser la ultima fase
     * TODO: NOTIFICACION AL SUPERVISOR DEL SECTOR ENCARGADO DE LA PROXIMA FASE
     * 
     * @param batch
     */
    @Override
    @Transactional
    public void startNextPhase(Batch batch) {
        Optional<ProductionPhase> nextProductionPhase = batch.getPhases().stream()
                .filter(productionPhase -> productionPhase.getStatus()
                        .equals(ProductionPhaseStatus.PENDIENTE))
                .findFirst();

        if (nextProductionPhase.isEmpty()) {
            boolean isComplete = batch.getPhases().stream()
                    .allMatch(productionPhase -> productionPhase.getStatus()
                            .equals(ProductionPhaseStatus.COMPLETADA));
            if (isComplete)
                completeBatch(batch);
        }
        ;

        ProductionPhase productionPhase = nextProductionPhase.get();
        List<Sector> sectors = sectorService.getAllSectorsAvailableByPhase(productionPhase.getPhase());
        if (sectors.isEmpty())
            throw new BadRequestException("Falta sector para la fase " + productionPhase.getPhase());

        productionPhase.setStatus(ProductionPhaseStatus.EN_PROCESO);
        productionPhase.setSector(sectors.get(0));
        productionPhase.setStartDate(OffsetDateTime.now());

        productionPhaseRepository.save(productionPhase);
    }

    private void completeBatch(Batch batch) {
        batch.setStatus(BatchStatus.COMPLETADO);
        batch.setCompletedDate(OffsetDateTime.now());

        batchRepository.save(batch);
    }
}
