package com.enigcode.frozen_backend.production_phases.controller;

import com.enigcode.frozen_backend.production_phases.DTO.ProductionPhaseResponseDTO;
import com.enigcode.frozen_backend.production_phases.DTO.ProductionPhaseUnderReviewDTO;
import com.enigcode.frozen_backend.production_phases.service.ProductionPhaseService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/production-phases")
public class ProductionPhaseController {
    private final ProductionPhaseService productionPhaseService;

    @Operation(
            summary = "Enviar a revision lote después de la fase",
            description = "El supervisor de produccion del sector envía su fase a ser revisada por calidad"
    )
    @PatchMapping("/{id}")
    @PreAuthorize("@securityService.isSupervisorOfPhase(authentication, #id)")
    public ResponseEntity<ProductionPhaseResponseDTO> setUnderReview(
            @PathVariable Long id,
            @Valid @RequestBody ProductionPhaseUnderReviewDTO dto){
        ProductionPhaseResponseDTO productionPhaseResponseDTO = productionPhaseService.setUnderReview(id,dto);

        return new ResponseEntity<>(productionPhaseResponseDTO, HttpStatus.OK);
    }
}
