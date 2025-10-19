package com.enigcode.frozen_backend.materials.service;

import com.enigcode.frozen_backend.materials.DTO.*;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import org.springframework.data.domain.Page;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface MaterialService {
    MaterialResponseDTO createMaterial(MaterialCreateDTO materialCreateDTO);

    MaterialResponseDTO updateMaterial(Long id, MaterialUpdateDTO materialUpdateDTO);

    MaterialResponseDTO toggleActive(Long id);

    Page<MaterialResponseDTO> findAll(MaterialFilterDTO filterDTO, Pageable pageable);

    List<MaterialSimpleResponseDTO> getMaterialSimpleList(String name, Boolean active, Phase phase);

    MaterialDetailDTO getMaterial(Long id);
}
