package com.enigcode.frozen_backend.materials.mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.materials.DTO.MaterialCreateDTO;
import com.enigcode.frozen_backend.materials.DTO.MaterialDetailDTO;
import com.enigcode.frozen_backend.materials.DTO.MaterialResponseDTO;
import com.enigcode.frozen_backend.materials.DTO.MaterialUpdateDTO;
import com.enigcode.frozen_backend.materials.model.Material;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MaterialMapper {
        @Mapping(target = "isBelowThreshold", expression = "java(material.getStock() != null && material.getThreshold() != null "
                        +
                        "&& material.getStock() <= material.getThreshold())")
        @Mapping(target = "totalStock", expression = "java(material.getStock() + material.getReservedStock())")
        @Mapping(source = "stock", target = "availableStock")
        @Mapping(source = "reservedStock", target = "reservedStock")
        MaterialDetailDTO toDetailDto(Material material);

        @Mapping(target = "isBelowThreshold", expression = "java(material.getStock() != null && material.getThreshold() != null "
                        +
                        "&& material.getStock() <= material.getThreshold())")
        @Mapping(target = "totalStock", expression = "java(material.getStock() + material.getReservedStock())")
        @Mapping(source = "stock", target = "availableStock")
        MaterialResponseDTO toResponseDto(Material material);

        @Mapping(target = "stock", source = "stock", defaultValue = "0.0")
        Material toEntity(MaterialCreateDTO materialCreateDTO);

        @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
        Material partialUpdate(MaterialUpdateDTO materialupdateDTO, @MappingTarget Material material);

        // Mappings para funcionalidad de almacén
        @Mapping(target = "isBelowThreshold", expression = "java(material.getStock() != null && material.getThreshold() != null "
                        + "&& material.getStock() <= material.getThreshold())")
        @Mapping(target = "warehouseX", ignore = true)
        @Mapping(target = "warehouseY", ignore = true)
        @Mapping(target = "levelDisplay", ignore = true)
        com.enigcode.frozen_backend.materials.DTO.MaterialWarehouseLocationDTO toWarehouseLocationDTO(
                        Material material);
}