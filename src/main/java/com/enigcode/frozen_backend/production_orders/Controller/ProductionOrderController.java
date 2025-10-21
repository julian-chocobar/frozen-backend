package com.enigcode.frozen_backend.production_orders.Controller;

import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderFilterDTO;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderResponseDTO;
import com.enigcode.frozen_backend.production_orders.Model.OrderStatus;
import com.enigcode.frozen_backend.production_orders.Service.ProductionOrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/production-orders")
@RequiredArgsConstructor
public class ProductionOrderController {

        final ProductionOrderService productionOrderService;

        @Operation(summary = "Creación de orden de producción", description = "Se crea una nueva orden de produccion pendiente, la cual crea su lote asociado,queda a"
                        +
                        " espera de aprobación o rechazo")
        @PostMapping
        public ResponseEntity<ProductionOrderResponseDTO> createProductionOrder(
                        @Valid @RequestBody ProductionOrderCreateDTO productionOrderCreateDTO) {
                ProductionOrderResponseDTO productionOrderResponseDTO = productionOrderService
                                .createProductionOrder(productionOrderCreateDTO);

                return new ResponseEntity<>(productionOrderResponseDTO, HttpStatus.CREATED);
        }

        @Operation(summary = "Aprobación de orden de produccion", description = "Un usuario con cargo determinado puede aprobar una orden de produccion")
        @PatchMapping("/{id}/approve")
        public ResponseEntity<ProductionOrderResponseDTO> approveOrder(@PathVariable Long id) {
                ProductionOrderResponseDTO dto = productionOrderService.approveOrder(id);

                return new ResponseEntity<>(dto, HttpStatus.OK);
        }

        @Operation(summary = "Cancelación de orden de produccion", description = "Se puede cancelar una orden de produccion en caso de que se quiera echar marcha atras antes"
                        +
                        "de ser aprobada o rechazada")
        @PatchMapping("/{id}/cancel")
        public ResponseEntity<ProductionOrderResponseDTO> cancelOrder(@PathVariable Long id) {
                ProductionOrderResponseDTO dto = productionOrderService.returnOrder(id, OrderStatus.CANCELADA);

                return new ResponseEntity<>(dto, HttpStatus.OK);
        }

        @Operation(summary = "Rechazo de orden de produccion", description = "Se puede rechazar una orden de produccion si tenes un rol especifico")
        @PatchMapping("/{id}/reject")
        public ResponseEntity<ProductionOrderResponseDTO> rejectOrder(@PathVariable Long id) {
                ProductionOrderResponseDTO dto = productionOrderService.returnOrder(id, OrderStatus.RECHAZADA);

                return new ResponseEntity<>(dto, HttpStatus.OK);
        }

        @Operation(summary = "Obtener movimientos", description = "Obtiene todos los movimientos con paginación y filtros")
        @GetMapping
        public ResponseEntity<Map<String, Object>> getProductionOrders(
                        ProductionOrderFilterDTO filterDTO,
                        @PageableDefault(size = 10, sort = "creationDate", direction = Sort.Direction.DESC) Pageable pageable) {

                Page<ProductionOrderResponseDTO> pageResponse = productionOrderService.findAll(filterDTO, pageable);

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

        @Operation(summary = "Recibir informacion de una orden de produccion", description = "Devuelve los datos de una ordend de produccion especificada por id")
        @GetMapping("/{id}")
        public ResponseEntity<ProductionOrderResponseDTO> getProductionOrder(@PathVariable Long id) {
                ProductionOrderResponseDTO dto = productionOrderService.getProductionOrder(id);

                return new ResponseEntity<>(dto, HttpStatus.OK);
        }
}
