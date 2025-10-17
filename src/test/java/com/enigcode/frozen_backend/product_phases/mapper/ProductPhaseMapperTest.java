package com.enigcode.frozen_backend.product_phases.mapper;

import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseResponseDTO;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseUpdateDTO;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class ProductPhaseMapperTest {

    private final ProductPhaseMapper mapper = Mappers.getMapper(ProductPhaseMapper.class);

    @Test
    void testToResponseDto() {
        ProductPhase phase = new ProductPhase();
        phase.setId(1L);
        phase.setPhase(Phase.MOLIENDA);

        ProductPhaseResponseDTO dto = mapper.toResponseDto(phase);

        assertNotNull(dto);
        assertEquals(phase.getId(), dto.getId());
        assertEquals(phase.getPhase(), dto.getPhase());
    }

    @Test
    void testPartialUpdate() {
        ProductPhase phase = new ProductPhase();
        phase.setId(1L);
        phase.setPhase(Phase.MOLIENDA);
        phase.setInput(100.0);
        phase.setOutput(90.0);

        ProductPhaseUpdateDTO updateDTO = new ProductPhaseUpdateDTO();
        updateDTO.setInput(120.0); // Only updating input

        mapper.partialUpdate(updateDTO, phase);

        assertNotNull(phase);
        assertEquals(1L, phase.getId());
        assertEquals(Phase.MOLIENDA, phase.getPhase());
        assertEquals(120.0, phase.getInput()); // Updated
        assertEquals(90.0, phase.getOutput()); // Unchanged
    }
}
