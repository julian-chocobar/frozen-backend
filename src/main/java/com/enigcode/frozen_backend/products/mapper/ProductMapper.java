package com.enigcode.frozen_backend.products.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.products.DTO.ProductResponseDTO;
import com.enigcode.frozen_backend.products.model.Product;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    ProductResponseDTO toResponseDto(Product product);

}
