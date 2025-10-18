package com.enigcode.frozen_backend.production_orders.Service;

import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderResponseDTO;


public interface ProductionOrderService {
    ProductionOrderResponseDTO createProductionOrder(ProductionOrderCreateDTO productionOrderCreateDTO);
    ProductionOrderResponseDTO approveOrder(Long id);
    ProductionOrderResponseDTO cancelOrder(Long id);
}
