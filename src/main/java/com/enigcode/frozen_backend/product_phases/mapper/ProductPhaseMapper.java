package com.enigcode.frozen_backend.product_phases.mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseResponseDTO;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseUpdateDTO;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config =GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductPhaseMapper {

    @Mapping(target = "inputMaterials", source = "input")
    @Mapping(target = "estimatedTime", source = "estimatedHours")
    ProductPhaseResponseDTO toResponseDto(ProductPhase productPhase);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(ProductPhaseUpdateDTO productPhaseUpdateDTO, @MappingTarget ProductPhase productPhase);
}
