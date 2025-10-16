package com.enigcode.frozen_backend.materials.mapper;

import com.enigcode.frozen_backend.materials.DTO.MaterialCreateDTO;
import com.enigcode.frozen_backend.materials.DTO.MaterialResponseDTO;
import com.enigcode.frozen_backend.materials.DTO.MaterialUpdateDTO;
import com.enigcode.frozen_backend.materials.model.Material;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class MaterialMapperTest {

    private final MaterialMapper mapper = Mappers.getMapper(MaterialMapper.class);

    @Test
    void testToResponseDto_isBelowThresholdTrue() {
        Material material = new Material();
        material.setStock(5.0);
        material.setThreshold(10.0);

        MaterialResponseDTO dto = mapper.toResponseDto(material);

        assertTrue(dto.getIsBelowThreshold(), "Debe ser true cuando stock <= threshold");
    }

    @Test
    void testToResponseDto_isBelowThresholdFalse() {
        Material material = new Material();
        material.setStock(15.0);
        material.setThreshold(10.0);

        MaterialResponseDTO dto = mapper.toResponseDto(material);

        assertFalse(dto.getIsBelowThreshold(), "Debe ser false cuando stock > threshold");
    }

    @Test
    void testToEntityFromCreateDTO() {
        MaterialCreateDTO createDTO = new MaterialCreateDTO();
        createDTO.setName("Malta");
        createDTO.setStock(20.0);
        createDTO.setThreshold(5.0);
        // no seteamos type, unitMeasurement, etc. — solo lo básico

        Material material = mapper.toEntity(createDTO);

        assertEquals("Malta", material.getName());
        assertEquals(20.0, material.getStock());
        assertEquals(5.0, material.getThreshold());
    }

    /**
     * No se puede actualizar el stock,
     * se actualiza con la creacion de movimientos
     */

    /**
     * @Test
     *       void testPartialUpdate_ignoresNullValues() {
     *       Material material = new Material();
     *       material.setName("Cebada");
     *       material.setStock(100.0);
     * 
     *       MaterialUpdateDTO updateDTO = new MaterialUpdateDTO();
     *       updateDTO.setStock(120.0);
     *       // no seteamos name → debería mantenerse igual
     *       mapper.partialUpdate(updateDTO, material);
     * 
     *       assertEquals("Cebada", material.getName());
     *       assertEquals(120.0, material.getStock());
     *       }
     */
}
