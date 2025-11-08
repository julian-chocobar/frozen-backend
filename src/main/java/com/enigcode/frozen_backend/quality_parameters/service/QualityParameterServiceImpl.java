package com.enigcode.frozen_backend.quality_parameters.service;

import com.enigcode.frozen_backend.quality_parameters.mapper.QualityParameterMapper;
import com.enigcode.frozen_backend.quality_parameters.repository.QualityParameterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QualityParameterServiceImpl implements QualityParameterService{
    private final QualityParameterMapper qualityParameterMapper;
    private final QualityParameterRepository qualityParameterRepository;
}
