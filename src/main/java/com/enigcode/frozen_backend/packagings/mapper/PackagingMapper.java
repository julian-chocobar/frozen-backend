package com.enigcode.frozen_backend.packagings.mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.packagings.DTO.PackagingCreateDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingResponseDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingSimpleResponseDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingUpdateDTO;
import com.enigcode.frozen_backend.packagings.model.Packaging;

import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PackagingMapper {

    Packaging toEntity(PackagingCreateDTO packagingCreateDTO);

    @Mapping(source = "material.name", target = "materialName")
    PackagingResponseDTO toResponseDto(Packaging packaging);

    PackagingSimpleResponseDTO toSimpleResponseDTO(Packaging packaging);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Packaging partialUpdate(PackagingUpdateDTO packagingUpdateDTO, @MappingTarget Packaging packaging);
}
