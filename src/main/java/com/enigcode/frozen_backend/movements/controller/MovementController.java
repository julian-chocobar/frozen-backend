package com.enigcode.frozen_backend.movements.controller;

import com.enigcode.frozen_backend.movements.DTO.MovementCreateDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementDetailDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementResponseDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementFilterDTO;
import com.enigcode.frozen_backend.movements.service.MovementService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/movements")
@RequiredArgsConstructor
public class MovementController {

    final MovementService movementService;

    @Operation(summary = "Registrar movimiento", description = "Registra un nuevo movimiento relacionado con un material, restando o sumando el stock del mismo")
    @PostMapping
    public ResponseEntity<MovementResponseDTO> createMovement(
            @Valid @RequestBody MovementCreateDTO movementCreateDTO) {
        MovementResponseDTO movementResponseDTO = movementService.createMovement(movementCreateDTO);

        return new ResponseEntity<>(movementResponseDTO, HttpStatus.CREATED);
    }

    @Operation(summary = "Mostrar movimiento especifico", description = "informacion detallada de un movimiento especifico")
    @GetMapping("/{id}")
    public ResponseEntity<MovementDetailDTO> getMovement(@PathVariable Long id) {
        MovementDetailDTO movementDetailDTO = movementService.getMovement(id);

        return new ResponseEntity<>(movementDetailDTO, HttpStatus.OK);
    }

    @Operation(summary = "Obtener movimientos", description = "Obtiene todos los movimientos con paginación y filtros")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMovements(
            MovementFilterDTO filterDTO,
            @PageableDefault(size = 10, sort = "creationDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MovementResponseDTO> pageResponse = movementService.findAll(filterDTO, pageable);

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

    @Operation(summary = "Marcar movimiento como en proceso", description = "Marca un movimiento pendiente como en proceso. Solo para operarios de almacén.")
    @PatchMapping("/{id}/in-progress")
    public ResponseEntity<MovementResponseDTO> toggleInProgressPending(@PathVariable Long id) {
        MovementResponseDTO inProgressMovement = movementService.toggleInProgressPending(id);
        return ResponseEntity.ok(inProgressMovement);
    }

    @Operation(summary = "Completar movimiento pendiente", description = "Completa un movimiento pendiente ejecutando el cambio de stock. Solo para operarios de almacén.")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<MovementResponseDTO> completeMovement(@PathVariable Long id) {
        MovementResponseDTO completedMovement = movementService.completeMovement(id);
        return ResponseEntity.ok(completedMovement);
    }

}
