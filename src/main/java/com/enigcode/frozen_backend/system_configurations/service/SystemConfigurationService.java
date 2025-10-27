package com.enigcode.frozen_backend.system_configurations.service;

import com.enigcode.frozen_backend.system_configurations.DTO.SystemConfigurationResponseDTO;
import com.enigcode.frozen_backend.system_configurations.DTO.WorkingDayUpdateDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface SystemConfigurationService {
    SystemConfigurationResponseDTO getSystemConfiguration();
    SystemConfigurationResponseDTO updateWorkingDays(List<WorkingDayUpdateDTO> dtos);
}
