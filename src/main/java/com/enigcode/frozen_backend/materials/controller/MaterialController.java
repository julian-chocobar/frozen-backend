package com.enigcode.frozen_backend.materials.controller;

import com.enigcode.frozen_backend.materials.DTO.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import com.enigcode.frozen_backend.materials.service.MaterialService;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        public ResponseEntity<MaterialResponseDTO> createMaterial(
                        @Valid @RequestBody MaterialCreateDTO materialCreateDTO) {
                MaterialResponseDTO materialResponseDTO = materialService.saveMaterial(materialCreateDTO);
                return new ResponseEntity<>(materialResponseDTO, HttpStatus.CREATED);
        }

        @Operation(summary = "Modificación de material", description = "Permite modificar ciertos campos de un material registrado")
        @PatchMapping("/{id}")
        public ResponseEntity<MaterialResponseDTO> updateMaterial(@PathVariable Long id,
                        @Valid @RequestBody MaterialUpdateDTO materialUpdateDTO) {
                MaterialResponseDTO materialResponseDTO = materialService.updateMaterial(id, materialUpdateDTO);

                return new ResponseEntity<>(materialResponseDTO, HttpStatus.OK);
        }

        @Operation(summary = "Cambiar estado material", description = "Cambia el estado del material al contrario (activo, inactivo)")
        @PatchMapping("/{id}/toggle-active")
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
        public ResponseEntity<List<MaterialSimpleResponseDTO>> getMaterialIdNameList() {
                List<MaterialSimpleResponseDTO> list = materialService.getMaterialSimpleList();
                return ResponseEntity.ok(list);
        }

        @Operation(summary = "Obtiene detalle material", description = "Obtiene una respuesta mas detallada sobre la informacion de un material en especifico")
        @GetMapping("/{id}")
        public ResponseEntity<MaterialDetailDTO> getMaterial(@PathVariable Long id) {
                MaterialDetailDTO materialDetailDTO = materialService.getMaterial(id);

                return new ResponseEntity<>(materialDetailDTO, HttpStatus.OK);
        }

}
