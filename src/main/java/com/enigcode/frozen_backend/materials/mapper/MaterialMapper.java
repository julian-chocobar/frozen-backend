package com.enigcode.frozen_backend.materials.mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.materials.DTO.MaterialResponseDTO;
import com.enigcode.frozen_backend.materials.model.Material;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class, componentModel = "spring")
public interface MaterialMapper {

    @Mapping(target = "unitMeasurement", ignore = true)
    MaterialResponseDTO toDto(Material entity);
    
    @Mapping(target = "id", ignore = true)  
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    Material toEntity(MaterialResponseDTO dto);
}
