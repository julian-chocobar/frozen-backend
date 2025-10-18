package com.enigcode.frozen_backend.product_phases.mapper;

import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseResponseDTO;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseUpdateDTO;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProductPhaseMapperTest {

    private final ProductPhaseMapper mapper = Mappers.getMapper(ProductPhaseMapper.class);

    @Test
    void testToResponseDto() {
        ProductPhase phase = ProductPhase.builder()
                .id(1L)
                .phase(Phase.ENVASADO)
                .input(100.0)
                .output(95.0)
                .outputUnit(UnitMeasurement.LT)
                .estimatedHours(2.5)
                .isReady(false)
                .creationDate(OffsetDateTime.now())
                .build();

        ProductPhaseResponseDTO dto = mapper.toResponseDto(phase);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(Phase.ENVASADO, dto.getPhase());
        assertEquals(100.0, dto.getInput());
        assertEquals(95.0, dto.getOutput());
        assertEquals(UnitMeasurement.LT, dto.getOutputUnit());
        assertEquals(2.5, dto.getEstimatedHours());
    }

    @Test
    void testPartialUpdate() {
        ProductPhase existing = ProductPhase.builder()
                .id(1L)
                .phase(Phase.MOLIENDA)
                .input(50.0)
                .output(45.0)
                .outputUnit(UnitMeasurement.KG)
                .estimatedHours(1.0)
                .isReady(false)
                .creationDate(OffsetDateTime.now())
                .build();

        ProductPhaseUpdateDTO updateDTO = new ProductPhaseUpdateDTO();
        updateDTO.setOutput(48.0);
        updateDTO.setEstimatedHours(1.5);

        mapper.partialUpdate(updateDTO, existing);

        assertEquals(50.0, existing.getInput()); // no cambia
        assertEquals(48.0, existing.getOutput()); // actualizado
        assertEquals(1.5, existing.getEstimatedHours()); // actualizado
        assertEquals(UnitMeasurement.KG, existing.getOutputUnit()); // no cambia
    }
}
