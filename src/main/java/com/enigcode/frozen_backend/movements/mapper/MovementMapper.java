package com.enigcode.frozen_backend.movements.mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.movements.DTO.MovementDetailDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementResponseDTO;
import com.enigcode.frozen_backend.movements.model.Movement;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MovementMapper {
    @Mapping(source = "material.type", target = "materialType")
    @Mapping(source = "material.unitMeasurement", target = "unitMeasurement")
    MovementResponseDTO toResponseDto(Movement movement);

    @Mapping(source = "material.type", target = "materialType")
    @Mapping(source = "material.id", target = "materialId")
    @Mapping(source = "material.name", target = "materialName")
    @Mapping(source = "material.code", target = "materialCode")
    @Mapping(source = "material.unitMeasurement", target = "unitMeasurement")
    MovementDetailDTO toDetailDTO(Movement movement);
}