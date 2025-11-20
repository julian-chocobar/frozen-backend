package com.enigcode.frozen_backend.analytics.mapper;

import com.enigcode.frozen_backend.analytics.DTO.MonthlyTotalDTO;
import com.enigcode.frozen_backend.analytics.DTO.MonthlyTotalProjectionDTO;
import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import java.util.List;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AnalyticsMapper {
    @Mapping(
            target = "month",
            expression = "java( java.time.YearMonth.of(source.getYear(), source.getMonth()).toString() )"
    )
    @Mapping(target = "total", source = "total", qualifiedByName = "round2")
    MonthlyTotalDTO toMonthlyTotalDTO(MonthlyTotalProjectionDTO source);

    List<MonthlyTotalDTO> toMonthlyProductionDTOList(List<MonthlyTotalProjectionDTO> source);
}
