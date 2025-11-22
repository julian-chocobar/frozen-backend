package com.enigcode.frozen_backend.system_configurations.dto;

import com.enigcode.frozen_backend.system_configurations.DTO.SystemConfigurationResponseDTO;
import com.enigcode.frozen_backend.system_configurations.DTO.WorkingDayUpdateDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SystemConfigurationDtoSerializationTest {

    private final ObjectMapper mapper;

    SystemConfigurationDtoSerializationTest() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void workingDayUpdateDto_serializesAndDeserializesLocalTime() throws Exception {
        WorkingDayUpdateDTO dto = WorkingDayUpdateDTO.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .isWorkingDay(true)
                .openingHour(LocalTime.of(8, 30))
                .closingHour(LocalTime.of(17, 15))
                .build();

        String json = mapper.writeValueAsString(dto);
        assertThat(json).contains("08:30:00");

        WorkingDayUpdateDTO read = mapper.readValue(json, WorkingDayUpdateDTO.class);
        assertThat(read.getOpeningHour()).isEqualTo(LocalTime.of(8, 30));
        assertThat(read.getClosingHour()).isEqualTo(LocalTime.of(17, 15));
    }

    @Test
    void systemConfigurationResponseDto_serializesWorkingDays() throws Exception {
        SystemConfigurationResponseDTO resp = SystemConfigurationResponseDTO.builder()
                .isActive(true)
                .workingDays(List.of())
                .build();

        String json = mapper.writeValueAsString(resp);
        assertThat(json).contains("isActive");

        SystemConfigurationResponseDTO read = mapper.readValue(json, SystemConfigurationResponseDTO.class);
        assertThat(read.getIsActive()).isTrue();
    }
}
