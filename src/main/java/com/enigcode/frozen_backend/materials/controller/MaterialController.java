package com.enigcode.frozen_backend.materials.controller;

import lombok.RequiredArgsConstructor;
import com.enigcode.frozen_backend.materials.DTO.MaterialResponseDTO;
import com.enigcode.frozen_backend.materials.service.MaterialService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.enigcode.frozen_backend.materials.DTO.MaterialCreateDTO;
import com.enigcode.frozen_backend.materials.DTO.MaterialFilterDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/materials")
@RequiredArgsConstructor
public class MaterialController {

    final MaterialService materialService;

    @Operation(summary="Registrar un nuevo material en la base de datos")
    @PostMapping
    public ResponseEntity<MaterialResponseDTO> createMaterial(
            @Valid @RequestBody MaterialCreateDTO materialCreateDTO
    ){
        MaterialResponseDTO materialResponseDTO = materialService.saveMaterial(materialCreateDTO);
        return new ResponseEntity<>(materialResponseDTO,HttpStatus.CREATED);
    }

    /**
     * Busca materiales con paginación y filtros.
     * 
     * @param filterDTO  Criterios de filtrado (opcional)
     * @param pageable Información de paginación:
     *                 - page: número de página (0-based)
     *                 - size: tamaño de página
     *                 - sort: ordenamiento (ej: creationDate,desc)
     * @return Página de materiales y metadata
     */
    @GetMapping
    @Operation(summary = "Obtener materiales", description = "Obtiene todos los materiales con paginación y filtros")
    public ResponseEntity<Map<String, Object>> getMaterials(
            MaterialFilterDTO filterDTO,
            @PageableDefault(size = 10, sort = "creationDate,desc") Pageable pageable) {

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

}
