package com.enigcode.frozen_backend.production_phases_qualities.controller;

import com.enigcode.frozen_backend.production_phases_qualities.DTO.ProductionPhaseQualityCreateDTO;
import com.enigcode.frozen_backend.production_phases_qualities.DTO.ProductionPhaseQualityResponseDTO;
import com.enigcode.frozen_backend.production_phases_qualities.DTO.ProductionPhaseQualityUpdateDTO;
import com.enigcode.frozen_backend.production_phases_qualities.service.ProductionPhaseQualityService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/production-phases-qualities")
@RequiredArgsConstructor
public class ProductionPhaseQualityController {
    private final ProductionPhaseQualityService productionPhaseQualityService;

    @Operation(
            summary = "Crear calidad de fase",
            description = "Completa un parámetro de calidad para una fase en especifico")
    @PostMapping
    @PreAuthorize("hasRole('OPERARIO_DE_CALIDAD')")
    public ResponseEntity<ProductionPhaseQualityResponseDTO> createProductionPhaseQuality(
            @Valid @RequestBody ProductionPhaseQualityCreateDTO dto){
        ProductionPhaseQualityResponseDTO responseDTO = productionPhaseQualityService.createProductionPhaseQuality(dto);

        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Modificar calidad de fase",
            description = "Modificar un parámetro de calidad para una fase en especifico")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('OPERARIO_DE_CALIDAD')")
    public ResponseEntity<ProductionPhaseQualityResponseDTO> updateProductionPhaseQuality(
            @PathVariable Long id,
            @Valid @RequestBody ProductionPhaseQualityUpdateDTO dto){
        ProductionPhaseQualityResponseDTO responseDTO = productionPhaseQualityService.updateProductionPhaseQuality(id,dto);

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @Operation(
            summary = "Ver calidad de una fase",
            description = "Ver calidad especifica de una fase")
    @GetMapping("/{id}")
    public ResponseEntity<ProductionPhaseQualityResponseDTO> getProductionPhaseQuality(@PathVariable Long id){
        ProductionPhaseQualityResponseDTO dto = productionPhaseQualityService.getProductionPhaseQuality(id);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(
            summary = "Ver todos los parámetros de calidad de una fase",
            description = "Ver todos los parámetros de calidad de una fase en especifico")
    @GetMapping("/by-phase/{id}")
    public ResponseEntity<List<ProductionPhaseQualityResponseDTO>> getProductionPhaseQualityByPhase(
            @PathVariable Long id){
        List<ProductionPhaseQualityResponseDTO> dto = productionPhaseQualityService.getProductionPhaseQualityByPhase(id);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(
            summary = "Ver todos los parámetros de calidad de un lote",
            description = "Ver todos los parámetros de calidad de un lote")
    @GetMapping("/by-batch/{id}")
    public ResponseEntity<List<ProductionPhaseQualityResponseDTO>> getProductionPhaseQualityByBatch(
            @PathVariable Long id){
        List<ProductionPhaseQualityResponseDTO> dto = productionPhaseQualityService.getProductionPhaseQualityByBatch(id);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
}
