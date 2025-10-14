package com.enigcode.frozen_backend.products.controller;

import com.enigcode.frozen_backend.products.DTO.ProductCreateDTO;
import com.enigcode.frozen_backend.products.DTO.ProductResponseDTO;
import com.enigcode.frozen_backend.products.DTO.ProductUpdateDTO;
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

    @Operation(
            summary = "Crear un producto",
            description = "Crea un producto no listo para orden, se le asignan fases pero requieren completarse"
    )
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductCreateDTO productCreateDTO){
        ProductResponseDTO productResponseDTO = productService.createProduct(productCreateDTO);

        return new ResponseEntity<>(productResponseDTO, HttpStatus.OK);
    }

    @Operation(
            summary = "Marcar producto como Listo",
            description = "Cambia el estado del producto como ready haciendo que este disponible para produccion"
    )

    @PatchMapping("/{id}/mark-ready")
    public ResponseEntity<ProductResponseDTO> markAsReady(@PathVariable Long id){
        ProductResponseDTO productResponseDTO = productService.markAsReady(id);

        return new ResponseEntity<>(productResponseDTO, HttpStatus.OK);
    }

    @Operation(
            summary = "Alternar estado producto",
            description = "Alternar estado producto al contrario (activo, inactivo)")
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ProductResponseDTO> toggleProduct(@PathVariable Long id){
        ProductResponseDTO productResponseDTO = productService.toggleActive(id);

        return new ResponseEntity<>(productResponseDTO, HttpStatus.OK);
    }

    @Operation(summary = "Modificar producto",
            description = "Modificar ciertos cambios de un producto especifico")
    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id,
                                                            @Valid @RequestBody ProductUpdateDTO productUpdateDTO){
        ProductResponseDTO productResponseDTO = productService.updateProduct(id, productUpdateDTO);

        return new ResponseEntity<>(productResponseDTO, HttpStatus.CREATED);
    }

    @Operation(summary = "Mostrar información producto",
            description = "Muestra información de un producto en especifico")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProduct(@PathVariable Long id){
        ProductResponseDTO productResponseDTO = productService.getProduct(id);

        return new ResponseEntity<>(productResponseDTO,HttpStatus.OK);
    }
}
