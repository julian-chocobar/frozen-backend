package com.enigcode.frozen_backend.warehouse.controller;

import com.enigcode.frozen_backend.materials.DTO.LocationValidationDTO;
import com.enigcode.frozen_backend.materials.DTO.LocationValidationRequestDTO;
import com.enigcode.frozen_backend.materials.model.WarehouseZone;
import com.enigcode.frozen_backend.warehouse.service.WarehouseLayoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/warehouse")
@RequiredArgsConstructor
@Tag(name = "Warehouse", description = "API para gestión simple del almacén")
public class WarehouseController {

        private final WarehouseLayoutService warehouseLayoutService;

        @Operation(summary = "Obtener mapa del almacén", description = "Obtiene el diseño SVG estático del almacén")
        @GetMapping("/layout")
        public ResponseEntity<String> getWarehouseLayout() {
                String svg = warehouseLayoutService.getWarehouseSvg();

                return ResponseEntity.ok()
                                .contentType(MediaType.valueOf("image/svg+xml"))
                                .header("Cache-Control", "max-age=3600") // Cache por 1 hora
                                .body(svg);
        }

        @Operation(summary = "Validar ubicación", description = "Valida si una ubicación es válida en el almacén")
        @PostMapping("/validate-location")
        @PreAuthorize("hasRole('SUPERVISOR_DE_ALMACEN') or hasRole('OPERARIO_DE_ALMACEN')")
        public ResponseEntity<LocationValidationDTO> validateLocation(
                        @Valid @RequestBody LocationValidationRequestDTO request) {

                boolean isValid = warehouseLayoutService.isValidLocation(
                                request.getZone(),
                                request.getSection(),
                                request.getLevel());

                LocationValidationDTO response = LocationValidationDTO.builder()
                                .isValid(isValid)
                                .message(isValid ? "Ubicación válida"
                                                : "Ubicación no válida. Zona: " + request.getZone() +
                                                                ", Sección: " + request.getSection() +
                                                                ", Nivel: " + request.getLevel())
                                .build();

                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Obtener zonas disponibles", description = "Lista todas las zonas del almacén con sus secciones disponibles")
        @GetMapping("/zones")
        @PreAuthorize("hasRole('SUPERVISOR_DE_ALMACEN') or hasRole('OPERARIO_DE_ALMACEN')")
        public ResponseEntity<Map<String, List<String>>> getAvailableZones() {
                Map<String, List<String>> zonesWithSections = new java.util.HashMap<>();

                for (WarehouseZone zone : WarehouseZone.values()) {
                        zonesWithSections.put(zone.name(), zone.getAvailableSections());
                }

                return ResponseEntity.ok(zonesWithSections);
        }
}