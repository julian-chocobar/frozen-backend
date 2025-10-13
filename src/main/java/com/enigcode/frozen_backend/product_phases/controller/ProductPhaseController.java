package com.enigcode.frozen_backend.product_phases.controller;

import com.enigcode.frozen_backend.product_phases.service.ProductPhaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("product-phases")
@RequiredArgsConstructor
public class ProductPhaseController {

    final ProductPhaseService productPhaseService;
}
