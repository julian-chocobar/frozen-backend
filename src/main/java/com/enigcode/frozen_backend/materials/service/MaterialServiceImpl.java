package com.enigcode.frozen_backend.materials.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.materials.DTO.*;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import com.enigcode.frozen_backend.materials.mapper.MaterialMapper;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.enigcode.frozen_backend.materials.specification.MaterialSpecification;
import com.enigcode.frozen_backend.product_phases.model.Phase;

import jakarta.transaction.Transactional;

import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.WarehouseCoordinates;
import com.enigcode.frozen_backend.warehouse.service.WarehouseLayoutService;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {

    final MaterialRepository materialRepository;
    final MaterialMapper materialMapper;
    final WarehouseLayoutService warehouseLayoutService;

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
        // La validación de código único se hará después de generar el código

        Material material = materialMapper.toEntity(materialCreateDTO);
        material.setCreationDate(OffsetDateTime.now(ZoneOffset.UTC));
        material.setLastUpdateDate(OffsetDateTime.now(ZoneOffset.UTC));
        material.setIsActive(Boolean.TRUE);

        // Asegurar valor por defecto seguro para reservedStock
        if (material.getReservedStock() == null) {
            material.setReservedStock(0.0);
        }

        // Asignar ubicación automática si no se proporcionó
        assignWarehouseLocationIfNeeded(material, materialCreateDTO);

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
        MaterialType type = material.getType();
        UnitMeasurement unit = material.getUnitMeasurement();

        // Si es ENVASE o ETIQUETADO → debe ser UNIDAD
        if ((type == MaterialType.ENVASE || type == MaterialType.ETIQUETADO) && unit != UnitMeasurement.UNIDAD)
            throw new BadRequestException("El material de tipo: " + type + " debe tener como unidad de medida UNIDAD");

        // Si la unidad es UNIDAD → solo se permite ENVASE, OTROS o ETIQUETADO
        boolean isUnitAllowedType = type == MaterialType.ENVASE ||
                type == MaterialType.OTROS ||
                type == MaterialType.ETIQUETADO;

        if (unit == UnitMeasurement.UNIDAD && !isUnitAllowedType)
            throw new BadRequestException(
                    "La unidad de medida UNIDAD solo se permite con materiales de tipo ENVASE, OTROS o ETIQUETADO");

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
    public MaterialResponseDTO updateMaterial(@NonNull Long id, MaterialUpdateDTO materialUpdateDTO) {
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
    public MaterialResponseDTO toggleActive(@NonNull Long id) {
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
    public List<MaterialSimpleResponseDTO> getMaterialSimpleList(String name, Boolean active, Phase phase,
            MaterialType type) {
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }
        String q = name.trim();
        List<Material> results;

        // Handle Phase filter (takes precedence over type if both are provided)
        if (phase != null) {
            List<MaterialType> validMaterialTypes = getValidMaterialTypesForPhase(phase);
            if (active == null) {
                results = materialRepository.findTop10ByTypeInAndNameContainingIgnoreCase(validMaterialTypes, q);
            } else if (active) {
                results = materialRepository
                        .findTop10ByTypeInAndNameContainingIgnoreCaseAndIsActiveTrue(validMaterialTypes, q);
            } else {
                results = materialRepository
                        .findTop10ByTypeInAndNameContainingIgnoreCaseAndIsActiveFalse(validMaterialTypes, q);
            }
        }
        // Handle MaterialType filter (only if phase is not provided)
        else if (type != null) {
            if (active == null) {
                results = materialRepository.findTop10ByTypeInAndNameContainingIgnoreCase(List.of(type), q);
            } else if (active) {
                results = materialRepository.findTop10ByTypeInAndNameContainingIgnoreCaseAndIsActiveTrue(List.of(type),
                        q);
            } else {
                results = materialRepository.findTop10ByTypeInAndNameContainingIgnoreCaseAndIsActiveFalse(List.of(type),
                        q);
            }
        }
        // No type or phase filter
        else {
            if (active == null) {
                results = materialRepository.findTop10ByNameContainingIgnoreCase(q);
            } else if (active) {
                results = materialRepository.findTop10ByNameContainingIgnoreCaseAndIsActiveTrue(q);
            } else {
                results = materialRepository.findTop10ByNameContainingIgnoreCaseAndIsActiveFalse(q);
            }
        }

        return results.stream()
                .map(m -> new MaterialSimpleResponseDTO(m.getId(), m.getCode(), m.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the list of valid material types for a given phase
     * 
     * @param phase The phase to get valid material types for
     * @return List of valid MaterialType for the phase, or null if phase is null or
     *         no restrictions
     */
    private List<MaterialType> getValidMaterialTypesForPhase(Phase phase) {
        if (phase == null) {
            return null;
        }

        Map<Phase, List<MaterialType>> phaseMaterialMap = new EnumMap<>(Phase.class);
        phaseMaterialMap.put(Phase.MOLIENDA, List.of(MaterialType.MALTA));
        phaseMaterialMap.put(Phase.MACERACION, List.of(MaterialType.AGUA));
        phaseMaterialMap.put(Phase.FILTRACION, List.of());
        phaseMaterialMap.put(Phase.COCCION, List.of(MaterialType.AGUA, MaterialType.LUPULO));
        phaseMaterialMap.put(Phase.FERMENTACION, List.of(MaterialType.LEVADURA));
        phaseMaterialMap.put(Phase.MADURACION, List.of());
        phaseMaterialMap.put(Phase.GASIFICACION, List.of());
        phaseMaterialMap.put(Phase.ENVASADO, List.of(MaterialType.ENVASE));
        phaseMaterialMap.put(Phase.DESALCOHOLIZACION, List.of());

        return phaseMaterialMap.get(phase);
    }

    /**
     * Funcion para mostrar un material especifico segun id
     * 
     * @param id
     * @return Vista detallada de los elementos del material
     */
    @Override
    public MaterialDetailDTO getMaterial(@NonNull Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado con ID: " + id));

        return materialMapper.toDetailDto(material);
    }

    // Métodos para funcionalidad de almacén

    @Override
    public List<MaterialWarehouseLocationDTO> getWarehouseLocations(String zone, Boolean activeOnly) {
        List<Material> materials;

        if (zone != null && !zone.trim().isEmpty()) {
            if (activeOnly) {
                materials = materialRepository.findByWarehouseZoneAndIsActiveTrue(zone);
            } else {
                materials = materialRepository.findByWarehouseZone(zone);
            }
        } else {
            if (activeOnly) {
                materials = materialRepository.findByIsActiveTrueAndWarehouseXIsNotNullAndWarehouseYIsNotNull();
            } else {
                materials = materialRepository.findByWarehouseXIsNotNullAndWarehouseYIsNotNull();
            }
        }

        return materials.stream()
                .map(materialMapper::toWarehouseLocationDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MaterialResponseDTO updateMaterialLocation(@NonNull Long id, MaterialLocationUpdateDTO locationUpdateDTO) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado con ID: " + id));

        material.setWarehouseX(locationUpdateDTO.getWarehouseX());
        material.setWarehouseY(locationUpdateDTO.getWarehouseY());
        material.setWarehouseZone(locationUpdateDTO.getWarehouseZone());
        material.setWarehouseSection(locationUpdateDTO.getWarehouseSection());
        material.setWarehouseLevel(locationUpdateDTO.getWarehouseLevel());
        material.setLastUpdateDate(OffsetDateTime.now(ZoneOffset.UTC));

        Material savedMaterial = materialRepository.save(material);
        return materialMapper.toResponseDto(savedMaterial);
    }

    @Override
    public WarehouseInfoDTO getWarehouseInfo(MaterialType materialType) {
        List<String> availableZones = List.of(
                "ZONA_MALTA", "ZONA_LUPULO", "ZONA_AGUA",
                "ZONA_LEVADURA", "ZONA_ENVASE", "ZONA_ETIQUETADO", "ZONA_OTROS");

        Map<String, List<String>> sectionsByZone = availableZones.stream()
                .collect(Collectors.toMap(
                        zone -> zone,
                        zone -> materialRepository.findWarehouseSectionsByZone(zone)));

        // Sugerir ubicación para el tipo de material
        WarehouseInfoDTO.SuggestedLocationDTO suggestedLocation = null;
        if (materialType != null) {
            String suggestedZone = warehouseLayoutService.getDefaultZoneForMaterialType(materialType);
            String suggestedSection = getNextAvailableSection(suggestedZone);
            WarehouseCoordinates coords = warehouseLayoutService.calculateCoordinatesForSection(suggestedZone,
                    suggestedSection);

            suggestedLocation = WarehouseInfoDTO.SuggestedLocationDTO.builder()
                    .zone(suggestedZone)
                    .section(suggestedSection)
                    .x(coords.getX())
                    .y(coords.getY())
                    .level(1)
                    .build();
        }

        return WarehouseInfoDTO.builder()
                .availableZones(availableZones)
                .sectionsByZone(sectionsByZone)
                .suggestedLocation(suggestedLocation)
                .build();
    }

    private String getNextAvailableSection(String zone) {
        // Buscar la siguiente sección disponible en la zona
        List<String> usedSections = materialRepository.findWarehouseSectionsByZone(zone);

        // Generar secciones A1, A2, A3... B1, B2, B3... etc.
        char letter = 'A';
        int number = 1;

        while (letter <= 'Z') {
            String section = letter + String.valueOf(number);
            if (!usedSections.contains(section)) {
                return section;
            }

            number++;
            if (number > 20) { // Máximo 20 secciones por letra
                letter++;
                number = 1;
            }
        }

        return "Z99"; // Fallback si todas las secciones están tomadas
    }

    // Método para asignar ubicación automática en la creación
    private void assignWarehouseLocationIfNeeded(Material material, MaterialCreateDTO dto) {
        // Si no se proporcionó zona, asignar por tipo de material
        if (dto.getWarehouseZone() == null || dto.getWarehouseZone().trim().isEmpty()) {
            material.setWarehouseZone(warehouseLayoutService.getDefaultZoneForMaterialType(dto.getType()));
        }

        // Si no se proporcionó sección, asignar la siguiente disponible
        if (dto.getWarehouseSection() == null || dto.getWarehouseSection().trim().isEmpty()) {
            material.setWarehouseSection(getNextAvailableSection(material.getWarehouseZone()));
        }

        // Si no se proporcionaron coordenadas, calcular posición por defecto
        if (dto.getWarehouseX() == null || dto.getWarehouseY() == null) {
            WarehouseCoordinates coords = warehouseLayoutService.calculateCoordinatesForSection(
                    material.getWarehouseZone(),
                    material.getWarehouseSection());
            material.setWarehouseX(coords.getX());
            material.setWarehouseY(coords.getY());
        }

        // Si no se proporcionó nivel, usar nivel 1
        if (dto.getWarehouseLevel() == null) {
            material.setWarehouseLevel(1);
        }
    }
}
