package com.enigcode.frozen_backend.product_phases.controller;

import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseResponseDTO;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseUpdateDTO;
import com.enigcode.frozen_backend.product_phases.service.ProductPhaseService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("product-phases")
@RequiredArgsConstructor
public class ProductPhaseController {

    final ProductPhaseService productPhaseService;

    @Operation(
            summary = "Modificar fase de produccion",
            description = "Actualiza campos seleccionados de una fase de produccion"
    )
    @PatchMapping("/{id}")
    public ResponseEntity<ProductPhaseResponseDTO> updateProductPhase(
            @PathVariable Long id,
            @Valid @RequestBody ProductPhaseUpdateDTO productPhaseUpdateDTO) {
        ProductPhaseResponseDTO productPhaseResponseDTO = productPhaseService.updateProductPhase(id, productPhaseUpdateDTO);
        return new ResponseEntity<>(productPhaseResponseDTO, HttpStatus.OK);
    }

    @Operation(
            summary = "Obtener fases de produccion",
            description = "Obtiene todas las fases de produccion con paginacion"
    )
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProductPhases(
            @PageableDefault(size = 10, sort = "creationDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductPhaseResponseDTO> pageResponse = productPhaseService.findAll(pageable);

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
            summary = "Obtener detalle de una fase",
            description = "Obtiene la informacion detallada de una fase de produccion especifica"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ProductPhaseResponseDTO> getProductPhase(@PathVariable Long id) {
        ProductPhaseResponseDTO productPhaseResponseDTO = productPhaseService.getProductPhase(id);
        return new ResponseEntity<>(productPhaseResponseDTO, HttpStatus.OK);
    }

    @Operation(
            summary = "Obtener fases por producto",
            description = "Obtiene todas las fases de produccion asociadas a un producto especifico"
    )
    @GetMapping("/by-product/{productId}")
    public ResponseEntity<List<ProductPhaseResponseDTO>> getProductPhasesByProduct(@PathVariable Long productId) {
        List<ProductPhaseResponseDTO> phases = productPhaseService.getByProduct(productId);
        return ResponseEntity.ok(phases);
    }
}
