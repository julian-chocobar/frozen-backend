package com.enigcode.frozen_backend.production_orders.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderFilterDTO;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderResponseDTO;
import com.enigcode.frozen_backend.production_orders.Model.OrderStatus;


public interface ProductionOrderService {
    ProductionOrderResponseDTO createProductionOrder(ProductionOrderCreateDTO productionOrderCreateDTO);
    ProductionOrderResponseDTO approveOrder(Long id);
    ProductionOrderResponseDTO returnOrder(Long id, OrderStatus orderStatus);
    Page<ProductionOrderResponseDTO> findAll(ProductionOrderFilterDTO filterDTO, Pageable pageable);
    ProductionOrderResponseDTO getProductionOrder(Long id);
}
