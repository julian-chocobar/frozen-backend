package com.enigcode.frozen_backend.materials.controller;

import com.enigcode.frozen_backend.materials.DTO.*;
import com.enigcode.frozen_backend.materials.model.MaterialType;

import java.util.List;
import lombok.RequiredArgsConstructor;
import com.enigcode.frozen_backend.materials.service.MaterialService;
import com.enigcode.frozen_backend.product_phases.model.Phase;

import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/materials")
@RequiredArgsConstructor
public class MaterialController {

        final MaterialService materialService;

        @Operation(summary = "Registrar  material", description = "Registra un nuevo material en base de datos asignandole un codigo unico")
        @PostMapping
        @PreAuthorize("hasRole('SUPERVISOR_DE_ALMACEN')")
        public ResponseEntity<MaterialResponseDTO> createMaterial(
                        @Valid @RequestBody MaterialCreateDTO materialCreateDTO) {
                MaterialResponseDTO materialResponseDTO = materialService.createMaterial(materialCreateDTO);
                return new ResponseEntity<>(materialResponseDTO, HttpStatus.CREATED);
        }

        @Operation(summary = "Modificación de material", description = "Permite modificar ciertos campos de un material registrado")
        @PatchMapping("/{id}")
        @PreAuthorize("hasRole('SUPERVISOR_DE_ALMACEN')")
        public ResponseEntity<MaterialResponseDTO> updateMaterial(@PathVariable Long id,
                        @Valid @RequestBody MaterialUpdateDTO materialUpdateDTO) {
                MaterialResponseDTO materialResponseDTO = materialService.updateMaterial(id, materialUpdateDTO);

                return new ResponseEntity<>(materialResponseDTO, HttpStatus.OK);
        }

        @Operation(summary = "Cambiar estado material", description = "Cambia el estado del material al contrario (activo, inactivo)")
        @PatchMapping("/{id}/toggle-active")
        @PreAuthorize("hasRole('SUPERVISOR_DE_ALMACEN')")
        public ResponseEntity<MaterialResponseDTO> toggleActive(@PathVariable Long id) {
                MaterialResponseDTO materialResponseDTO = materialService.toggleActive(id);

                return new ResponseEntity<>(materialResponseDTO, HttpStatus.OK);
        }

        @Operation(summary = "Obtener materiales", description = "Obtiene todos los materiales con paginación y filtros")
        @GetMapping
        public ResponseEntity<Map<String, Object>> getMaterials(
                        MaterialFilterDTO filterDTO,
                        @PageableDefault(size = 10, sort = "creationDate", direction = Sort.Direction.DESC) Pageable pageable) {
                Page<MaterialResponseDTO> pageResponse = materialService.findAll(filterDTO, pageable);

                // Metadata de la página para el frontend
                Map<String, Object> response = new HashMap<>();
                response.put("content", pageResponse.getContent());
                response.put("currentPage", pageResponse.getNumber());
                response.put("totalItems", pageResponse.getTotalElements());
                response.put("totalPages", pageResponse.getTotalPages());
                response.put("size", pageResponse.getSize());
                response.put("hasNext", pageResponse.hasNext());
                response.put("hasPrevious", pageResponse.hasPrevious());
                response.put("isFirst", pageResponse.isFirst());
                response.put("isLast", pageResponse.isLast());
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Lista de materiales (id y nombre)", description = "Devuelve una lista con el id y nombre de todos los materiales")
        @GetMapping("/id-name-list")
        public ResponseEntity<List<MaterialSimpleResponseDTO>> getAllMaterialIdNameList(
                        @RequestParam(required = false, defaultValue = "") String name,
                        @RequestParam(required = false) Boolean active,
                        @RequestParam(required = false) Phase phase,
                        @RequestParam(required = false) MaterialType type) {
                // null = todos, true = solo activos, false = solo inactivos
                List<MaterialSimpleResponseDTO> list = materialService.getMaterialSimpleList(name, active, phase, type);
                return ResponseEntity.ok(list);
        }

        @Operation(summary = "Obtiene detalle material", description = "Obtiene una respuesta mas detallada sobre la informacion de un material en especifico")
        @GetMapping("/{id}")
        public ResponseEntity<MaterialDetailDTO> getMaterial(@PathVariable Long id) {
                MaterialDetailDTO materialDetailDTO = materialService.getMaterial(id);

                return new ResponseEntity<>(materialDetailDTO, HttpStatus.OK);
        }

        // Endpoints para funcionalidad de almacén

        @Operation(summary = "Obtener ubicaciones de materiales", description = "Obtiene las ubicaciones de todos los materiales activos para mostrar en el mapa del almacén")
        @GetMapping("/warehouse-map")
        public ResponseEntity<List<MaterialWarehouseLocationDTO>> getWarehouseMap(
                        @RequestParam(required = false) String zone,
                        @RequestParam(required = false, defaultValue = "true") Boolean activeOnly) {
                List<MaterialWarehouseLocationDTO> locations = materialService.getWarehouseLocations(zone, activeOnly);
                return ResponseEntity.ok(locations);
        }

        @Operation(summary = "Actualizar ubicación de material", description = "Actualiza la ubicación física de un material en el almacén")
        @PatchMapping("/{id}/location")
        @PreAuthorize("hasRole('SUPERVISOR_DE_ALMACEN')")
        public ResponseEntity<MaterialResponseDTO> updateMaterialLocation(
                        @PathVariable Long id,
                        @Valid @RequestBody MaterialLocationUpdateDTO locationUpdateDTO) {
                MaterialResponseDTO updatedMaterial = materialService.updateMaterialLocation(id, locationUpdateDTO);
                return ResponseEntity.ok(updatedMaterial);
        }

        @Operation(summary = "Obtener información del almacén", description = "Obtiene zonas disponibles y próxima ubicación sugerida")
        @GetMapping("/warehouse-info")
        public ResponseEntity<WarehouseInfoDTO> getWarehouseInfo(
                        @RequestParam(required = false) MaterialType materialType) {
                WarehouseInfoDTO info = materialService.getWarehouseInfo(materialType);
                return ResponseEntity.ok(info);
        }

}
