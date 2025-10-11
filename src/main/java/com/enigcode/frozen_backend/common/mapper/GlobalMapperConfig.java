package com.enigcode.frozen_backend.common.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.WARN,
        uses = {
                DateMapperUtil.class, // <-- Ejemplo de un Bean de Spring que ayuda a mapear
                // OtraClaseDeAyuda.class
        }
)
public class GlobalMapperConfig {
}
