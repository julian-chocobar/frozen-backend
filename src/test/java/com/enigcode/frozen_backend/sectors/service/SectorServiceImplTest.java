package com.enigcode.frozen_backend.sectors.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.sectors.DTO.SectorCreateDTO;
import com.enigcode.frozen_backend.sectors.DTO.SectorResponseDTO;
import com.enigcode.frozen_backend.sectors.DTO.SectorUpdateDTO;
import com.enigcode.frozen_backend.sectors.mapper.SectorMapper;
import com.enigcode.frozen_backend.sectors.model.Sector;
import com.enigcode.frozen_backend.sectors.model.SectorType;
import com.enigcode.frozen_backend.sectors.repository.SectorRepository;
import com.enigcode.frozen_backend.users.model.Role;
import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SectorServiceImplTest {

    @Mock
    private SectorRepository sectorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SectorMapper sectorMapper;

    @InjectMocks
    private SectorServiceImpl sectorService;

    private Sector sector;
    private SectorResponseDTO responseDTO;
    private User supervisor;

    @BeforeEach
    void setUp() {
        supervisor = new User();
        supervisor.setId(1L);
        supervisor.setUsername("supervisor");
        supervisor.setRoles(Set.of(Role.SUPERVISOR_DE_PRODUCCION));

        sector = new Sector();
        sector.setId(1L);
        sector.setName("Sector Molienda");
        sector.setType(SectorType.PRODUCCION);
        sector.setPhase(Phase.MOLIENDA);
        sector.setProductionCapacity(500.0);
        sector.setIsTimeActive(true);
        sector.setIsActive(true);
        sector.setSupervisor(supervisor);

        responseDTO = new SectorResponseDTO();
        responseDTO.setName("Sector Molienda");
        responseDTO.setSupervisorId(1L);
        responseDTO.setType(SectorType.PRODUCCION);
        responseDTO.setPhase(Phase.MOLIENDA);
        responseDTO.setProductionCapacity(500.0);
        responseDTO.setIsTimeActive(true);
    }

    @Test
    void createSector_withValidProduccionData_success() {
        SectorCreateDTO createDTO = new SectorCreateDTO();
        createDTO.setName("Sector Molienda");
        createDTO.setSupervisorId(1L);
        createDTO.setType(SectorType.PRODUCCION);
        createDTO.setPhase(Phase.MOLIENDA);
        createDTO.setProductionCapacity(500.0);
        createDTO.setIsTimeActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(supervisor));
        when(sectorMapper.toEntity(createDTO)).thenReturn(sector);
        when(sectorRepository.saveAndFlush(any(Sector.class))).thenReturn(sector);
        when(sectorMapper.toResponseDTO(sector)).thenReturn(responseDTO);

        SectorResponseDTO result = sectorService.createSector(createDTO);

        assertNotNull(result);
        assertEquals("Sector Molienda", result.getName());
        assertEquals(SectorType.PRODUCCION, result.getType());
        verify(sectorRepository, times(1)).saveAndFlush(any(Sector.class));
    }

    @Test
    void createSector_withValidAlmacenData_success() {
        User almacenSupervisor = new User();
        almacenSupervisor.setId(2L);
        almacenSupervisor.setRoles(Set.of(Role.SUPERVISOR_DE_ALMACEN));

        SectorCreateDTO createDTO = new SectorCreateDTO();
        createDTO.setName("Almacén Principal");
        createDTO.setSupervisorId(2L);
        createDTO.setType(SectorType.ALMACEN);

        Sector almacenSector = new Sector();
        almacenSector.setName("Almacén Principal");
        almacenSector.setType(SectorType.ALMACEN);
        almacenSector.setSupervisor(almacenSupervisor);

        SectorResponseDTO almacenResponse = new SectorResponseDTO();
        almacenResponse.setName("Almacén Principal");
        almacenResponse.setType(SectorType.ALMACEN);

        when(userRepository.findById(2L)).thenReturn(Optional.of(almacenSupervisor));
        when(sectorMapper.toEntity(createDTO)).thenReturn(almacenSector);
        when(sectorRepository.saveAndFlush(any(Sector.class))).thenReturn(almacenSector);
        when(sectorMapper.toResponseDTO(almacenSector)).thenReturn(almacenResponse);

        SectorResponseDTO result = sectorService.createSector(createDTO);

        assertNotNull(result);
        assertEquals("Almacén Principal", result.getName());
        assertEquals(SectorType.ALMACEN, result.getType());
    }

    @Test
    void createSector_withNonExistentSupervisor_throwsResourceNotFoundException() {
        SectorCreateDTO createDTO = new SectorCreateDTO();
        createDTO.setName("Sector Test");
        createDTO.setSupervisorId(999L);
        createDTO.setType(SectorType.ALMACEN);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sectorService.createSector(createDTO));
    }

    @Test
    void createSector_withWrongSupervisorRole_throwsBadRequestException() {
        User wrongSupervisor = new User();
        wrongSupervisor.setId(3L);
        wrongSupervisor.setRoles(Set.of(Role.OPERARIO_DE_PRODUCCION)); // Rol incorrecto

        SectorCreateDTO createDTO = new SectorCreateDTO();
        createDTO.setName("Sector Test");
        createDTO.setSupervisorId(3L);
        createDTO.setType(SectorType.PRODUCCION);
        createDTO.setPhase(Phase.MOLIENDA);
        createDTO.setProductionCapacity(500.0);
        createDTO.setIsTimeActive(true);

        when(userRepository.findById(3L)).thenReturn(Optional.of(wrongSupervisor));

        assertThrows(BadRequestException.class, () -> sectorService.createSector(createDTO));
    }

    @Test
    void createSector_produccionWithoutRequiredFields_throwsBadRequestException() {
        SectorCreateDTO createDTO = new SectorCreateDTO();
        createDTO.setName("Sector Incompleto");
        createDTO.setSupervisorId(1L);
        createDTO.setType(SectorType.PRODUCCION);
        // Falta phase, productionCapacity, isTimeActive

        when(userRepository.findById(1L)).thenReturn(Optional.of(supervisor));

        assertThrows(BadRequestException.class, () -> sectorService.createSector(createDTO));
    }

    @Test
    void getSector_withValidId_success() {
        when(sectorRepository.findById(1L)).thenReturn(Optional.of(sector));
        when(sectorMapper.toResponseDTO(sector)).thenReturn(responseDTO);

        SectorResponseDTO result = sectorService.getSector(1L);

        assertNotNull(result);
        assertEquals("Sector Molienda", result.getName());
        verify(sectorRepository, times(1)).findById(1L);
    }

    @Test
    void getSector_withInvalidId_throwsResourceNotFoundException() {
        when(sectorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sectorService.getSector(999L));
    }

    @Test
    void updateSector_withValidData_success() {
        SectorUpdateDTO updateDTO = new SectorUpdateDTO();
        updateDTO.setName("Sector Actualizado");

        when(sectorRepository.findById(1L)).thenReturn(Optional.of(sector));
        when(sectorMapper.partialUpdateSector(updateDTO, sector)).thenReturn(sector);
        when(sectorRepository.save(sector)).thenReturn(sector);
        when(sectorMapper.toResponseDTO(sector)).thenReturn(responseDTO);

        SectorResponseDTO result = sectorService.updateDTO(updateDTO, 1L);

        assertNotNull(result);
        verify(sectorRepository, times(1)).save(sector);
    }

    @Test
    void updateSector_withInvalidId_throwsResourceNotFoundException() {
        SectorUpdateDTO updateDTO = new SectorUpdateDTO();
        updateDTO.setName("Sector Actualizado");

        when(sectorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sectorService.updateDTO(updateDTO, 999L));
    }

    @Test
    void updateSector_changingSupervisorWithWrongRole_throwsBadRequestException() {
        User newSupervisor = new User();
        newSupervisor.setId(4L);
        newSupervisor.setRoles(Set.of(Role.OPERARIO_DE_ALMACEN)); // Rol incorrecto para PRODUCCION

        SectorUpdateDTO updateDTO = new SectorUpdateDTO();
        updateDTO.setSupervisorId(4L);
        updateDTO.setType(SectorType.PRODUCCION);

        when(sectorRepository.findById(1L)).thenReturn(Optional.of(sector));
        when(userRepository.findById(4L)).thenReturn(Optional.of(newSupervisor));

        assertThrows(BadRequestException.class, () -> sectorService.updateDTO(updateDTO, 1L));
    }
}
