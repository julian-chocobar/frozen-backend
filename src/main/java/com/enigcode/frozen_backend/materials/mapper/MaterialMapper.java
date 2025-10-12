package com.enigcode.frozen_backend.materials.mapper;
import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.materials.DTO.MaterialCreateDTO;
import com.enigcode.frozen_backend.materials.DTO.MaterialDetailDTO;
import com.enigcode.frozen_backend.materials.DTO.MaterialResponseDTO;
import com.enigcode.frozen_backend.materials.DTO.MaterialUpdateDTO;
import com.enigcode.frozen_backend.materials.model.Material;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, componentModel = "spring")
public interface MaterialMapper {
    @Mapping( target = "is_below_threshold",
        expression = "java(material.getStock() != null && material.getThreshold() != null " +
                "&& material.getStock() <= material.getThreshold())" )
    MaterialDetailDTO toDetailDto(Material material);

    @Mapping( target = "is_below_threshold",
            expression = "java(material.getStock() != null && material.getThreshold() != null " +
                    "&& material.getStock() <= material.getThreshold())" )
    MaterialResponseDTO toResponseDto(Material material); Material toEntity(MaterialCreateDTO materialCreateDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Material partialUpdate(MaterialUpdateDTO materialupdateDTO, @MappingTarget Material material);
}