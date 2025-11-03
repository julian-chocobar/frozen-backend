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

    @Test
    void testSetPhaseOrder_prePersist() {
        // Given: ProductPhase con fase asignada pero sin phaseOrder
        ProductPhase phase = ProductPhase.builder()
                .phase(Phase.MOLIENDA)
                .input(100.0)
                .output(95.0)
                .estimatedHours(2.0)
                .isReady(false)
                .creationDate(OffsetDateTime.now())
                .build();

        assertNull(phase.getPhaseOrder());

        // When: Se ejecuta el callback @PrePersist
        phase.setPhaseOrder();

        // Then: phaseOrder debe tener el valor del orden de la fase
        assertNotNull(phase.getPhaseOrder());
        assertEquals(Phase.MOLIENDA.getOrder(), phase.getPhaseOrder());
        assertEquals(1, phase.getPhaseOrder());
    }

    @Test
    void testSetPhaseOrder_withDifferentPhases() {
        // Test con diferentes fases
        ProductPhase molienda = ProductPhase.builder().phase(Phase.MOLIENDA).build();
        molienda.setPhaseOrder();
        assertEquals(1, molienda.getPhaseOrder());

        ProductPhase fermentacion = ProductPhase.builder().phase(Phase.FERMENTACION).build();
        fermentacion.setPhaseOrder();
        assertEquals(5, fermentacion.getPhaseOrder());

        ProductPhase envasado = ProductPhase.builder().phase(Phase.ENVASADO).build();
        envasado.setPhaseOrder();
        assertEquals(9, envasado.getPhaseOrder());
    }

    @Test
    void testSetPhaseOrder_whenPhaseIsNull() {
        // Given: ProductPhase sin fase asignada
        ProductPhase phase = ProductPhase.builder()
                .input(100.0)
                .output(95.0)
                .build();

        // When: Se ejecuta el callback
        phase.setPhaseOrder();

        // Then: phaseOrder debe seguir siendo null
        assertNull(phase.getPhaseOrder());
    }

    @Test
    void testSetPhaseOrder_preUpdate() {
        // Given: ProductPhase existente que cambia de fase
        ProductPhase phase = ProductPhase.builder()
                .phase(Phase.MOLIENDA)
                .phaseOrder(1)
                .input(100.0)
                .output(95.0)
                .build();

        // When: Se cambia la fase y se ejecuta @PreUpdate
        phase.setPhase(Phase.ENVASADO);
        phase.setPhaseOrder();

        // Then: phaseOrder debe actualizarse al nuevo orden
        assertEquals(Phase.ENVASADO.getOrder(), phase.getPhaseOrder());
        assertEquals(9, phase.getPhaseOrder());
    }
}
