package com.enigcode.frozen_backend.materials.service;

import com.enigcode.frozen_backend.materials.DTO.MaterialResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.enigcode.frozen_backend.materials.DTO.MaterialCreateDTO;
import com.enigcode.frozen_backend.materials.DTO.MaterialFilterDTO;

public interface MaterialService {

    Page<MaterialResponseDTO> findAll(MaterialFilterDTO filterDTO, Pageable pageable);

    MaterialResponseDTO saveMaterial(MaterialCreateDTO materialCreateDTO)
}
