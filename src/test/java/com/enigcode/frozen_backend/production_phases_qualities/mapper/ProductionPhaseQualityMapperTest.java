package com.enigcode.frozen_backend.production_phases_qualities.mapper;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import com.enigcode.frozen_backend.production_phases_qualities.DTO.ProductionPhaseQualityCreateDTO;
import com.enigcode.frozen_backend.production_phases_qualities.DTO.ProductionPhaseQualityResponseDTO;
import com.enigcode.frozen_backend.production_phases_qualities.DTO.ProductionPhaseQualityUpdateDTO;
import com.enigcode.frozen_backend.production_phases_qualities.model.ProductionPhaseQuality;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProductionPhaseQualityMapperTest {

    private final ProductionPhaseQualityMapper mapper = Mappers.getMapper(ProductionPhaseQualityMapper.class);

    @Test
    void toEntity_fromCreateDTO_mapsSimpleFields() {
        ProductionPhaseQualityCreateDTO dto = ProductionPhaseQualityCreateDTO.builder()
                .qualityParameterId(10L)
                .productionPhaseId(20L)
                .value("5.2 pH")
                .isApproved(true)
                .build();

        ProductionPhaseQuality entity = mapper.toEntity(dto);
        assertNotNull(entity);
        assertEquals("5.2 pH", entity.getValue());
        assertTrue(entity.getIsApproved());
        // Relaciones (qualityParameter, productionPhase) se asignan en el servicio, no en el mapper.
        assertNull(entity.getQualityParameter());
        assertNull(entity.getProductionPhase());
    }

    @Test
    void toResponseDTO_mapsNestedFields() {
        ProductionPhase productionPhase = ProductionPhase.builder()
                .id(99L)
                .phase(Phase.MOLIENDA)
                .build();

        ProductionPhaseQuality entity = ProductionPhaseQuality.builder()
                .id(1L)
                .productionPhase(productionPhase)
                .value("OK")
                .isApproved(false)
                .realizationDate(OffsetDateTime.now())
                .build();
        // qualityParameter.name es mapeado, simulamos nombre via mock de objeto mínimo
        var qualityParameter = new com.enigcode.frozen_backend.quality_parameters.model.QualityParameter();
        qualityParameter.setName("Temperatura");
        entity.setQualityParameter(qualityParameter);

        ProductionPhaseQualityResponseDTO dto = mapper.toResponseDTO(entity);
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Temperatura", dto.getQualityParameterName());
        assertEquals(99L, dto.getProductionPhaseId());
        assertEquals(Phase.MOLIENDA, dto.getProductionPhase());
        assertEquals("OK", dto.getValue());
        assertFalse(dto.getIsApproved());
        assertNotNull(dto.getRealizationDate());
    }

    @Test
    void partialUpdate_updatesOnlyNonNullFields() {
        ProductionPhaseQuality existing = ProductionPhaseQuality.builder()
                .id(1L)
                .value("Inicial")
                .isApproved(false)
                .build();

        ProductionPhaseQualityUpdateDTO updateDTO = ProductionPhaseQualityUpdateDTO.builder()
                .value("Actualizado")
                .isApproved(true)
                .build();

        ProductionPhaseQuality updated = mapper.partialUpdate(updateDTO, existing);

        assertEquals("Actualizado", updated.getValue());
        assertTrue(updated.getIsApproved());
    }

    @Test
    void partialUpdate_nullFields_doNotOverwrite() {
        ProductionPhaseQuality existing = ProductionPhaseQuality.builder()
                .id(1L)
                .value("Mantener")
                .isApproved(true)
                .build();

        ProductionPhaseQualityUpdateDTO updateDTO = ProductionPhaseQualityUpdateDTO.builder()
                .value(null)
                .isApproved(null)
                .build();

        ProductionPhaseQuality updated = mapper.partialUpdate(updateDTO, existing);
        assertEquals("Mantener", updated.getValue());
        assertTrue(updated.getIsApproved());
    }
}
