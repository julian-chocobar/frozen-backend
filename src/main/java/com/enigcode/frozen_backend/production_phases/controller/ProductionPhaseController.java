package com.enigcode.frozen_backend.production_phases.controller;

import com.enigcode.frozen_backend.production_phases.service.ProductionPhaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/production-phases")
public class ProductionPhaseController {
    private final ProductionPhaseService productionPhaseService;
}
