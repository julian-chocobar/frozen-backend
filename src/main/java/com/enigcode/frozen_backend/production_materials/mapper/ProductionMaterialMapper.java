package com.enigcode.frozen_backend.production_materials.mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.production_materials.DTO.ProductionMaterialResponseDTO;
import com.enigcode.frozen_backend.production_materials.model.ProductionMaterial;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductionMaterialMapper {

    @Mapping(target = "materialId", source = "material.id")
    @Mapping(target = "materialCode", source = "material.code")
    @Mapping(target = "productionPhaseId", source = "productionPhase.id")
    ProductionMaterialResponseDTO toResponseDTO(ProductionMaterial productionMaterial);
}
