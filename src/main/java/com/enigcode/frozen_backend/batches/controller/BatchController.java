package com.enigcode.frozen_backend.batches.controller;

import com.enigcode.frozen_backend.batches.DTO.BatchResponseDTO;
import com.enigcode.frozen_backend.batches.service.BatchService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/batches")
@RequiredArgsConstructor
public class BatchController {

    final BatchService batchService;

    @Operation(
            summary = "Recibir informacion sobre los lotes",
            description = "Devuelve la informacion sobre un lote especificado segun id"
    )
    @GetMapping("/{id}")
    public ResponseEntity<BatchResponseDTO> getBatch(@PathVariable Long id){
        BatchResponseDTO dto = batchService.getBatch(id);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
}
