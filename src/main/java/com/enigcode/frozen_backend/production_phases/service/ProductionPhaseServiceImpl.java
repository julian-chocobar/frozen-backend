package com.enigcode.frozen_backend.production_phases.service;

import com.enigcode.frozen_backend.production_phases.mapper.ProductionPhaseMapper;
import com.enigcode.frozen_backend.production_phases.repository.ProductionPhaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductionPhaseServiceImpl implements ProductionPhaseService{
    private final ProductionPhaseRepository productionPhaseRepository;
    private final ProductionPhaseMapper productionPhaseMapper;
}
