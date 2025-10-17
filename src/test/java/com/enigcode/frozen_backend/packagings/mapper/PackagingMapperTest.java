package com.enigcode.frozen_backend.packagings.mapper;

import com.enigcode.frozen_backend.packagings.DTO.PackagingCreateDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingResponseDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingSimpleResponseDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingUpdateDTO;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class PackagingMapperTest {

    private final PackagingMapper mapper = Mappers.getMapper(PackagingMapper.class);

    @Test
    void testToEntity() {
        PackagingCreateDTO dto = new PackagingCreateDTO();
        dto.setName("Caja grande");
        dto.setQuantity(50.0);

        Packaging entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals("Caja grande", entity.getName());
        assertEquals(50, entity.getQuantity());
    }

    @Test
    void testToResponseDto() {
        Packaging packaging = new Packaging();
        packaging.setId(1L);
        packaging.setName("Bolsa plástica");
        packaging.setQuantity(10.0);

        PackagingResponseDTO dto = mapper.toResponseDto(packaging);

        assertNotNull(dto);
        assertEquals("Bolsa plástica", dto.getName());
        assertEquals(10, dto.getQuantity());
    }

    @Test
    void testToSimpleResponseDTO() {
        Packaging packaging = new Packaging();
        packaging.setId(2L);
        packaging.setName("Caja chica");

        PackagingSimpleResponseDTO dto = mapper.toSimpleResponseDTO(packaging);

        assertNotNull(dto);
        assertEquals(2L, dto.getId());
        assertEquals("Caja chica", dto.getName());
    }

    @Test
    void testPartialUpdate() {
        Packaging packaging = new Packaging();
        packaging.setName("Caja vieja");
        packaging.setQuantity(30.0);

        PackagingUpdateDTO updateDTO = new PackagingUpdateDTO();
        updateDTO.setQuantity(40.0); // solo modificamos capacidad

        Packaging updated = mapper.partialUpdate(updateDTO, packaging);

        assertEquals("Caja vieja", updated.getName()); // nombre no cambia
        assertEquals(40, updated.getQuantity()); // capacidad actualizada
    }
}
