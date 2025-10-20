package com.enigcode.frozen_backend.packagings.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.enigcode.frozen_backend.packagings.DTO.PackagingCreateDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingResponseDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingSimpleResponseDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingUpdateDTO;
import com.enigcode.frozen_backend.packagings.service.PackagingService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/packagings")
@RequiredArgsConstructor
public class PackagingController {

    final PackagingService packagingService;

    @Operation(
            summary="Registrar  empaque",
            description = "Registra un nuevo empaque en base de datos")
    @PostMapping
    public ResponseEntity<PackagingResponseDTO> createPackaging(
            @Valid @RequestBody PackagingCreateDTO packagingCreateDTO){
        PackagingResponseDTO packagingResponseDTO = packagingService.createPackaging(packagingCreateDTO);

        return new ResponseEntity<>(packagingResponseDTO,HttpStatus.CREATED);
    }

    @Operation(
            summary = "Cambiar estado empaque",
            description = "Cambia el estado del empaque al contrario (activo, inactivo)")
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<PackagingResponseDTO> toggleActive(@PathVariable Long id){
        PackagingResponseDTO packagingResponseDTO = packagingService.toggleActive(id);
        return new ResponseEntity<>(packagingResponseDTO, HttpStatus.OK);
    }

    @Operation(
            summary = "Obtener empaque",
            description = "Obtiene un empaque a partir de su id"
    )
    @GetMapping("/{id}")
    public ResponseEntity<PackagingResponseDTO> getPackaging(@PathVariable Long id){
        PackagingResponseDTO packagingResponseDTO = packagingService.getPackaging(id);

        return new ResponseEntity<>(packagingResponseDTO,HttpStatus.OK);
    }

    @Operation(
            summary = "Obtener empaques",
            description = "Obtiene todos los empaques con paginación y filtros")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPackagings(
            @PageableDefault(size = 10, sort = "creationDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PackagingResponseDTO> pageResponse = packagingService.findAll(pageable);

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

    @Operation(
        summary = "Obtener lista simple de empaques",
        description = "Obtiene una lista simple con id y nombre de todos los empaques activos"
    )
    @GetMapping("/id-name-list")
    public ResponseEntity<List<PackagingSimpleResponseDTO>> getPackagingList(
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long productId) {
        List<PackagingSimpleResponseDTO> packagings = packagingService.getPackagingList(name, active, productId);
        return ResponseEntity.ok(packagings);
    }


    @Operation(
        summary = "Modificar packaging",
        description = "Modifica un packaging en especifico segun el id "
    )
    @PatchMapping("/{id}")
    public ResponseEntity<PackagingResponseDTO> updatePackaging(@PathVariable Long id,
        @Valid @RequestBody PackagingUpdateDTO packagingUpdateDTO) {
        PackagingResponseDTO packagingResponseDTO = packagingService.updatePackaging(id, packagingUpdateDTO);
        return new ResponseEntity<>(packagingResponseDTO, HttpStatus.OK);
    }
}
