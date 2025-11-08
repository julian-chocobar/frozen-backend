package com.enigcode.frozen_backend.production_phases_qualities.mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.production_phases_qualities.DTO.ProductionPhaseQualityResponseDTO;
import com.enigcode.frozen_backend.production_phases_qualities.model.ProductionPhaseQuality;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductionPhaseQualityMapper {
    @Mapping(target = "qualityParameterName", source = "qualityParameter.name")
    @Mapping(target = "productionPhaseId", source = "productionPhase.id")
    @Mapping(target = "productionPhase", source = "productionPhase.phase")
    ProductionPhaseQualityResponseDTO toResponseDTO (ProductionPhaseQuality productionPhaseQuality);
}
