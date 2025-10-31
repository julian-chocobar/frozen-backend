package com.enigcode.frozen_backend.system_configurations.service;

import com.enigcode.frozen_backend.system_configurations.DTO.SystemConfigurationResponseDTO;
import com.enigcode.frozen_backend.system_configurations.DTO.WorkingDayUpdateDTO;
import com.enigcode.frozen_backend.system_configurations.model.WorkingDay;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

public interface SystemConfigurationService {
    SystemConfigurationResponseDTO getSystemConfiguration();

    SystemConfigurationResponseDTO updateWorkingDays(List<WorkingDayUpdateDTO> dtos);

    Map<DayOfWeek,WorkingDay> getWorkingDays();
}
