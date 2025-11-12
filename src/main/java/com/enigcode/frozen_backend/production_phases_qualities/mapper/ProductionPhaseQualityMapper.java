package com.enigcode.frozen_backend.production_phases_qualities.mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.production_phases_qualities.DTO.ProductionPhaseQualityCreateDTO;
import com.enigcode.frozen_backend.production_phases_qualities.DTO.ProductionPhaseQualityResponseDTO;
import com.enigcode.frozen_backend.production_phases_qualities.DTO.ProductionPhaseQualityUpdateDTO;
import com.enigcode.frozen_backend.production_phases_qualities.model.ProductionPhaseQuality;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductionPhaseQualityMapper {
    @Mapping(target = "qualityParameterName", source = "qualityParameter.name")
    @Mapping(target = "productionPhaseId", source = "productionPhase.id")
    @Mapping(target = "productionPhase", source = "productionPhase.phase")
    ProductionPhaseQualityResponseDTO toResponseDTO(ProductionPhaseQuality productionPhaseQuality);

    ProductionPhaseQuality toEntity(ProductionPhaseQualityCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productionPhase", ignore = true)
    @Mapping(target = "qualityParameter", ignore = true)
    @Mapping(target = "realizationDate", ignore = true)
    @Mapping(target = "version", ignore = true) // La versión no se actualiza mediante update
    @Mapping(target = "value", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "isApproved", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "isActive", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ProductionPhaseQuality partialUpdate(ProductionPhaseQualityUpdateDTO dto,
            @MappingTarget ProductionPhaseQuality productionPhaseQuality);
}
