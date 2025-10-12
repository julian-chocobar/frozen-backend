package com.enigcode.frozen_backend.materials.service;

import com.enigcode.frozen_backend.materials.DTO.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MaterialService {
    MaterialResponseDTO saveMaterial(MaterialCreateDTO materialCreateDTO);
    MaterialResponseDTO updateMaterial(Long id, MaterialUpdateDTO materialUpdateDTO);
    MaterialResponseDTO toggleActive(Long id);
    Page<MaterialResponseDTO> findAll(MaterialFilterDTO filterDTO, Pageable pageable);
    MaterialDetailDTO getMaterials(Long id);
}
