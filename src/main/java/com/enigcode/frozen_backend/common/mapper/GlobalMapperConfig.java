package com.enigcode.frozen_backend.common.mapper;

import org.mapstruct.*;

@MapperConfig(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {
                DoubleRoundingUtil.class
        }
)
public interface GlobalMapperConfig {

        @Named("round2")
        default Double mapDouble(Double value) {
                return DoubleRoundingUtil.round2(value);
        }
}
