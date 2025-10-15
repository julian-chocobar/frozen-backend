package com.enigcode.frozen_backend.recipes.mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.recipes.DTO.RecipeCreateDTO;
import com.enigcode.frozen_backend.recipes.DTO.RecipeResponseDTO;
import com.enigcode.frozen_backend.recipes.model.Recipe;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface RecipeMapper {

    Recipe toEntity(RecipeCreateDTO recipeCreateDTO);


    @Mapping(source = "material.name", target = "materialName")
    @Mapping(source = "material.code", target = "materialCode")
    @Mapping(source = "material.unitMeasurement.", target = "materialUnit")
    RecipeResponseDTO toResponseDTO(Recipe recipe);
}
