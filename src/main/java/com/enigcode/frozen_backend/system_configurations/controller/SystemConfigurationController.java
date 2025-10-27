package com.enigcode.frozen_backend.system_configurations.controller;

import com.enigcode.frozen_backend.system_configurations.DTO.SystemConfigurationResponseDTO;
import com.enigcode.frozen_backend.system_configurations.DTO.WorkingDayUpdateDTO;
import com.enigcode.frozen_backend.system_configurations.service.SystemConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system-configurations")
@RequiredArgsConstructor
public class SystemConfigurationController {

    final SystemConfigurationService systemConfigurationService;

    @Operation(summary = "Devolver parametros de configuración",
    description = "Devuelve la configuracion activa del sistema, o la crea una default en caso de no existir")
    @GetMapping
    public ResponseEntity<SystemConfigurationResponseDTO> getSystemConfiguration(){
        SystemConfigurationResponseDTO systemConfigurationResponseDTO = systemConfigurationService.getSystemConfiguration();

        return new ResponseEntity<>(systemConfigurationResponseDTO, HttpStatus.OK);
    }

    @Operation(summary = "Cambiar parametros de working days",
    description = "Modifica parametros especificos de los working days")
    @PatchMapping("working-days")
    public ResponseEntity<SystemConfigurationResponseDTO> updateWorkingDays(
            @Valid @RequestBody List<WorkingDayUpdateDTO> dtos){
        SystemConfigurationResponseDTO dto = systemConfigurationService.updateWorkingDays(dtos);

        return  new ResponseEntity<>(dto, HttpStatus.OK);
    }
}
