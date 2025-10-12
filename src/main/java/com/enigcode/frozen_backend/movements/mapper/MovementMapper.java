package com.enigcode.frozen_backend.movements.mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.movements.DTO.MovementResponseDTO;
import com.enigcode.frozen_backend.movements.model.Movement;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MovementMapper {
    @Mapping(source = "material.type", target = "materialType")
    MovementResponseDTO toResponseDto(Movement movement);
}