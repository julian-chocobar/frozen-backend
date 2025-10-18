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
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ProductionOrderResponseDTO> approveOrder(@PathVariable Long id){
        ProductionOrderResponseDTO dto = productionOrderService.approveOrder(id);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(
            summary = "Cancelación de orden de produccion",
            description = "Se puede cancelar una orden de produccion en caso de que se quiera echar marcha atras antes" +
                    "de ser aprobada o rechazada"
    )
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ProductionOrderResponseDTO> cancelOrder(@PathVariable Long id){
        ProductionOrderResponseDTO dto = productionOrderService.cancelOrder(id);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
}
