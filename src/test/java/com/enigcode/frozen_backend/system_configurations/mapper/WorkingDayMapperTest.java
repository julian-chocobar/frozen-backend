package com.enigcode.frozen_backend.system_configurations.mapper;

import com.enigcode.frozen_backend.system_configurations.DTO.WorkingDayUpdateDTO;
import com.enigcode.frozen_backend.system_configurations.model.WorkingDay;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class WorkingDayMapperTest {

    private final WorkingDayMapper mapper = Mappers.getMapper(WorkingDayMapper.class);

    @Test
    void partialUpdate_shouldOnlyUpdateNonNullFields() {
        WorkingDay existing = new WorkingDay();
        existing.setDayOfWeek(java.time.DayOfWeek.MONDAY);
        existing.setIsWorkingDay(true);
        existing.setOpeningHour(LocalTime.of(8, 0));
        existing.setClosingHour(LocalTime.of(17, 0));

        WorkingDayUpdateDTO dto = new WorkingDayUpdateDTO();
        dto.setDayOfWeek(java.time.DayOfWeek.MONDAY);
        dto.setIsWorkingDay(false); // change
        dto.setOpeningHour(null); // keep
        dto.setClosingHour(null); // keep

        mapper.partialUpdate(dto, existing);

        assertThat(existing.getIsWorkingDay()).isFalse();
        assertThat(existing.getOpeningHour()).isEqualTo(LocalTime.of(8, 0));
        assertThat(existing.getClosingHour()).isEqualTo(LocalTime.of(17, 0));
    }
}
