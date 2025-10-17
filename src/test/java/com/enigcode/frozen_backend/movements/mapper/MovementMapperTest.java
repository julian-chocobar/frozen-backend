package com.enigcode.frozen_backend.movements.mapper;

import com.enigcode.frozen_backend.movements.DTO.MovementDetailDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementResponseDTO;
import com.enigcode.frozen_backend.movements.model.Movement;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class MovementMapperTest {

    private final MovementMapper mapper = Mappers.getMapper(MovementMapper.class);

    @Test
    void testToResponseDto() {
        Material material = new Material();
        material.setType(MaterialType.MALTA);

        Movement movement = new Movement();
        movement.setMaterial(material);

        MovementResponseDTO dto = mapper.toResponseDto(movement);

        assertEquals(MaterialType.MALTA, dto.getMaterialType());
    }

    @Test
    void testToDetailDTO() {
        Material material = new Material();
        material.setId(1L);
        material.setName("Malta");
        material.setCode("M001");
        material.setType(MaterialType.MALTA);

        Movement movement = new Movement();
        movement.setMaterial(material);

        MovementDetailDTO dto = mapper.toDetailDTO(movement);

        assertEquals(1L, dto.getMaterialId());
        assertEquals("Malta", dto.getMaterialName());
        assertEquals("M001", dto.getMaterialCode());
        assertEquals(MaterialType.MALTA, dto.getMaterialType());
    }
}