package com.enigcode.frozen_backend.product_phases.mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseCreateDTO;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseResponseDTO;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(config =GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductPhaseMapper {

    @Mapping(target = "inputMaterials", source = "input")
    @Mapping(target = "estimatedTime", source = "estimatedHours")
    ProductPhaseResponseDTO toResponseDto(ProductPhase productPhase);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "isReady", ignore = true)
    ProductPhase toEntity(ProductPhaseCreateDTO productPhaseCreateDTO);
}
