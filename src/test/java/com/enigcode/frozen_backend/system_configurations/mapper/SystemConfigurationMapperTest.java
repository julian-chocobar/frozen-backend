package com.enigcode.frozen_backend.system_configurations.mapper;

import com.enigcode.frozen_backend.system_configurations.DTO.SystemConfigurationResponseDTO;
import com.enigcode.frozen_backend.system_configurations.model.SystemConfiguration;
import com.enigcode.frozen_backend.system_configurations.model.WorkingDay;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.DayOfWeek;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SystemConfigurationMapperTest {

    private final SystemConfigurationMapper mapper = Mappers.getMapper(SystemConfigurationMapper.class);

    @Test
    void toResponseDto_mapsBasicFields_andWorkingDays() {
        WorkingDay w1 = new WorkingDay();
        w1.setDayOfWeek(DayOfWeek.MONDAY);
        WorkingDay w2 = new WorkingDay();
        w2.setDayOfWeek(DayOfWeek.TUESDAY);

        SystemConfiguration entity = new SystemConfiguration();
        entity.setIsActive(true);
        entity.setWorkingDays(List.of(w1, w2));

        SystemConfigurationResponseDTO dto = mapper.toResponseDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getIsActive()).isTrue();
        assertThat(dto.getWorkingDays()).hasSize(2);
    }
}
