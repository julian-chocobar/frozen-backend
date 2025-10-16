package com.enigcode.frozen_backend.products.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.products.DTO.ProductResponseDTO;
import com.enigcode.frozen_backend.products.DTO.ProductUpdateDTO;
import com.enigcode.frozen_backend.products.model.Product;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    ProductResponseDTO toResponseDto(Product product);

}
