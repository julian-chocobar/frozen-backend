package com.enigcode.frozen_backend.materials.mapper;

import com.enigcode.frozen_backend.materials.DTO.MaterialCreateDTO;
import com.enigcode.frozen_backend.materials.DTO.MaterialResponseDTO;
import com.enigcode.frozen_backend.materials.DTO.MaterialDetailDTO;
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
        material.setReservedStock(0.0);
        material.setThreshold(10.0);

        MaterialResponseDTO dto = mapper.toResponseDto(material);

        assertTrue(dto.getIsBelowThreshold(), "Debe ser true cuando stock <= threshold");
    }

    @Test
    void testToResponseDto_isBelowThresholdFalse() {
        Material material = new Material();
        material.setStock(15.0);
        material.setReservedStock(0.0);
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

    @Test
    void testToDetailDto_isBelowThresholdTrue() {
        Material material = new Material();
        material.setStock(3.0);
        material.setReservedStock(0.0);
        material.setThreshold(3.0);

        MaterialDetailDTO dto = mapper.toDetailDto(material);

        assertTrue(dto.getIsBelowThreshold(), "Debe ser true cuando stock <= threshold");
    }

    @Test
    void testPartialUpdate_IgnoresNullsAndUpdatesNonNulls() {
        Material existing = new Material();
        existing.setName("Lúpulo");
        existing.setStock(10.0);
        existing.setReservedStock(0.0);
        existing.setThreshold(2.0);

        MaterialUpdateDTO update = new MaterialUpdateDTO();
        update.setName(null); // debe ignorar
        update.setThreshold(5.0); // debe actualizar

        Material result = mapper.partialUpdate(update, existing);

        assertEquals("Lúpulo", result.getName()); // no cambia
        assertEquals(10.0, result.getStock()); // no cambia
        assertEquals(5.0, result.getThreshold()); // actualizado
    }
}
