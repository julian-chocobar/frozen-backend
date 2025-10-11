package com.enigcode.frozen_backend.materials.service;

import lombok.RequiredArgsConstructor;
import com.enigcode.frozen_backend.materials.DTO.MaterialResponseDTO;
import com.enigcode.frozen_backend.materials.mapper.MaterialMapper;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.enigcode.frozen_backend.materials.specification.MaterialSpecification;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.DTO.MaterialFilterDTO;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService{

    final MaterialRepository materialRepository;
    final MaterialMapper materialMapper;

    @Override
    public Page<MaterialResponseDTO> findAll(MaterialFilterDTO filterDTO, Pageable pageable) {
        Pageable pageRequest = PageRequest.of(
                pageable.getPageNumber(), 
                pageable.getPageSize(),
                pageable.getSort());
        Page<Material> materials = materialRepository.findAll(
                MaterialSpecification.createFilter(filterDTO), pageRequest);
        return materials.map(materialMapper::toDto);
    }
}
