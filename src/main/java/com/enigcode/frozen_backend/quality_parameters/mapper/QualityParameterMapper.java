package com.enigcode.frozen_backend.quality_parameters.mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterCreateDTO;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterResponseDTO;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterSimpleDTO;
import com.enigcode.frozen_backend.quality_parameters.model.QualityParameter;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface QualityParameterMapper {
    QualityParameter toEntity(QualityParameterCreateDTO createDto);

    QualityParameterResponseDTO toResponseDTO(QualityParameter savedQualityParameter);

    QualityParameterSimpleDTO toSimpleDTO(QualityParameter qualityParameter);
}
