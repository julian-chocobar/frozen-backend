package com.enigcode.frozen_backend.production_phases_qualities.controller;

import com.enigcode.frozen_backend.production_phases_qualities.service.ProductionPhaseQualityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/production-phases-qualities")
@RequiredArgsConstructor
public class ProductionPhaseQualityController {
    private final ProductionPhaseQualityService productionPhaseQualityService;
}
