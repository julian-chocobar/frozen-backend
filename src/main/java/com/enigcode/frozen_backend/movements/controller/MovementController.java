package com.enigcode.frozen_backend.movements.controller;

import com.enigcode.frozen_backend.movements.DTO.MovementCreateDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementDetailDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementResponseDTO;
import com.enigcode.frozen_backend.movements.service.MovementService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/movements")
@RequiredArgsConstructor
public class MovementController {

    final MovementService movementService;

    @Operation(
            summary = "Registrar movimiento",
            description = "Registra un nuevo movimiento relacionado con un material, restando o sumando el stock del mismo")
    @PostMapping
    public ResponseEntity<MovementResponseDTO> createMovement(
            @Valid @RequestBody MovementCreateDTO movementCreateDTO){
        MovementResponseDTO movementResponseDTO = movementService.createMovement(movementCreateDTO);

        return new ResponseEntity<>(movementResponseDTO, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Mostrar movimiento especifico",
            description = "informacion detallada de un movimiento especifico"
    )
    @GetMapping("/{id}")
    public ResponseEntity<MovementDetailDTO> getMovement(@PathVariable Long id){
        MovementDetailDTO movementDetailDTO = movementService.getMovement(id);

        return new ResponseEntity<>(movementDetailDTO, HttpStatus.OK);
    }
}
