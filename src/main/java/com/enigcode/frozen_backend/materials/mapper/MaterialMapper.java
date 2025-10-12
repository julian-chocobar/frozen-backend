package com.enigcode.frozen_backend.materials.mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.materials.DTO.MaterialCreateDTO;
import com.enigcode.frozen_backend.materials.DTO.MaterialResponseDTO;
import com.enigcode.frozen_backend.materials.DTO.MaterialUpdateDTO;
import com.enigcode.frozen_backend.materials.model.Material;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, componentModel = "spring")
public interface MaterialMapper {

    Material toEntity(MaterialResponseDTO materialResponseDTO);
    MaterialResponseDTO toResponseDto(Material material);

    Material toEntity(MaterialCreateDTO materialCreateDTO);
    MaterialCreateDTO toCreateDto(Material material);

    Material toEntity(MaterialUpdateDTO materialUpdateDTO);
    MaterialUpdateDTO toUpdateDto(Material material);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Material partialUpdate(MaterialResponseDTO materialResponseDTO, @MappingTarget Material material);
}
