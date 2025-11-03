package com.enigcode.frozen_backend.sectors.mapper;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.sectors.DTO.SectorCreateDTO;
import com.enigcode.frozen_backend.sectors.DTO.SectorResponseDTO;
import com.enigcode.frozen_backend.sectors.model.Sector;
import com.enigcode.frozen_backend.sectors.model.SectorType;
import com.enigcode.frozen_backend.users.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class SectorMapperTest {

    private final SectorMapper mapper = Mappers.getMapper(SectorMapper.class);

    @Test
    void testToEntity_fromCreateDTO() {
        SectorCreateDTO createDTO = new SectorCreateDTO();
        createDTO.setName("Sector Molienda");
        createDTO.setSupervisorId(1L);
        createDTO.setType(SectorType.PRODUCCION);
        createDTO.setPhase(Phase.MOLIENDA);
        createDTO.setProductionCapacity(500.0);
        createDTO.setIsTimeActive(true);

        Sector sector = mapper.toEntity(createDTO);

        assertEquals("Sector Molienda", sector.getName());
        assertEquals(SectorType.PRODUCCION, sector.getType());
        assertEquals(Phase.MOLIENDA, sector.getPhase());
        assertEquals(500.0, sector.getProductionCapacity());
        assertTrue(sector.getIsTimeActive());
    }

    @Test
    void testToResponseDTO_mapsSupervisorIdCorrectly() {
        User supervisor = new User();
        supervisor.setId(5L);
        supervisor.setName("Test Supervisor");

        Sector sector = new Sector();
        sector.setId(1L);
        sector.setName("Sector Almacén");
        sector.setType(SectorType.ALMACEN);
        sector.setSupervisor(supervisor);

        SectorResponseDTO responseDTO = mapper.toResponseDTO(sector);

        assertEquals("Sector Almacén", responseDTO.getName());
        assertEquals(SectorType.ALMACEN, responseDTO.getType());
        assertEquals(5L, responseDTO.getSupervisorId());
    }

    @Test
    void testToResponseDTO_withProduccionFields() {
        User supervisor = new User();
        supervisor.setId(3L);

        Sector sector = new Sector();
        sector.setId(2L);
        sector.setName("Sector Fermentación");
        sector.setType(SectorType.PRODUCCION);
        sector.setPhase(Phase.FERMENTACION);
        sector.setProductionCapacity(1000.0);
        sector.setIsTimeActive(false);
        sector.setSupervisor(supervisor);

        SectorResponseDTO responseDTO = mapper.toResponseDTO(sector);

        assertEquals("Sector Fermentación", responseDTO.getName());
        assertEquals(SectorType.PRODUCCION, responseDTO.getType());
        assertEquals(Phase.FERMENTACION, responseDTO.getPhase());
        assertEquals(1000.0, responseDTO.getProductionCapacity());
        assertFalse(responseDTO.getIsTimeActive());
        assertEquals(3L, responseDTO.getSupervisorId());
    }

    
}
