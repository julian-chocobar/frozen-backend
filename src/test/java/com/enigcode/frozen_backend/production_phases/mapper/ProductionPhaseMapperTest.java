package com.enigcode.frozen_backend.production_phases.mapper;

import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.production_phases.DTO.ProductionPhaseResponseDTO;
import com.enigcode.frozen_backend.production_phases.DTO.ProductionPhaseUnderReviewDTO;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhaseStatus;
import com.enigcode.frozen_backend.sectors.model.Sector;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class ProductionPhaseMapperTest {

    private final ProductionPhaseMapper mapper = Mappers.getMapper(ProductionPhaseMapper.class);

    @Test
    void testToResponseDTO_MapsAllFields() {
        Batch batch = Batch.builder()
                .id(1L)
                .code("BATCH-001")
                .build();

        Sector sector = Sector.builder()
                .id(2L)
                .build();

        ProductionPhase productionPhase = ProductionPhase.builder()
                .id(1L)
                .batch(batch)
                .sector(sector)
                .phase(Phase.MOLIENDA)
                .status(ProductionPhaseStatus.EN_PROCESO)
                .input(100.0)
                .output(95.0)
                .standardInput(100.0)
                .standardOutput(98.0)
                .build();

        ProductionPhaseResponseDTO dto = mapper.toResponseDTO(productionPhase);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(1L, dto.getBatchId());
        assertEquals("BATCH-001", dto.getBatchCode());
        assertEquals(2L, dto.getSectorId());
        assertEquals(Phase.MOLIENDA, dto.getPhase());
        assertEquals(ProductionPhaseStatus.EN_PROCESO, dto.getStatus());
        assertEquals(100.0, dto.getInput());
        assertEquals(95.0, dto.getOutput());
    }

    @Test
    void testPartialUpdate_UpdatesOnlyProvidedFields() {
        ProductionPhase existingPhase = ProductionPhase.builder()
                .id(1L)
                .phase(Phase.MOLIENDA)
                .status(ProductionPhaseStatus.EN_PROCESO)
                .input(80.0)
                .output(75.0)
                .build();

        ProductionPhaseUnderReviewDTO updateDTO = ProductionPhaseUnderReviewDTO.builder()
                .input(100.0)
                .output(95.0)
                .build();

        ProductionPhase result = mapper.partialUpdate(updateDTO, existingPhase);

        assertNotNull(result);
        assertEquals(100.0, result.getInput());
        assertEquals(95.0, result.getOutput());
        // Campos no actualizados se preservan
        assertEquals(1L, result.getId());
        assertEquals(Phase.MOLIENDA, result.getPhase());
        assertEquals(ProductionPhaseStatus.EN_PROCESO, result.getStatus());
    }

    @Test
    void testToResponseDTO_WithNullBatch() {
        ProductionPhase productionPhase = ProductionPhase.builder()
                .id(1L)
                .phase(Phase.FERMENTACION)
                .status(ProductionPhaseStatus.PENDIENTE)
                .build();

        ProductionPhaseResponseDTO dto = mapper.toResponseDTO(productionPhase);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertNull(dto.getBatchId());
        assertNull(dto.getBatchCode());
    }

    @Test
    void testToResponseDTO_WithNullSector() {
        ProductionPhase productionPhase = ProductionPhase.builder()
                .id(1L)
                .phase(Phase.COCCION)
                .status(ProductionPhaseStatus.COMPLETADA)
                .build();

        ProductionPhaseResponseDTO dto = mapper.toResponseDTO(productionPhase);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertNull(dto.getSectorId());
    }
}
