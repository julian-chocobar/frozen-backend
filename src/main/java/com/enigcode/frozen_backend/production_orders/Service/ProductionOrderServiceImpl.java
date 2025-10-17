package com.enigcode.frozen_backend.production_orders.Service;

import com.enigcode.frozen_backend.production_orders.Mapper.ProductionOrderMapper;
import com.enigcode.frozen_backend.production_orders.Repository.ProductionOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductionOrderServiceImpl implements ProductionOrderService{

    final ProductionOrderMapper productionOrderMapper;
    final ProductionOrderRepository productionOrderRepository;
}
