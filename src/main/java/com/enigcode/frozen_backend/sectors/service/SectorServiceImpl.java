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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SectorServiceImpl implements SectorService {

    final SectorRepository sectorRepository;
    final UserRepository userRepository;
    final SectorMapper sectorMapper;

    /**
     * Crea un sector de un tipo y si el tipo es produccion entonces se debera
     * incluir parametros extras
     * 
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public SectorResponseDTO createSector(SectorCreateDTO dto) {
        User supervisor = userRepository.findById(dto.getSupervisorId()).orElseThrow(
                () -> new ResourceNotFoundException("No se encontró usuario con id " + dto.getSupervisorId()));

        if (!rolPhaseValidation(supervisor.getRoles(), dto.getType()))
            throw new BadRequestException("El rol:" + supervisor.getRoles()
                    + "del usuario no concuerda con el tipo de sector asignado:" + dto.getType());

        if (dto.getType().equals(SectorType.PRODUCCION) && !dtoProductionValidation(dto))
            throw new BadRequestException(
                    "El DTO para el tipo de sector PRODUCCION no tiene todos los campos requeridos");

        Sector sector = sectorMapper.toEntity(dto);
        sector.setSupervisor(supervisor);
        sector.setIsActive(Boolean.TRUE);
        sector.setCreationDate(OffsetDateTime.now());

        Sector savedSector = sectorRepository.saveAndFlush(sector);

        return sectorMapper.toResponseDTO(savedSector);
    }

    /**
     * Busca y devuelve sector segun el id
     * 
     * @param id
     * @return
     */
    @Override
    @Transactional
    public SectorResponseDTO getSector(Long id) {
        Sector sector = sectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró sector de id " + id));

        return sectorMapper.toResponseDTO(sector);
    }

    /**
     * Modifica ciertos parametros de un sector en especifico manteniendo las
     * restricciones de los mismos
     * 
     * @param dto
     * @param id
     * @return
     */
    @Override
    @Transactional
    public SectorResponseDTO updateDTO(SectorUpdateDTO dto, Long id) {
        Sector sector = sectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró sector de id " + id));
        User supervisor = null;

        if (dto.getSupervisorId() != null) {
            supervisor = userRepository.findById(dto.getSupervisorId()).orElseThrow(
                    () -> new ResourceNotFoundException("No se encontró usuario con id " + dto.getSupervisorId()));
        }

        if (supervisor == null)
            supervisor = sector.getSupervisor();

        if (dto.getType() != null && !rolPhaseValidation(supervisor.getRoles(), dto.getType()))
            throw new BadRequestException("El rol:" + supervisor.getRoles()
                    + "del usuario no concuerda con el tipo de sector asignado:" + dto.getType());

        if (dto.getType() != null && !dto.getType().equals(SectorType.PRODUCCION) && dtoProductionValidation(dto))
            throw new BadRequestException(
                    "El DTO para el tipo de sector" + dto.getType() + " no tiene campos para PRODUCCION");

        Sector updatedSector = sectorMapper.partialUpdateSector(dto, sector);
        updatedSector.setSupervisor(supervisor);

        Sector savedSector = sectorRepository.save(updatedSector);

        return sectorMapper.toResponseDTO(savedSector);
    }

    @Override
    @Transactional
    public List<Sector> getAllSectorsAvailableByPhase(Phase phase) {
        return sectorRepository.findAvailableProductionSectorsByPhase(phase);
    }

    @Override
    @Transactional
    public void saveAll(List<Sector> sectors) {
        sectorRepository.saveAll(sectors);
    }

    private boolean dtoProductionValidation(SectorCreateDTO dto) {
        return dto.getPhase() != null &&
                dto.getProductionCapacity() != null &&
                dto.getIsTimeActive() != null;
    }

    private boolean dtoProductionValidation(SectorUpdateDTO dto) {
        return dto.getPhase() != null &&
                dto.getProductionCapacity() != null &&
                dto.getIsTimeActive() != null;
    }

    private boolean rolPhaseValidation(Set<Role> supervisorRoles, SectorType type) {
        return supervisorRoles.stream().anyMatch(rol -> (rol == Role.SUPERVISOR_DE_PRODUCCION
                && type.equals(SectorType.PRODUCCION)) ||
                (rol == Role.SUPERVISOR_DE_ALMACEN
                        && type.equals(SectorType.ALMACEN))
                ||
                (rol == Role.SUPERVISOR_DE_CALIDAD
                        && type.equals(SectorType.CALIDAD)));
    }
}
