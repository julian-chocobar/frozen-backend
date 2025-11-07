package com.enigcode.frozen_backend.production_materials.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.production_materials.DTO.ProductionMaterialResponseDTO;
import com.enigcode.frozen_backend.production_materials.mapper.ProductionMaterialMapper;
import com.enigcode.frozen_backend.production_materials.model.ProductionMaterial;
import com.enigcode.frozen_backend.production_materials.repository.ProductionMaterialRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionMaterialServiceImpl implements ProductionMaterialService {

    private final ProductionMaterialRepository productionMaterialRepository;
    private final ProductionMaterialMapper productionMaterialMapper;

    @Override
    @Transactional
    public ProductionMaterialResponseDTO getProductionMaterial(Long id) {
        ProductionMaterial productionMaterial = productionMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró production material de id " + id));

        return productionMaterialMapper.toResponseDTO(productionMaterial);
    }

    @Override
    @Transactional
    public List<ProductionMaterialResponseDTO> getProductionMaterialByPhase(Long id) {
        List<ProductionMaterial> productionMaterials = productionMaterialRepository.findAllByProductionPhaseId(id);

        return productionMaterials.stream().map(productionMaterialMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional
    public List<ProductionMaterialResponseDTO> getProductionMaterialByBatch(Long id) {
        List<ProductionMaterial> productionMaterials = productionMaterialRepository.findAllByBatchId(id);

        return productionMaterials.stream().map(productionMaterialMapper::toResponseDTO).toList();
    }
}
