package com.enigcode.frozen_backend.batches.controller;

import com.enigcode.frozen_backend.batches.DTO.BatchFilterDTO;
import com.enigcode.frozen_backend.batches.DTO.BatchResponseDTO;
import com.enigcode.frozen_backend.batches.service.BatchService;
import com.enigcode.frozen_backend.batches.service.BatchTraceabilityService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/batches")
@RequiredArgsConstructor
public class BatchController {

        final BatchService batchService;
        final BatchTraceabilityService batchTraceabilityService;

        @Operation(summary = "Cancelar un lote en especifico", description = "Se cancela la producción de un lote dado por id")
        @PatchMapping("/cancel-batch/{id}")
        @PreAuthorize("hasRole('GERENTE_DE_PLANTA')")
        public ResponseEntity<BatchResponseDTO> cancelBatch(@PathVariable Long id) {
                BatchResponseDTO dto = batchService.cancelBatch(id);

                return new ResponseEntity<>(dto, HttpStatus.OK);
        }

        @Operation(summary = "Recibir informacion sobre los lotes", description = "Devuelve la informacion sobre un lote especificado segun id")
        @GetMapping("/{id}")
        @PreAuthorize("hasRole('OPERARIO_DE_PRODUCCION') or hasRole('SUPERVISOR_DE_PRODUCCION') or hasRole('GERENTE_DE_PLANTA')"
                        + " or hasRole('SUPERVISOR_DE_CALIDAD') or hasRole('OPERARIO_DE_CALIDAD')")
        public ResponseEntity<BatchResponseDTO> getBatch(@PathVariable Long id) {
                BatchResponseDTO dto = batchService.getBatch(id);

                return new ResponseEntity<>(dto, HttpStatus.OK);
        }

        @Operation(summary = "Obtener lotes", description = "Obtiene todos los lotes con paginación y filtros")
        @GetMapping
        @PreAuthorize("hasRole('OPERARIO_DE_PRODUCCION') or hasRole('SUPERVISOR_DE_PRODUCCION') or hasRole('GERENTE_DE_PLANTA')"
                        + " or hasRole('SUPERVISOR_DE_CALIDAD') or hasRole('OPERARIO_DE_CALIDAD')")
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

        @Operation(summary = "Procesar lotes programados para hoy", description = "Ejecuta manualmente el proceso que inicia los lotes planificados para la fecha actual. Útil para pruebas.")
        @PostMapping("/process-today")
        @PreAuthorize("hasRole('GERENTE_DE_PLANTA')")
        public ResponseEntity<Void> processBatchesForToday() {
                batchService.processBatchesForToday();
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Descargar PDF de trazabilidad de lote", description = "Genera y descarga un PDF completo con toda la información de trazabilidad del lote: fases, materiales, usuarios, parámetros de calidad, etc.")
        @GetMapping("/{id}/traceability-pdf")
        @PreAuthorize("hasRole('SUPERVISOR_DE_PRODUCCION') or hasRole('GERENTE_DE_PLANTA') or hasRole('SUPERVISOR_DE_CALIDAD')")
        public ResponseEntity<byte[]> downloadTraceabilityPDF(@PathVariable Long id) {
                byte[] pdfContent = batchTraceabilityService.generateTraceabilityPDF(id);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", "trazabilidad_lote_" + id + ".pdf");
                headers.setContentLength(pdfContent.length);

                return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        }

}
