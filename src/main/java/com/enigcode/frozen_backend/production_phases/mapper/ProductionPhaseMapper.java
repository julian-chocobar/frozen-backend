package com.enigcode.frozen_backend.production_phases.mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.production_phases.DTO.ProductionPhaseResponseDTO;
import com.enigcode.frozen_backend.production_phases.DTO.ProductionPhaseUnderReviewDTO;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductionPhaseMapper {
    ProductionPhase partialUpdate(ProductionPhaseUnderReviewDTO dto, @MappingTarget ProductionPhase productionPhase);
    ProductionPhaseResponseDTO toResponseDTO(ProductionPhase savedProductionPhase);
}
