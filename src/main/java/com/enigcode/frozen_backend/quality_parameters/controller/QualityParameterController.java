package com.enigcode.frozen_backend.quality_parameters.controller;

import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterCreateDTO;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterResponseDTO;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterUpdateDTO;
import com.enigcode.frozen_backend.quality_parameters.service.QualityParameterService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quality-parameters")
@RequiredArgsConstructor
public class QualityParameterController {
    private final QualityParameterService qualityParameterService;

    @Operation(
            summary = "Crear parámetro de calidad",
            description = "Crea un parámetro de calidad nuevo para una fase en especifico")
    @PostMapping
    @PreAuthorize("hasRole('SUPERVISOR_DE_CALIDAD')")
    public ResponseEntity<QualityParameterResponseDTO> createQualityParameter(
            @Valid @RequestBody QualityParameterCreateDTO createDto){
        QualityParameterResponseDTO dto = qualityParameterService.createQualityParameter(createDto);

        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Update parámetro de calidad",
            description = "Modifica la descripción de un parámetro de calidad existente")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SUPERVISOR_DE_CALIDAD')")
    public ResponseEntity<QualityParameterResponseDTO> updateQualityParameter(
            @PathVariable Long id,
            @Valid @RequestBody QualityParameterUpdateDTO updateDto){
        QualityParameterResponseDTO dto = qualityParameterService.updateQualityParameter(id, updateDto);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(
            summary = "Cambiar activo en parámetro de calidad",
            description = "Cambia el estado activo de un parámetro de calidad existente")
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('SUPERVISOR_DE_CALIDAD')")
    public ResponseEntity<QualityParameterResponseDTO> toggleActive(@PathVariable Long id){
        QualityParameterResponseDTO dto = qualityParameterService.toggleActive(id);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(
            summary = "Ver parámetro de calidad",
            description = "Ver parámetro de calidad especifico")
    @GetMapping("/{id}")
    public ResponseEntity<QualityParameterResponseDTO> getQualityParameter(@PathVariable Long id){
        QualityParameterResponseDTO dto = qualityParameterService.getQualityParameter(id);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(
            summary = "Ver todos los parámetros de calidad",
            description = "Ver todos los parámetros de calidad")
    @GetMapping
    public ResponseEntity<List<QualityParameterResponseDTO>> getQualityParameters(){
        List<QualityParameterResponseDTO> dto = qualityParameterService.getQualityParameters();

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }


}
