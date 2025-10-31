package com.enigcode.frozen_backend.products.controller;

import com.enigcode.frozen_backend.products.DTO.ProductCreateDTO;
import com.enigcode.frozen_backend.products.DTO.ProductResponseDTO;
import com.enigcode.frozen_backend.products.DTO.ProductSimpleDTO;
import com.enigcode.frozen_backend.products.DTO.ProductUpdateDTO;
import com.enigcode.frozen_backend.products.DTO.ProductFilterDTO;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    final ProductService productService;

    @Operation(summary = "Crear un producto", description = "Crea un producto no listo para orden, se le asignan fases pero requieren completarse")
    @PostMapping
    @PreAuthorize("hasRole('SUPERVISOR_DE_PRODUCCION')")
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductCreateDTO productCreateDTO) {
        ProductResponseDTO productResponseDTO = productService.createProduct(productCreateDTO);

        return new ResponseEntity<>(productResponseDTO, HttpStatus.CREATED);
    }

    @Operation(summary = "Marcar producto como Listo", description = "Cambia el estado del producto como ready haciendo que este disponible para produccion")
    @PatchMapping("/{id}/toogle-ready")
    @PreAuthorize("hasRole('SUPERVISOR_DE_PRODUCCION')")
    public ResponseEntity<ProductResponseDTO> toogleReady(@PathVariable Long id) {
        ProductResponseDTO productResponseDTO = productService.toggleReady(id);
        return new ResponseEntity<>(productResponseDTO, HttpStatus.OK);
    }

    @Operation(summary = "Alternar estado producto", description = "Alternar estado producto al contrario (activo, inactivo)")
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('SUPERVISOR_DE_PRODUCCION')")
    public ResponseEntity<ProductResponseDTO> toggleProduct(@PathVariable Long id) {
        ProductResponseDTO productResponseDTO = productService.toggleActive(id);

        return new ResponseEntity<>(productResponseDTO, HttpStatus.OK);
    }

    @Operation(summary = "Modificar producto", description = "Modificar ciertos cambios de un producto especifico")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SUPERVISOR_DE_PRODUCCION')")
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

    @Operation(summary = "Lista de productos (id y nombre)", description = "Devuelve una lista con el id y nombre de todos los productos")
    @GetMapping("/id-name-list")
    public ResponseEntity<List<ProductSimpleDTO>> getAllProductSimpleList(
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean ready) {
        // null = todos, true = solo activos, false = solo inactivos
        List<ProductSimpleDTO> list = productService.getProductSimpleList(name, active, ready);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Obtener producto por id", description = "Obtiene una respuesta con la informacion de un producto en especifico")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProduct(@PathVariable Long id) {
        ProductResponseDTO productResponseDTO = productService.getProduct(id);
        return new ResponseEntity<>(productResponseDTO, HttpStatus.OK);
    }

}