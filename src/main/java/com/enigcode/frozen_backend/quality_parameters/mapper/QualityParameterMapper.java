package com.enigcode.frozen_backend.quality_parameters.mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface QualityParameterMapper {
}
