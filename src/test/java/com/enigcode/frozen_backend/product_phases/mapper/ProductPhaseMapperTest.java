package com.enigcode.frozen_backend.product_phases.mapper;

import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseResponseDTO;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseUpdateDTO;
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
        phase.setName("Congelado");
        phase.setDescription("Fase de congelado");
        phase.setActive(true);

        ProductPhaseResponseDTO dto = mapper.toResponseDto(phase);

        assertNotNull(dto);
        assertEquals(phase.getId(), dto.getId());
        assertEquals(phase.getName(), dto.getName());
        assertEquals(phase.getDescription(), dto.getDescription());
    }

    @Test
    void testPartialUpdate() {
        ProductPhase existing = new ProductPhase();
        existing.setId(1L);
        existing.setName("Empaque");
        existing.setDescription("Fase inicial");

        ProductPhaseUpdateDTO updateDTO = new ProductPhaseUpdateDTO();
        updateDTO.setDescription("Fase de empaquetado actualizada");

        mapper.partialUpdate(updateDTO, existing);

        assertEquals("Empaque", existing.getName());
        assertEquals("Fase de empaquetado actualizada", existing.getDescription());
    }
}
