package com.enigcode.frozen_backend.batches.controller;

import com.enigcode.frozen_backend.batches.DTO.BatchFilterDTO;
import com.enigcode.frozen_backend.batches.DTO.BatchResponseDTO;
import com.enigcode.frozen_backend.batches.service.BatchService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    @Operation(summary = "Obtener movimientos", description = "Obtiene todos los movimientos con paginación y filtros")
        @GetMapping
        public ResponseEntity<Map<String, Object>> getBatches(
                BatchFilterDTO filterDTO,
                @PageableDefault(size = 10, sort = "creationDate", direction = Sort.Direction.DESC) Pageable pageable) {

                Page<BatchResponseDTO> pageResponse = batchService.findAll(filterDTO, pageable);
                        
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
