package com.enigcode.frozen_backend.system_configurations.DTO;

import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkingDayUpdateDTO {
    private DayOfWeek dayOfWeek;
    private Boolean isWorkingDay;
    private LocalTime openingHour;
    private LocalTime closingHour;
}
