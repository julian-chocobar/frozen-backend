package com.enigcode.frozen_backend.system_configurations.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.system_configurations.DTO.SystemConfigurationResponseDTO;
import com.enigcode.frozen_backend.system_configurations.DTO.WorkingDayUpdateDTO;
import com.enigcode.frozen_backend.system_configurations.mapper.SystemConfigurationMapper;
import com.enigcode.frozen_backend.system_configurations.mapper.WorkingDayMapper;
import com.enigcode.frozen_backend.system_configurations.model.DayOfWeek;
import com.enigcode.frozen_backend.system_configurations.model.SystemConfiguration;
import com.enigcode.frozen_backend.system_configurations.model.WorkingDay;
import com.enigcode.frozen_backend.system_configurations.repository.SystemConfigurationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SystemConfigurationServiceImpl implements SystemConfigurationService{

    final SystemConfigurationRepository systemConfigurationRepository;
    final SystemConfigurationMapper systemConfigurationMapper;
    final WorkingDayMapper workingDayMapper;

    /**
     * Devuelve la ultima configuracion activa y si no existe crea una default
     * @return
     */
    @Override
    @Transactional
    public SystemConfigurationResponseDTO getSystemConfiguration(){
        SystemConfiguration systemConfiguration = systemConfigurationRepository.findFirstByIsActiveTrueOrderByIdDesc()
                .orElseGet(this::createSystemConfiguration);
        return systemConfigurationMapper.toResponseDto(systemConfiguration);
    }

    private SystemConfiguration createSystemConfiguration() {
        List<WorkingDay> workingDayList = Arrays.stream(DayOfWeek.values()).map( day -> {
                return WorkingDay.builder()
                        .dayOfWeek(day)
                        .build();}).toList();

        SystemConfiguration systemConfiguration = SystemConfiguration.builder()
                .workingDays(workingDayList)
                .isActive(Boolean.TRUE)
                .build();

        return systemConfigurationRepository.saveAndFlush(systemConfiguration);
    }

    /**
     * Update de los dias de trabajo, cambiando unicamente sus parametros de la configuracion
     * @param dtos
     * @param id
     * @return
     */
    @Override
    @Transactional
    public SystemConfigurationResponseDTO updateWorkingDays(List<WorkingDayUpdateDTO> dtos) {
        SystemConfiguration systemConfiguration = systemConfigurationRepository.findFirstByIsActiveTrueOrderByIdDesc().
                orElseThrow(() -> new ResourceNotFoundException("No existe configuración activa"));

        dtos.forEach(dto -> {
            Optional<WorkingDay> workingDay = findWorkingDay(dto.getDayOfWeek(), systemConfiguration);
            workingDay.ifPresent(day -> workingDayMapper.partialUpdate(dto, day));
        });

         SystemConfiguration savedSystemConfiguration = systemConfigurationRepository.save(systemConfiguration);

        return systemConfigurationMapper.toResponseDto(savedSystemConfiguration);
    }

    private Optional<WorkingDay> findWorkingDay(DayOfWeek dayOfWeek, SystemConfiguration systemConfiguration) {
        return systemConfiguration.getWorkingDays().stream()
                .filter(workingDay -> workingDay.getDayOfWeek() == dayOfWeek)
                .findFirst();
    }
}
