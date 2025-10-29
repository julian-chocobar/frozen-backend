package com.enigcode.frozen_backend.packagings.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.enigcode.frozen_backend.packagings.DTO.PackagingCreateDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingResponseDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingSimpleResponseDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingUpdateDTO;
import com.enigcode.frozen_backend.packagings.mapper.PackagingMapper;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.packagings.repository.PackagingRepository;
import com.enigcode.frozen_backend.products.model.Product;
import com.enigcode.frozen_backend.products.repository.ProductRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PackagingServiceImpl implements PackagingService{

    private final PackagingRepository packagingRepository;
    private final PackagingMapper packagingMapper;
    private final MaterialRepository materialRepository;
    private final ProductRepository productRepository;

    /**
     * Crea un nuevo packaging en la base de datos segun DTO
     * @param packagingCreateDTO
     * @return PackagingResponseDTO
     */
    @Override
    @Transactional
    public PackagingResponseDTO createPackaging(@Valid PackagingCreateDTO packagingCreateDTO) {
        Material packagingMaterial = materialRepository.findById(packagingCreateDTO.getPackagingMaterialId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Material no encontrado con id "
                                + packagingCreateDTO.getPackagingMaterialId()));

        Material labelingMaterial = materialRepository.findById(packagingCreateDTO.getLabelingMaterialId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Material no encontrado con id "
                                + packagingCreateDTO.getLabelingMaterialId()));

        if (!packagingMaterial.getType().equals(MaterialType.ENVASE))
            throw new BadRequestException("El tipo de material packaging debe ser " + MaterialType.ENVASE);

        if (!labelingMaterial.getType().equals(MaterialType.ETIQUETADO))
            throw new BadRequestException("El tipo de material etiquetado debe ser " + MaterialType.ETIQUETADO);

        if (packagingCreateDTO.getUnitMeasurement().equals(UnitMeasurement.UNIDAD))
            throw new BadRequestException("La unidad de medida no puede ser " + UnitMeasurement.UNIDAD);

        Packaging packaging = packagingMapper.toEntity(packagingCreateDTO);
        packaging.setPackagingMaterial(packagingMaterial);
        packaging.setLabelingMaterial(labelingMaterial);
        packaging.setCreationDate(OffsetDateTime.now());
        packaging.setIsActive(Boolean.TRUE);

        Packaging savedPackaging = packagingRepository.saveAndFlush(packaging);

        return packagingMapper.toResponseDto(savedPackaging);
    }

    /**
     * Alterna el estado del paquete 
     * @param id
     * @return PackagingResponseDTO
     */
    @Override
    @Transactional
    public PackagingResponseDTO toggleActive(Long id) {
        Packaging packaging = packagingRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Packaging no encontrado con ID: " + id));
        
        packaging.toggleActive();

        Packaging savedPackaging = packagingRepository.save(packaging);

        return packagingMapper.toResponseDto(savedPackaging);
    }

    @Override
    @Transactional
    public Page<PackagingResponseDTO> findAll(Pageable pageable) {
        Pageable pageRequest = PageRequest.of(
            pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());
        Page<Packaging> packagings = packagingRepository.findAll(pageRequest);

        return packagings.map(packagingMapper::toResponseDto);
    }

    /**
     * Funcion para mostrar un paquete especifico segun id
     * 
     * @param id
     * @retutn Vista detallada de los elementos del paquete
     */
    @Override
    @Transactional
    public PackagingResponseDTO getPackaging(Long id) {
        Packaging packaging = packagingRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Packaging no encontrado con ID: " + id));
                
        return packagingMapper.toResponseDto(packaging);
    } 

    /**
     * Funcion que cambia ciertos parametros de un paquete preexistente
     * @param id
     * @param packagingUpdateDTO
     * @return PackagingResponseDTO
     */
    @Override
    @Transactional
    public PackagingResponseDTO updatePackaging(Long id,@Valid PackagingUpdateDTO packagingUpdateDTO) {
        Packaging originalPackaging = packagingRepository.findById(id)
                        .orElseThrow(()-> new ResourceNotFoundException("No se encontró packaging con id "+ id));
        Packaging updatedPackaging = packagingMapper.partialUpdate(packagingUpdateDTO, originalPackaging);

        if(packagingUpdateDTO.getPackagingMaterialId() != null
                && !packagingUpdateDTO.getPackagingMaterialId().equals(originalPackaging.getPackagingMaterial().getId())) {
            Material material = materialRepository.findById(packagingUpdateDTO.getPackagingMaterialId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Material no encontrado con id "
                                    + packagingUpdateDTO.getPackagingMaterialId()));
            if (!material.getType().equals(MaterialType.ENVASE))
                throw new BadRequestException("El tipo de material debe ser un envase " + MaterialType.ENVASE);
            originalPackaging.setPackagingMaterial(material);}

        if(packagingUpdateDTO.getLabelingMaterialId() != null
                && !packagingUpdateDTO.getLabelingMaterialId().equals(originalPackaging.getLabelingMaterial().getId())) {
            Material material = materialRepository.findById(packagingUpdateDTO.getLabelingMaterialId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Material no encontrado con id "
                                    + packagingUpdateDTO.getLabelingMaterialId()));
            if (!material.getType().equals(MaterialType.ETIQUETADO))
                throw new BadRequestException("El tipo de material debe ser un envase " + MaterialType.ETIQUETADO);
            originalPackaging.setPackagingMaterial(material);}

        if (packagingUpdateDTO.getUnitMeasurement() != null &&
                packagingUpdateDTO.getUnitMeasurement().equals(UnitMeasurement.UNIDAD))
            throw new BadRequestException("La unidad de medida no puede ser " + UnitMeasurement.UNIDAD);
        Packaging savedPackaging = packagingRepository.save(updatedPackaging);

        return packagingMapper.toResponseDto(savedPackaging);
    }

    /**
     * Funcion para mostrar a todos los paquetes activos
     *
     * @retutn Vista detallada de los paquetes activos
     */
    @Override
    @Transactional
    public List<PackagingSimpleResponseDTO> getPackagingList(String name, Boolean isActive, Long productId) {
        // If no name is provided, return empty list
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }
        
        String searchName = name.trim();
        List<Packaging> results;
        
        // Get unit measurement from product if productId is provided
        UnitMeasurement productUnitMeasurement = null;
        if (productId != null) {
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
            productUnitMeasurement = product.getUnitMeasurement();
        }
        
        // Query based on active status and unit measurement
        if (productUnitMeasurement != null) {
            // Filter by unit measurement from product
            if (isActive == null) {
                results = packagingRepository.findTop10ByUnitMeasurementAndNameContainingIgnoreCase(productUnitMeasurement, searchName);
            } else if (isActive) {
                results = packagingRepository.findTop10ByUnitMeasurementAndNameContainingIgnoreCaseAndIsActiveTrue(productUnitMeasurement, searchName);
            } else {
                results = packagingRepository.findTop10ByUnitMeasurementAndNameContainingIgnoreCaseAndIsActiveFalse(productUnitMeasurement, searchName);
            }
        } else {
            // No unit measurement filter
            if (isActive == null) {
                results = packagingRepository.findTop10ByNameContainingIgnoreCase(searchName);
            } else if (isActive) {
                results = packagingRepository.findTop10ByNameContainingIgnoreCaseAndIsActiveTrue(searchName);
            } else {
                results = packagingRepository.findTop10ByNameContainingIgnoreCaseAndIsActiveFalse(searchName);
            }
        }
        
        return results.stream()
                .map(packagingMapper::toSimpleResponseDTO)
                .toList();
    }
}
