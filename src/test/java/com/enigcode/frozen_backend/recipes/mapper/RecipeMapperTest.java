package com.enigcode.frozen_backend.recipes.mapper;

import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.recipes.DTO.RecipeCreateDTO;
import com.enigcode.frozen_backend.recipes.DTO.RecipeResponseDTO;
import com.enigcode.frozen_backend.recipes.model.Recipe;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class RecipeMapperTest {
    private final RecipeMapper mapper = Mappers.getMapper(RecipeMapper.class);

    @Test
    void testToEntity() {
        RecipeCreateDTO dto = RecipeCreateDTO.builder()
                .productPhaseId(1L)
                .materialId(2L)
                .quantity(5.0)
                .build();
        // No productPhase/material set here, just check quantity mapping
        Recipe entity = mapper.toEntity(dto);
        assertNotNull(entity);
        assertEquals(5, entity.getQuantity());
    }

    @Test
    void testToResponseDTO_mapsMaterialFields() {
        Material material = Material.builder()
                .id(2L)
                .name("Malta")
                .code("M001")
                .unitMeasurement(UnitMeasurement.KG)
                .build();
        Recipe recipe = Recipe.builder()
                .id(10L)
                .material(material)
                .quantity(7)
                .build();
        RecipeResponseDTO dto = mapper.toResponseDTO(recipe);
        assertNotNull(dto);
        assertEquals("Malta", dto.getMaterialName());
        assertEquals("M001", dto.getMaterialCode());
        assertEquals("KG", dto.getMaterialUnit());
        assertEquals(7, dto.getQuantity());
    }
}
