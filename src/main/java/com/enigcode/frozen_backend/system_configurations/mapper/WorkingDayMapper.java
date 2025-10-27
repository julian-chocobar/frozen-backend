package com.enigcode.frozen_backend.system_configurations.mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.system_configurations.DTO.WorkingDayUpdateDTO;
import com.enigcode.frozen_backend.system_configurations.model.WorkingDay;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface WorkingDayMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    WorkingDay partialUpdate(WorkingDayUpdateDTO workingDayUpdateDTO, @MappingTarget WorkingDay workingDay);
}