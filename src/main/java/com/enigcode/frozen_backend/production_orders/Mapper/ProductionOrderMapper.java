package com.enigcode.frozen_backend.production_orders.Mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderResponseDTO;
import com.enigcode.frozen_backend.production_orders.Model.ProductionOrder;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductionOrderMapper {
    ProductionOrder toEntity(ProductionOrderCreateDTO productionOrderCreateDTO);

    @Mapping(target = "batchId", source = "batch.id")
    @Mapping(target = "batchCode", source = "batch.code")
    @Mapping(target = "packagingName", source = "batch.packaging.name")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "unitMeasurement", source = "product.unitMeasurement")
    @Mapping(target = "plannedDate", source = "batch.plannedDate")
    @Mapping(target = "startDate", source = "batch.startDate")
    @Mapping(target = "completedDate", source = "batch.completedDate")
    @Mapping(target = "estimatedCompletedDate", source = "batch.estimatedCompletedDate")
    ProductionOrderResponseDTO toResponseDTO(ProductionOrder productionOrder);
}