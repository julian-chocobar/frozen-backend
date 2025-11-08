package com.enigcode.frozen_backend.quality_parameters.controller;

import com.enigcode.frozen_backend.quality_parameters.service.QualityParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quality-parameters")
@RequiredArgsConstructor
public class QualityParameterController {
    private final QualityParameterService qualityParameterService;


}
