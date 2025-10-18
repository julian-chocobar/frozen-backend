package com.enigcode.frozen_backend.batches.service;

import com.enigcode.frozen_backend.batches.DTO.BatchResponseDTO;
import com.enigcode.frozen_backend.batches.mapper.BatchMapper;
import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.model.BatchStatus;
import com.enigcode.frozen_backend.batches.repository.BatchRepository;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.packagings.repository.PackagingRepository;

import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import com.enigcode.frozen_backend.products.model.Product;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService{

    final BatchRepository batchRepository;
    final BatchMapper batchMapper;
    final PackagingRepository packagingRepository;

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

        OffsetDateTime estimatedEndDate = estimateEndDate(product);

        Batch batch = Batch.builder()
                        .packaging(packaging)
                        .status(BatchStatus.PENDIENTE)
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
     * Funcion que estima la fecha de finalizacion de un producto teniendo en cuenta el trabajo en la pyme
     * TODO: CUANDO SE TENGAN LAS AREAS SE DEBERA CALCULAR EN BASE A LA CANTIDAD DE HORAS QUE TRABAJAN LAS DISTINTAS AREAS
     * @param product
     * @return
     */
    private OffsetDateTime estimateEndDate(Product product) {
        return null;
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
}
