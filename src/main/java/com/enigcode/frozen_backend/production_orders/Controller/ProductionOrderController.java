package com.enigcode.frozen_backend.production_orders.Controller;

import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderResponseDTO;
import com.enigcode.frozen_backend.production_orders.Service.ProductionOrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/production-orders")
@RequiredArgsConstructor
public class ProductionOrderController {

    final ProductionOrderService productionOrderService;

    @Operation(
            summary = "Creación de orden de producción",
            description = "Se crea una nueva orden de produccion pendiente, la cual crea su lote asociado,queda a" +
                    " espera de aprobación o rechazo"
    )
    @PostMapping
    public ResponseEntity<ProductionOrderResponseDTO> createProductionOrder(
            @Valid @RequestBody ProductionOrderCreateDTO productionOrderCreateDTO){
        ProductionOrderResponseDTO productionOrderResponseDTO =
                productionOrderService.createProductionOrder(productionOrderCreateDTO);

        return new ResponseEntity<>(productionOrderResponseDTO, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Aprobación de orden de produccion",
            description = "Un usuario con cargo determinado puede aprobar una orden de produccion"
    )
    @PatchMapping("/{id}/approve-order")
    public ResponseEntity<ProductionOrderResponseDTO> approveOrder(@PathVariable Long id){
        ProductionOrderResponseDTO dto = productionOrderService.approveOrder(id);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
}
