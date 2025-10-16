package com.enigcode.frozen_backend.products.controller;

import com.enigcode.frozen_backend.products.DTO.ProductCreateDTO;
import com.enigcode.frozen_backend.products.DTO.ProductResponseDTO;
import com.enigcode.frozen_backend.products.DTO.ProductUpdateDTO;
import com.enigcode.frozen_backend.products.DTO.ProductFilterDTO;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.enigcode.frozen_backend.products.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    final ProductService productService;

    @Operation(summary = "Crear un producto", description = "Crea un producto no listo para orden, se le asignan fases pero requieren completarse")
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductCreateDTO productCreateDTO) {
        ProductResponseDTO productResponseDTO = productService.createProduct(productCreateDTO);

        return new ResponseEntity<>(productResponseDTO, HttpStatus.OK);
    }

    @Operation(summary = "Marcar producto como Listo", description = "Cambia el estado del producto como ready haciendo que este disponible para produccion")
    @PatchMapping("/{id}/mark-ready")
    public ResponseEntity<ProductResponseDTO> markAsReady(@PathVariable Long id) {
        ProductResponseDTO productResponseDTO = productService.markAsReady(id);

        return new ResponseEntity<>(productResponseDTO, HttpStatus.OK);
    }

    @Operation(summary = "Alternar estado producto", description = "Alternar estado producto al contrario (activo, inactivo)")
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ProductResponseDTO> toggleProduct(@PathVariable Long id) {
        ProductResponseDTO productResponseDTO = productService.toggleActive(id);

        return new ResponseEntity<>(productResponseDTO, HttpStatus.OK);
    }

    @Operation(summary = "Modificar producto", description = "Modificar ciertos cambios de un producto especifico")
    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id,
            @Valid @RequestBody ProductUpdateDTO productUpdateDTO) {
        ProductResponseDTO productResponseDTO = productService.updateProduct(id, productUpdateDTO);

        return new ResponseEntity<>(productResponseDTO, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener productos", description = "Obtiene todos los productos con paginación y filtros")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts(
            ProductFilterDTO filterDTO,
            @PageableDefault(size = 10, sort = "creationDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ProductResponseDTO> pageResponse = productService.findAll(filterDTO, pageable);

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