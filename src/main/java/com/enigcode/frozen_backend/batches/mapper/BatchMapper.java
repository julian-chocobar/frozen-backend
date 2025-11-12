package com.enigcode.frozen_backend.batches.mapper;

import com.enigcode.frozen_backend.batches.DTO.BatchResponseDTO;
import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface BatchMapper {
    @Mapping(source = "packaging.name", target = "packagingName")
    @Mapping(source = "productionOrder.product.name", target = "productName")
    @Mapping(source = "productionOrder.id", target = "orderId")
    @Mapping(source = "assignedUser.name", target = "assignedUserName")
    @Mapping(source = "assignedUser.id", target = "assignedUserId")
    BatchResponseDTO toResponseDTO(Batch batch);
}
