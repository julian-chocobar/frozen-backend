package com.enigcode.frozen_backend.system_configurations.mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.system_configurations.DTO.SystemConfigurationResponseDTO;
import com.enigcode.frozen_backend.system_configurations.model.SystemConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(config =GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SystemConfigurationMapper {
    SystemConfigurationResponseDTO toResponseDto(SystemConfiguration systemConfiguration);
}
