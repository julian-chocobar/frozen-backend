package com.enigcode.frozen_backend.materials.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.materials.DTO.*;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import lombok.RequiredArgsConstructor;
import com.enigcode.frozen_backend.materials.mapper.MaterialMapper;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.enigcode.frozen_backend.materials.specification.MaterialSpecification;

import jakarta.transaction.Transactional;

import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {

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
    public MaterialResponseDTO createMaterial(MaterialCreateDTO materialCreateDTO) {
        Material material = materialMapper.toEntity(materialCreateDTO);
        material.setCreationDate(OffsetDateTime.now(ZoneOffset.UTC));
        material.setIsActive(Boolean.TRUE);

        // Asegurar valor por defecto seguro para reservedStock
        if (material.getReservedStock() == null) {
            material.setReservedStock(0.0);
        }

        materialUnitVerification(material);

        Material savedMaterial = materialRepository.save(material);
        String code = this.generateCode(savedMaterial.getType(), savedMaterial.getId());
        savedMaterial.setCode(code);

        Material finalMaterial = materialRepository.saveAndFlush(savedMaterial);

        return materialMapper.toResponseDto(finalMaterial);
    }

    /**
     * Funcion que verifica que un material de tipo ENVASE tenga la unidad
     * correspondiente UNIDAD o
     * que la unidad de medida UNIDAD sea solo asignada a materiales de tipo ENVASE
     * o OTROS
     * 
     * @param material
     */
    private static void materialUnitVerification(Material material) {
        if (material.getType().equals(MaterialType.ENVASE) &&
                !material.getUnitMeasurement().equals(UnitMeasurement.UNIDAD))
            throw new BadRequestException("El material de tipo ENVASE debe tener como unidad de medida UNIDAD");

        if (material.getUnitMeasurement().equals(UnitMeasurement.UNIDAD) &&
                (!material.getType().equals(MaterialType.ENVASE) &&
                        !material.getType().equals(MaterialType.OTROS)))
            throw new BadRequestException("La unida de medida UNIDAD debe tener como material un tipo ENVASE o OTROS");
    }

    /**
     * Funcion que genera codigo de materiales
     * 
     * @param type
     * @param id
     * @return devuelve el codigo generado
     */
    private String generateCode(MaterialType type, Long id) {
        String prefix = type.name().substring(0, 3).toUpperCase();
        return prefix + "-" + id;
    }

    /**
     * Funcion que cambia ciertos parametros de un material preexistente
     * 
     * @param id
     * @param materialUpdateDTO
     * @return MaterialResponseDTO
     */
    @Override
    @Transactional
    public MaterialResponseDTO updateMaterial(Long id, MaterialUpdateDTO materialUpdateDTO) {
        Material originalMaterial = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado con ID: " + id));

        String newCode = null;
        if (!originalMaterial.getType().equals(materialUpdateDTO.getType()))
            newCode = generateCode(materialUpdateDTO.getType(), originalMaterial.getId());

        Material updatedMaterial = materialMapper.partialUpdate(materialUpdateDTO, originalMaterial);
        updatedMaterial.setLastUpdateDate(OffsetDateTime.now(ZoneOffset.UTC));

        if (newCode != null)
            updatedMaterial.setCode(newCode);

        materialUnitVerification(updatedMaterial);

        Material savedUpdatedMaterial = materialRepository.save(updatedMaterial);

        return materialMapper.toResponseDto(savedUpdatedMaterial);
    }

    /**
     * Funcion que cambia el estado de un material,
     * 
     * @param id
     * @return materialResponseDTO
     */
    @Override
    @Transactional
    public MaterialResponseDTO toggleActive(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado con ID: " + id));
        material.toggleActive();

        Material savedMaterial = materialRepository.save(material);

        return materialMapper.toResponseDto(savedMaterial);
    }

    /**
     * Busca materiales con paginación y filtros.
     *
     * @param filterDTO Criterios de filtrado (opcional)
     * @param pageable  Información de paginación:
     *                  - page: número de página (0-based)
     *                  - size: tamaño de página
     *                  - sort: ordenamiento (ej: creationDate,desc)
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

    /**
     * Funcion para mostrar una lista simple de materiales (id y nombre)
     * 
     * @return Lista con id y nombre de todos los materiales
     */
    @Override
    public List<MaterialSimpleResponseDTO> getMaterialSimpleList(String name, Boolean active) {
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }
        String q = name.trim();
        List<Material> results;
        if (active == null) {
            results = materialRepository.findTop10ByNameContainingIgnoreCase(q);
        } else if (active) {
            results = materialRepository.findTop10ByNameContainingIgnoreCaseAndIsActiveTrue(q);
        } else {
            results = materialRepository.findTop10ByNameContainingIgnoreCaseAndIsActiveFalse(q);
        }
        return results.stream()
                .map(m -> new MaterialSimpleResponseDTO(m.getId(), m.getCode(), m.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Funcion para mostrar un material especifico segun id
     * 
     * @param id
     * @return Vista detallada de los elementos del material
     */
    @Override
    public MaterialDetailDTO getMaterial(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado con ID: " + id));

        return materialMapper.toDetailDto(material);
    }
}
