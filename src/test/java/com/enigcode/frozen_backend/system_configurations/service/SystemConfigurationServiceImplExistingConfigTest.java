package com.enigcode.frozen_backend.system_configurations.service;

import com.enigcode.frozen_backend.system_configurations.DTO.SystemConfigurationResponseDTO;
import com.enigcode.frozen_backend.system_configurations.mapper.SystemConfigurationMapper;
import com.enigcode.frozen_backend.system_configurations.model.SystemConfiguration;
import com.enigcode.frozen_backend.system_configurations.model.WorkingDay;
import com.enigcode.frozen_backend.system_configurations.repository.SystemConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemConfigurationServiceImplExistingConfigTest {

    @Mock
    private SystemConfigurationRepository repository;

    @Mock
    private SystemConfigurationMapper mapper;

    @InjectMocks
    private SystemConfigurationServiceImpl service;

    @Test
    void getSystemConfiguration_returnsExistingWithoutCreating() {
        SystemConfiguration cfg = SystemConfiguration.builder().isActive(true).build();
        WorkingDay w1 = WorkingDay.builder().dayOfWeek(DayOfWeek.MONDAY).systemConfiguration(cfg).build();
        cfg.setWorkingDays(Arrays.asList(w1));

        when(repository.findFirstByIsActiveTrueOrderByIdDesc()).thenReturn(Optional.of(cfg));

        SystemConfigurationResponseDTO dto = SystemConfigurationResponseDTO.builder().isActive(true).workingDays(cfg.getWorkingDays()).build();
        when(mapper.toResponseDto(eq(cfg))).thenReturn(dto);

        SystemConfigurationResponseDTO res = service.getSystemConfiguration();

        assertNotNull(res);
        assertEquals(Boolean.TRUE, res.getIsActive());
        verify(repository, never()).saveAndFlush(any());
        verify(mapper).toResponseDto(eq(cfg));
    }
}
