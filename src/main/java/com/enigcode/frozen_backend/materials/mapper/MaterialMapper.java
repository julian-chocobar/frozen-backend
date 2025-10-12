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
    @Mapping( target = "is_below_threshold",
        expression = "java(material.getStock() != null && material.getThreshold() != null " +
                "&& material.getStock() <= material.getThreshold())" )
    MaterialDetailDTO toDetailDto(Material material);

    @Mapping( target = "is_below_threshold",
            expression = "java(material.getStock() != null && material.getThreshold() != null " +
                    "&& material.getStock() <= material.getThreshold())" )
    MaterialResponseDTO toResponseDto(Material material); 
    
    @Mapping(target = "id", ignore = true)  
    @Mapping(target = "code", ignore = true)  
    @Mapping(target = "creationDate", ignore = true)  
    @Mapping(target = "isActive", ignore = true)  
    @Mapping(target = "lastUpdateDate", ignore = true)  
    Material toEntity(MaterialCreateDTO materialCreateDTO);

    @Mapping(target = "id", ignore = true)  
    @Mapping(target = "code", ignore = true)  
    @Mapping(target = "creationDate", ignore = true)  
    @Mapping(target = "isActive", ignore = true)  
    @Mapping(target = "lastUpdateDate", ignore = true)  
    @Mapping(target = "stock", ignore = true)  
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Material partialUpdate(MaterialUpdateDTO materialupdateDTO, @MappingTarget Material material);
}