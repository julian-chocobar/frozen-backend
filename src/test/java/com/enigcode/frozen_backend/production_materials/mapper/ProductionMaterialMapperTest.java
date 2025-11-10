package com.enigcode.frozen_backend.production_materials.mapper;

import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import com.enigcode.frozen_backend.production_materials.DTO.ProductionMaterialResponseDTO;
import com.enigcode.frozen_backend.production_materials.model.ProductionMaterial;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class ProductionMaterialMapperTest {

    private final ProductionMaterialMapper mapper = Mappers.getMapper(ProductionMaterialMapper.class);

    @Test
    void toResponseDTO_mapsNestedFields() {
        Material material = Material.builder()
                .id(11L)
                .code("MAT-001")
                .build();

        ProductionPhase phase = ProductionPhase.builder()
                .id(22L)
                .build();

        ProductionMaterial entity = ProductionMaterial.builder()
                .id(5L)
                .material(material)
                .productionPhase(phase)
                .quantity(12.5)
                .build();

        ProductionMaterialResponseDTO dto = mapper.toResponseDTO(entity);

        assertNotNull(dto);
        assertEquals(5L, dto.getId());
        assertEquals(11L, dto.getMaterialId());
        assertEquals("MAT-001", dto.getMaterialCode());
        assertEquals(22L, dto.getProductionPhaseId());
        assertEquals(12.5, dto.getQuantity());
    }
}
