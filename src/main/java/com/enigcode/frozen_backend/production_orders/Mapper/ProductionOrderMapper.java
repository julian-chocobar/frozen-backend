package com.enigcode.frozen_backend.production_orders.Mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import com.enigcode.frozen_backend.production_orders.Model.ProductionOrder;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductionOrderMapper {
    ProductionOrder toEntity(ProductionOrderCreateDTO productionOrderCreateDTO);
}