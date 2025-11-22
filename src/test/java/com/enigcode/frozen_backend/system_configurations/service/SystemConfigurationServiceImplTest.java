package com.enigcode.frozen_backend.system_configurations.service;

import com.enigcode.frozen_backend.system_configurations.DTO.SystemConfigurationResponseDTO;
import com.enigcode.frozen_backend.system_configurations.DTO.WorkingDayUpdateDTO;
import com.enigcode.frozen_backend.system_configurations.mapper.SystemConfigurationMapper;
import com.enigcode.frozen_backend.system_configurations.mapper.WorkingDayMapper;
import com.enigcode.frozen_backend.system_configurations.model.SystemConfiguration;
import com.enigcode.frozen_backend.system_configurations.model.WorkingDay;
import com.enigcode.frozen_backend.system_configurations.repository.SystemConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemConfigurationServiceImplTest {

    @Mock
    private SystemConfigurationRepository repository;

    @Mock
    private SystemConfigurationMapper mapper;

    @Mock
    private WorkingDayMapper workingDayMapper;

    @InjectMocks
    private SystemConfigurationServiceImpl service;

    @BeforeEach
    void setup() {
        // MockitoExtension handles mocks
    }

    @Test
    void getSystemConfiguration_createsDefaultWhenNoneExists() {
        when(repository.findFirstByIsActiveTrueOrderByIdDesc()).thenReturn(Optional.empty());

        // Ensure saveAndFlush returns the created entity (mock behaviour)
        when(repository.saveAndFlush(any(SystemConfiguration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SystemConfigurationResponseDTO dummyDto = SystemConfigurationResponseDTO.builder().isActive(true).build();
        when(mapper.toResponseDto(any())).thenReturn(dummyDto);

        SystemConfigurationResponseDTO result = service.getSystemConfiguration();

        assertNotNull(result);
        verify(repository).findFirstByIsActiveTrueOrderByIdDesc();

        ArgumentCaptor<SystemConfiguration> captor = ArgumentCaptor.forClass(SystemConfiguration.class);
        verify(repository).saveAndFlush(captor.capture());

        SystemConfiguration saved = captor.getValue();
        assertTrue(Boolean.TRUE.equals(saved.getIsActive()));
        assertNotNull(saved.getWorkingDays());
        assertEquals(7, saved.getWorkingDays().size(), "Debe crear 7 working days, uno por DayOfWeek");

        // Ensure mapper is used to build response
        verify(mapper).toResponseDto(any(SystemConfiguration.class));
    }

    @Test
    void updateWorkingDays_appliesPartialUpdateAndSaves() {
        SystemConfiguration cfg = SystemConfiguration.builder().isActive(true).build();

        WorkingDay monday = WorkingDay.builder().dayOfWeek(DayOfWeek.MONDAY).systemConfiguration(cfg).build();
        WorkingDay tuesday = WorkingDay.builder().dayOfWeek(DayOfWeek.TUESDAY).systemConfiguration(cfg).build();
        cfg.setWorkingDays(List.of(monday, tuesday));

        when(repository.findFirstByIsActiveTrueOrderByIdDesc()).thenReturn(Optional.of(cfg));

        WorkingDayUpdateDTO dto = WorkingDayUpdateDTO.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .isWorkingDay(Boolean.TRUE)
                .openingHour(LocalTime.of(8,0))
                .closingHour(LocalTime.of(17,0))
                .build();

        SystemConfiguration savedCfg = SystemConfiguration.builder().isActive(true).workingDays(cfg.getWorkingDays()).build();
        when(repository.save(any(SystemConfiguration.class))).thenReturn(savedCfg);
        when(mapper.toResponseDto(any(SystemConfiguration.class))).thenReturn(SystemConfigurationResponseDTO.builder().isActive(true).build());

        SystemConfigurationResponseDTO resp = service.updateWorkingDays(List.of(dto));

        assertNotNull(resp);
        verify(repository).findFirstByIsActiveTrueOrderByIdDesc();

        // Verify partialUpdate was called for monday only
        verify(workingDayMapper, times(1)).partialUpdate(eq(dto), eq(monday));
        verify(workingDayMapper, never()).partialUpdate(any(), eq(tuesday));

        verify(repository).save(any(SystemConfiguration.class));
        verify(mapper).toResponseDto(any(SystemConfiguration.class));
    }

    @Test
    void getWorkingDays_returnsMapByDayOfWeek() {
        SystemConfiguration cfg = SystemConfiguration.builder().isActive(true).build();
        WorkingDay w1 = WorkingDay.builder().dayOfWeek(DayOfWeek.MONDAY).systemConfiguration(cfg).build();
        WorkingDay w2 = WorkingDay.builder().dayOfWeek(DayOfWeek.WEDNESDAY).systemConfiguration(cfg).build();
        cfg.setWorkingDays(Arrays.asList(w1, w2));


        when(repository.findFirstByIsActiveTrueOrderByIdDesc()).thenReturn(Optional.of(cfg));
        // The service maps the entity to a DTO; ensure mapper returns a DTO with workingDays set
        when(mapper.toResponseDto(eq(cfg))).thenReturn(SystemConfigurationResponseDTO.builder().workingDays(cfg.getWorkingDays()).isActive(cfg.getIsActive()).build());

        Map<DayOfWeek, WorkingDay> map = service.getWorkingDays();

        assertNotNull(map);
        assertEquals(2, map.size());
        assertSame(w1, map.get(DayOfWeek.MONDAY));
        assertSame(w2, map.get(DayOfWeek.WEDNESDAY));
    }
}
