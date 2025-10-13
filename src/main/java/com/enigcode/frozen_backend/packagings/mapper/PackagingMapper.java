package com.enigcode.frozen_backend.packagings.mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.packagings.DTO.PackagingCreateDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingResponseDTO;
import com.enigcode.frozen_backend.packagings.model.Packaging;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PackagingMapper {

    Packaging toEntity(PackagingCreateDTO packagingCreateDTO);

    PackagingResponseDTO toResponseDto(Packaging packaging);
}
