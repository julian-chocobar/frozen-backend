package com.enigcode.frozen_backend.production_phases.controller;

import com.enigcode.frozen_backend.production_phases.DTO.ProductionPhaseResponseDTO;
import com.enigcode.frozen_backend.production_phases.DTO.ProductionPhaseUnderReviewDTO;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import com.enigcode.frozen_backend.production_phases.service.ProductionPhaseService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/production-phases")
public class ProductionPhaseController {
    private final ProductionPhaseService productionPhaseService;

    @Operation(
            summary = "Enviar a revision lote después de la fase",
            description = "El supervisor de produccion del sector envía su fase a ser revisada por calidad"
    )
    @PatchMapping("/set-under-review/{id}")
    @PreAuthorize("@securityService.isSupervisorOfPhase(authentication, #id)")
    public ResponseEntity<ProductionPhaseResponseDTO> setUnderReview(
            @PathVariable Long id,
            @Valid @RequestBody ProductionPhaseUnderReviewDTO dto){
        ProductionPhaseResponseDTO productionPhaseResponseDTO = productionPhaseService.setUnderReview(id,dto);

        return new ResponseEntity<>(productionPhaseResponseDTO, HttpStatus.OK);
    }

    @Operation(
            summary = "Revision de parámetros de calidad",
            description = "Revisa los parámetros de calidad y puede aprobar y completar la fase, rechazarla y cancelarla" +
                    "o rechazarla y enviarla a ajustar dependiendo los quality parameters"
    )
    @PatchMapping("/review/{id}")
    @PreAuthorize("hasRole('SUPERVISOR_DE_CALIDAD')")
    public ResponseEntity<ProductionPhaseResponseDTO> reviewProductionPhase(@PathVariable Long id){
        ProductionPhaseResponseDTO dto = productionPhaseService.reviewProductionPhase(id);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(
            summary = "Ver una production phase",
            description = "Ver una production phase en especifico segun id")
    @GetMapping("/{id}")
    public ResponseEntity<ProductionPhaseResponseDTO> getProductionPhase(@PathVariable Long id){
        ProductionPhaseResponseDTO productionPhaseResponseDTO = productionPhaseService.getProductionPhase(id);

        return new ResponseEntity<>(productionPhaseResponseDTO, HttpStatus.OK);
    }

    @Operation(
            summary = "Ver production phase por batch",
            description = "Ver una lista de production phase segun id del batch")
    @GetMapping("/by-batch/{id}")
    public ResponseEntity<List<ProductionPhaseResponseDTO>> getProductionPhasesByBatch(@PathVariable Long id){
        List<ProductionPhaseResponseDTO> productionPhaseResponseDTOs= productionPhaseService.getProductionPhasesByBatch(id);

        return new ResponseEntity<>(productionPhaseResponseDTOs, HttpStatus.OK);
    }
}
