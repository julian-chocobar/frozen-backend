package com.enigcode.frozen_backend.materials.service;


import lombok.RequiredArgsConstructor;
import com.enigcode.frozen_backend.materials.DTO.MaterialResponseDTO;
import com.enigcode.frozen_backend.materials.mapper.MaterialMapper;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.enigcode.frozen_backend.materials.specification.MaterialSpecification;

import jakarta.transaction.Transactional;

import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.DTO.MaterialCreateDTO;
import com.enigcode.frozen_backend.materials.DTO.MaterialFilterDTO;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService{

    final MaterialRepository materialRepository;
    final MaterialMapper materialMapper;

    /**
     * Le asigna un codigo segun el tipo de material y la fecha de creacion
     * para guardarlo en la base de datos
     *
     * @param materialCreateDTO
     * @return el material guardado en base de datos
     *
     */
    @Override
    @Transactional
    public MaterialResponseDTO saveMaterial(MaterialCreateDTO materialCreateDTO) {
        Material material = materialMapper.toEntity(materialCreateDTO);
        material.setCreationDate(OffsetDateTime.now());

        Material savedMaterial = materialRepository.save(material);
        String code = this.generateCode(savedMaterial.getType(), savedMaterial.getId());
        savedMaterial.setCode(code);

        Material finalMaterial = materialRepository.save(savedMaterial);

        return materialMapper.toResponseDto(finalMaterial);
    }

    /**
     * Funcion que genera codigo de materiales
     * @param type
     * @param id
     * @return devuelve el codigo generado
     */
    private String generateCode(MaterialType type, Long id) {
        String prefix = type.name().substring(0, 3).toUpperCase();
        return prefix + "-" + id;
    }

    @Override
    @Transactional
    public MaterialResponseDTO toggleActive(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado con ID: " + id));
        return null;
    }

    /**
     * Busca materiales con paginación y filtros.
     *
     * @param filterDTO  Criterios de filtrado (opcional)
     * @param pageable Información de paginación:
     *                 - page: número de página (0-based)
     *                 - size: tamaño de página
     *                 - sort: ordenamiento (ej: creationDate,desc)
     * @return Página de materiales y metadata
     */
    @Override
    public Page<MaterialResponseDTO> findAll(MaterialFilterDTO filterDTO, Pageable pageable) {
        Pageable pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());
        Page<Material> materials = materialRepository.findAll(
                MaterialSpecification.createFilter(filterDTO), pageRequest);
        return materials.map(materialMapper::toResponseDto);
    }

}
