package com.enigcode.frozen_backend.warehouse.controller;

import com.enigcode.frozen_backend.materials.DTO.*;
import com.enigcode.frozen_backend.warehouse.config.WarehouseConfig;
import com.enigcode.frozen_backend.warehouse.service.WarehouseLayoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/warehouse")
@RequiredArgsConstructor
@Tag(name = "Warehouse", description = "API para gestión del diseño del almacén")
public class WarehouseController {

    private final WarehouseLayoutService warehouseLayoutService;

    @Operation(summary = "Obtener SVG del almacén", description = "Obtiene el diseño SVG del almacén")
    @GetMapping("/layout")
    public ResponseEntity<String> getWarehouseLayout(
            @RequestParam(required = false, defaultValue = "false") Boolean includeMaterials,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false, defaultValue = "normal") String mode) {

        String svg = warehouseLayoutService.getWarehouseSvg();

        // Aplicar estilos según modo
        svg = warehouseLayoutService.appleModeStyles(svg, mode);

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .header("Cache-Control", "max-age=300") // Cache por 5 minutos
                .body(svg);
    }

    @Operation(summary = "Obtener configuración del almacén", description = "Obtiene la configuración de zonas y secciones")
    @GetMapping("/config")
    @PreAuthorize("hasRole('SUPERVISOR_DE_ALMACEN') or hasRole('OPERARIO_DE_ALMACEN')")
    public ResponseEntity<WarehouseConfig> getWarehouseConfig() {
        return ResponseEntity.ok(warehouseLayoutService.getWarehouseConfig());
    }

    @Operation(summary = "Validar ubicación", description = "Valida si una ubicación es válida en el almacén")
    @PostMapping("/validate-location")
    @PreAuthorize("hasRole('SUPERVISOR_DE_ALMACEN') or hasRole('OPERARIO_DE_ALMACEN')")
    public ResponseEntity<LocationValidationDTO> validateLocation(
            @Valid @RequestBody LocationValidationRequestDTO request) {

        boolean isValid = warehouseLayoutService.isValidSection(
                request.getZone(),
                request.getSection());

        com.enigcode.frozen_backend.materials.model.WarehouseCoordinates coords = null;
        if (isValid) {
            coords = warehouseLayoutService.calculateCoordinatesForSection(
                    request.getZone(),
                    request.getSection());
        }

        LocationValidationDTO response = LocationValidationDTO.builder()
                .isValid(isValid)
                .coordinates(coords)
                .message(isValid ? "Ubicación válida" : "Ubicación no válida para la zona especificada")
                .build();

        return ResponseEntity.ok(response);
    }

    // Endpoints para gestión dinámica de zonas y secciones

    @Operation(summary = "Obtener secciones de una zona", description = "Lista todas las secciones disponibles en una zona específica")
    @GetMapping("/zones/{zone}/sections")
    @PreAuthorize("hasRole('SUPERVISOR_DE_ALMACEN') or hasRole('OPERARIO_DE_ALMACEN')")
    public ResponseEntity<com.enigcode.frozen_backend.warehouse.DTO.ZoneSectionsDTO> getZoneSections(
            @PathVariable String zone) {
        com.enigcode.frozen_backend.warehouse.DTO.ZoneSectionsDTO sections = warehouseLayoutService
                .getZoneSections(zone);
        return ResponseEntity.ok(sections);
    }

    @Operation(summary = "Obtener todas las zonas con sus secciones", description = "Lista todas las zonas del almacén con sus secciones")
    @GetMapping("/zones/sections")
    @PreAuthorize("hasRole('SUPERVISOR_DE_ALMACEN') or hasRole('OPERARIO_DE_ALMACEN')")
    public ResponseEntity<java.util.List<com.enigcode.frozen_backend.warehouse.DTO.ZoneSectionsDTO>> getAllZonesSections() {
        java.util.List<com.enigcode.frozen_backend.warehouse.DTO.ZoneSectionsDTO> allZones = warehouseLayoutService
                .getAllZonesSections();
        return ResponseEntity.ok(allZones);
    }

    @Operation(summary = "Actualizar configuración de zona", description = "Modifica la configuración de secciones de una zona específica")
    @PutMapping("/zones/{zone}/config")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR_DE_ALMACEN')")
    public ResponseEntity<com.enigcode.frozen_backend.warehouse.config.ZoneConfig> updateZoneConfig(
            @PathVariable String zone,
            @Valid @RequestBody com.enigcode.frozen_backend.warehouse.DTO.ZoneConfigUpdateDTO zoneConfigUpdate) {

        com.enigcode.frozen_backend.warehouse.config.ZoneConfig updatedConfig = warehouseLayoutService
                .updateZoneConfig(zone, zoneConfigUpdate);
        return ResponseEntity.ok(updatedConfig);
    }

    @Operation(summary = "Regenerar SVG del almacén", description = "Regenera el SVG con la configuración actualizada")
    @PostMapping("/layout/regenerate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR_DE_ALMACEN')")
    public ResponseEntity<String> regenerateLayout() {
        String newSvg = warehouseLayoutService.regenerateSvg();
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .header("Cache-Control", "no-cache") // No cache para SVG regenerado
                .body(newSvg);
    }

    @Operation(summary = "Obtener SVG dinámico", description = "Obtiene el SVG generado dinámicamente basado en la configuración actual")
    @GetMapping("/layout/dynamic")
    public ResponseEntity<String> getDynamicLayout() {
        String svg = warehouseLayoutService.regenerateSvg();
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .header("Cache-Control", "max-age=60") // Cache corto para SVG dinámico
                .body(svg);
    }
}