package com.enigcode.frozen_backend.sectors.mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.sectors.DTO.SectorCreateDTO;
import com.enigcode.frozen_backend.sectors.DTO.SectorResponseDTO;
import com.enigcode.frozen_backend.sectors.DTO.SectorUpdateDTO;
import com.enigcode.frozen_backend.sectors.model.Sector;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SectorMapper {
    Sector toEntity(SectorCreateDTO sectorCreateDTO);

    @Mapping(target = "supervisorId", source = "supervisor.id")
    SectorResponseDTO toResponseDTO(Sector sector);

    Sector partialUpdateSector(SectorUpdateDTO dto, @MappingTarget Sector sector);
}
