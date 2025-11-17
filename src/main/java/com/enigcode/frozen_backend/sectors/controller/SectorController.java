package com.enigcode.frozen_backend.sectors.controller;

import com.enigcode.frozen_backend.sectors.DTO.SectorCreateDTO;
import com.enigcode.frozen_backend.sectors.DTO.SectorResponseDTO;
import com.enigcode.frozen_backend.sectors.DTO.SectorUpdateDTO;
import com.enigcode.frozen_backend.sectors.service.SectorService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sectors")
@RequiredArgsConstructor
public class SectorController {

    final SectorService sectorService;

    @Operation(summary = "Crear un sector", description = "Crea un sector al que se le asigna un supervisor y fase correspondiente dependiendo el tipo")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('GERENTE_DE_PLANTA')")
    public ResponseEntity<SectorResponseDTO> createSector(@Valid @RequestBody SectorCreateDTO sectorCreateDTO) {
        SectorResponseDTO dto = sectorService.createSector(sectorCreateDTO);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @Operation(summary = "Listar todos los sectores", description = "Obtiene una lista paginada de todos los sectores")
    @GetMapping
    public ResponseEntity<Page<SectorResponseDTO>> getAllSectors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SectorResponseDTO> sectors = sectorService.findAll(pageable);
        return ResponseEntity.ok(sectors);
    }

    @Operation(summary = "Ver un sector especifico")
    @GetMapping("/{id}")
    public ResponseEntity<SectorResponseDTO> getSector(@PathVariable Long id) {
        SectorResponseDTO dto = sectorService.getSector(id);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(summary = "Modificar Sector", description = "Modifica un sector en especifico")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GERENTE_DE_PLANTA')")
    public ResponseEntity<SectorResponseDTO> updateSector(@Valid @RequestBody SectorUpdateDTO sectorUpdateDTO,
            @PathVariable Long id) {
        SectorResponseDTO dto = sectorService.updateDTO(sectorUpdateDTO, id);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
}
