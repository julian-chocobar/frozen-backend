package com.enigcode.frozen_backend.production_orders.Controller;

import com.enigcode.frozen_backend.production_orders.Service.ProductionOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/production-orders")
@RequiredArgsConstructor
public class ProductionOrderController {

    final ProductionOrderService productionOrderService;
}
