package com.enigcode.frozen_backend.quality_parameters.mapper;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterCreateDTO;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterResponseDTO;
import com.enigcode.frozen_backend.quality_parameters.model.QualityParameter;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QualityParameterMapperTest {

    private final QualityParameterMapper mapper = Mappers.getMapper(QualityParameterMapper.class);

    @Test
    void testToEntity_fromCreateDTO() {
        QualityParameterCreateDTO createDTO = QualityParameterCreateDTO.builder()
                .phase(Phase.MOLIENDA)
                .isCritical(true)
                .name("pH")
                .description("Medición de acidez")
                .build();

        QualityParameter entity = mapper.toEntity(createDTO);

        assertNotNull(entity);
        assertEquals(Phase.MOLIENDA, entity.getPhase());
        assertTrue(entity.isCritical()); // Lombok getter still isCritical() for primitive
        assertEquals("pH", entity.getName());
        assertEquals("Medición de acidez", entity.getDescription());
    }

    @Test
    void testToEntity_withMinimalData() {
        QualityParameterCreateDTO createDTO = QualityParameterCreateDTO.builder()
                .phase(Phase.FERMENTACION)
                .isCritical(false)
                .name("Temperatura")
                .build();

        QualityParameter entity = mapper.toEntity(createDTO);

        assertNotNull(entity);
        assertEquals(Phase.FERMENTACION, entity.getPhase());
        assertFalse(entity.isCritical());
        assertEquals("Temperatura", entity.getName());
        assertNull(entity.getDescription());
    }

    @Test
    void testToResponseDTO() {
        QualityParameter entity = QualityParameter.builder()
                .id(1L)
                .phase(Phase.COCCION)
                .critical(true)
                .name("Densidad")
                .description("Control de densidad del mosto")
                .active(true)
                .build();

        QualityParameterResponseDTO responseDTO = mapper.toResponseDTO(entity);

        assertNotNull(responseDTO);
        assertEquals(1L, responseDTO.getId());
        assertEquals(Phase.COCCION, responseDTO.getPhase());
                assertNotNull(responseDTO.getIsCritical());
                assertEquals(true, responseDTO.getIsCritical());
        assertEquals("Densidad", responseDTO.getName());
        assertEquals("Control de densidad del mosto", responseDTO.getDescription());
                assertNotNull(responseDTO.getIsActive());
                assertEquals(true, responseDTO.getIsActive());
    }

    @Test
    void testToResponseDTO_withInactiveParameter() {
        QualityParameter entity = QualityParameter.builder()
                .id(2L)
                .phase(Phase.ENVASADO)
                .critical(false)
                .name("Color")
                .description("Inspección visual del color")
                .active(false)
                .build();

        QualityParameterResponseDTO responseDTO = mapper.toResponseDTO(entity);

        assertNotNull(responseDTO);
        assertEquals(2L, responseDTO.getId());
                assertNotNull(responseDTO.getIsActive());
                assertEquals(false, responseDTO.getIsActive());
        assertEquals(Phase.ENVASADO, responseDTO.getPhase());
    }

    @Test
    void testToResponseDTO_handlesNullDescription() {
        QualityParameter entity = QualityParameter.builder()
                .id(3L)
                .phase(Phase.MOLIENDA)
                .critical(true)
                .name("Granulometría")
                .active(true)
                .build();

        QualityParameterResponseDTO responseDTO = mapper.toResponseDTO(entity);

        assertNotNull(responseDTO);
                assertEquals(3L, responseDTO.getId());
        assertNull(responseDTO.getDescription());
        assertEquals("Granulometría", responseDTO.getName());
    }

    @Test
    void testListMapping() {
        QualityParameter param1 = QualityParameter.builder()
                .id(1L)
                .phase(Phase.MOLIENDA)
                .critical(true)
                .name("pH")
                .active(true)
                .build();

        QualityParameter param2 = QualityParameter.builder()
                .id(2L)
                .phase(Phase.FERMENTACION)
                .critical(false)
                .name("Temperatura")
                .active(true)
                .build();

        List<QualityParameter> entities = Arrays.asList(param1, param2);
        List<QualityParameterResponseDTO> responseDTOs = entities.stream()
                .map(mapper::toResponseDTO)
                .toList();

        assertNotNull(responseDTOs);
        assertEquals(2, responseDTOs.size());
        assertEquals("pH", responseDTOs.get(0).getName());
        assertEquals("Temperatura", responseDTOs.get(1).getName());
    }

    @Test
    void testMapping_preservesCriticalFlag() {
        QualityParameterCreateDTO createDTO = QualityParameterCreateDTO.builder()
                .phase(Phase.MOLIENDA)
                .isCritical(true)
                .name("Test Critical")
                .build();

        QualityParameter entity = mapper.toEntity(createDTO);
        assertTrue(entity.isCritical(), "El flag critical debe mantenerse como true");

        QualityParameterCreateDTO createDTO2 = QualityParameterCreateDTO.builder()
                .phase(Phase.MOLIENDA)
                .isCritical(false)
                .name("Test Non-Critical")
                .build();

        QualityParameter entity2 = mapper.toEntity(createDTO2);
        assertFalse(entity2.isCritical(), "El flag critical debe mantenerse como false");
    }
}
