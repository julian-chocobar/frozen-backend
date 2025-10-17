package com.enigcode.frozen_backend.production_orders.Service;

import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.service.BatchService;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderResponseDTO;
import com.enigcode.frozen_backend.production_orders.Mapper.ProductionOrderMapper;
import com.enigcode.frozen_backend.production_orders.Repository.ProductionOrderRepository;
import com.enigcode.frozen_backend.products.model.Product;
import com.enigcode.frozen_backend.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductionOrderServiceImpl implements ProductionOrderService{

    final ProductionOrderMapper productionOrderMapper;
    final ProductRepository productRepository;
    final BatchService batchService;
    final ProductionOrderRepository productionOrderRepository;

    /**
     * Funcion que crea una nueva orden de produccion en estado pendiente junto al lote que le corresponde
     * @param productionOrderCreateDTO
     * @return
     */
    @Override
    public ProductionOrderResponseDTO createProductionOrder(ProductionOrderCreateDTO productionOrderCreateDTO) {
        Product product = productRepository.findById(productionOrderCreateDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró producto con id " + productionOrderCreateDTO.getProductId()));

        return null;
    }
}
