package com.enigcode.frozen_backend.common.service;

import com.enigcode.frozen_backend.system_configurations.DTO.WorkingDayUpdateDTO;
import com.enigcode.frozen_backend.system_configurations.service.SystemConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigurationDataLoaderService {

    private final SystemConfigurationService systemConfigurationService;

    public void loadSystemConfiguration() {
        systemConfigurationService.getSystemConfiguration();

        LocalTime open = LocalTime.of(9, 0);
        LocalTime close = LocalTime.of(17, 0);

        List<WorkingDayUpdateDTO> updates = List.of(
                WorkingDayUpdateDTO.builder().dayOfWeek(DayOfWeek.MONDAY).isWorkingDay(true)
                        .openingHour(open).closingHour(close).build(),
                WorkingDayUpdateDTO.builder().dayOfWeek(DayOfWeek.TUESDAY).isWorkingDay(true)
                        .openingHour(open).closingHour(close).build(),
                WorkingDayUpdateDTO.builder().dayOfWeek(DayOfWeek.WEDNESDAY).isWorkingDay(true)
                        .openingHour(open).closingHour(close).build(),
                WorkingDayUpdateDTO.builder().dayOfWeek(DayOfWeek.THURSDAY).isWorkingDay(true)
                        .openingHour(open).closingHour(close).build(),
                WorkingDayUpdateDTO.builder().dayOfWeek(DayOfWeek.FRIDAY).isWorkingDay(true)
                        .openingHour(open).closingHour(close).build(),
                WorkingDayUpdateDTO.builder().dayOfWeek(DayOfWeek.SATURDAY).isWorkingDay(false)
                        .openingHour(null).closingHour(null).build(),
                WorkingDayUpdateDTO.builder().dayOfWeek(DayOfWeek.SUNDAY).isWorkingDay(false)
                        .openingHour(null).closingHour(null).build());

        systemConfigurationService.updateWorkingDays(updates);
    }
}

