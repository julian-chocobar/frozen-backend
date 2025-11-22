package com.enigcode.frozen_backend.system_configurations.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.system_configurations.DTO.WorkingDayUpdateDTO;
import com.enigcode.frozen_backend.system_configurations.model.WorkingDay;
import com.enigcode.frozen_backend.system_configurations.model.SystemConfiguration;
import com.enigcode.frozen_backend.system_configurations.repository.SystemConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemConfigurationServiceImplNotFoundTest {

    @Mock
    private SystemConfigurationRepository repository;

    @InjectMocks
    private SystemConfigurationServiceImpl service;

    @Test
    void updateWorkingDays_throwsWhenNoActiveConfig() {
        when(repository.findFirstByIsActiveTrueOrderByIdDesc()).thenReturn(Optional.empty());

        WorkingDayUpdateDTO dto = WorkingDayUpdateDTO.builder().dayOfWeek(DayOfWeek.MONDAY).isWorkingDay(true).build();

        assertThrows(ResourceNotFoundException.class, () -> service.updateWorkingDays(List.of(dto)));
    }
}
