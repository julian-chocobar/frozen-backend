package com.enigcode.frozen_backend.system_configurations.service;

import com.enigcode.frozen_backend.system_configurations.DTO.SystemConfigurationResponseDTO;
import com.enigcode.frozen_backend.system_configurations.mapper.SystemConfigurationMapper;
import com.enigcode.frozen_backend.system_configurations.model.SystemConfiguration;
import com.enigcode.frozen_backend.system_configurations.model.WorkingDay;
import com.enigcode.frozen_backend.system_configurations.repository.SystemConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemConfigurationServiceImplParentReferenceTest {

    @Mock
    private SystemConfigurationRepository repository;

    @Mock
    private SystemConfigurationMapper mapper;

    @InjectMocks
    private SystemConfigurationServiceImpl service;

    @Test
    void createdWorkingDays_referenceParentConfiguration() {
        when(repository.findFirstByIsActiveTrueOrderByIdDesc()).thenReturn(java.util.Optional.empty());
        when(repository.saveAndFlush(any(SystemConfiguration.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponseDto(any())).thenReturn(SystemConfigurationResponseDTO.builder().isActive(true).build());

        SystemConfigurationResponseDTO res = service.getSystemConfiguration();
        assertNotNull(res);

        ArgumentCaptor<SystemConfiguration> captor = ArgumentCaptor.forClass(SystemConfiguration.class);
        verify(repository).saveAndFlush(captor.capture());

        SystemConfiguration saved = captor.getValue();
        assertNotNull(saved.getWorkingDays());
        for (WorkingDay wd : saved.getWorkingDays()) {
            assertSame(saved, wd.getSystemConfiguration(), "Cada WorkingDay debe referenciar la SystemConfiguration padre");
        }
    }
}
