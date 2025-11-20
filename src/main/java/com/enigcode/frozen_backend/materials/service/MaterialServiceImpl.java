package com.enigcode.frozen_backend.materials.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.materials.DTO.*;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.materials.model.WarehouseZone;

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
        if (materialUpdateDTO.getType() != null && !originalMaterial.getType().equals(materialUpdateDTO.getType()))
            newCode = generateCode(materialUpdateDTO.getType(), originalMaterial.getId());

        Material updatedMaterial = materialMapper.partialUpdate(materialUpdateDTO, originalMaterial);
        updatedMaterial.setLastUpdateDate(OffsetDateTime.now(ZoneOffset.UTC));

        if (newCode != null)
            updatedMaterial.setCode(newCode);

        // Actualizar ubicación del almacén si se proporciona
        if (hasWarehouseLocationData(materialUpdateDTO)) {
            updateMaterialLocationFromDTO(updatedMaterial, materialUpdateDTO);
        }

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
            try {
                WarehouseZone warehouseZone = WarehouseZone.valueOf(zone.toUpperCase());

                if (activeOnly) {
                    materials = materialRepository.findByWarehouseZoneAndIsActiveTrue(warehouseZone);
                } else {
                    materials = materialRepository.findByWarehouseZone(warehouseZone);
                }
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Zona de almacén no válida: " + zone);
            }
        } else {
            if (activeOnly) {
                materials = materialRepository.findByIsActiveTrueAndWarehouseZoneIsNotNull();
            } else {
                materials = materialRepository.findByWarehouseZoneIsNotNull();
            }
        }

        return materials.stream()
                .map(material -> {
                    MaterialWarehouseLocationDTO dto = materialMapper.toWarehouseLocationDTO(material);

                    // Calcular coordenadas usando el WarehouseLayoutService
                    Double[] coordinates = warehouseLayoutService.calculateCoordinates(
                            material.getWarehouseZone(),
                            material.getWarehouseSection(),
                            material.getWarehouseLevel());

                    dto.setWarehouseX(coordinates[0]);
                    dto.setWarehouseY(coordinates[1]);
                    dto.setLevelDisplay(warehouseLayoutService.getLevelDisplay(material.getWarehouseLevel()));

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MaterialResponseDTO updateMaterialLocation(@NonNull Long id, MaterialLocationUpdateDTO locationUpdateDTO) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado con ID: " + id));

        // Validar ubicación
        if (!warehouseLayoutService.isValidLocation(
                locationUpdateDTO.getWarehouseZone(),
                locationUpdateDTO.getWarehouseSection(),
                locationUpdateDTO.getWarehouseLevel())) {
            throw new BadRequestException("La ubicación especificada no es válida");
        }

        material.setWarehouseZone(locationUpdateDTO.getWarehouseZone());
        material.setWarehouseSection(locationUpdateDTO.getWarehouseSection());
        material.setWarehouseLevel(locationUpdateDTO.getWarehouseLevel());
        material.setLastUpdateDate(OffsetDateTime.now(ZoneOffset.UTC));

        Material savedMaterial = materialRepository.save(material);
        return materialMapper.toResponseDto(savedMaterial);
    }

    @Override
    public WarehouseInfoDTO getWarehouseInfo(MaterialType materialType) {
        // Obtener zonas disponibles con información
        List<WarehouseInfoDTO.ZoneInfoDTO> availableZones = java.util.Arrays.stream(
                WarehouseZone.values())
                .map(zone -> {
                    List<String> usedSections = materialRepository.findWarehouseSectionsByZone(zone);
                    return WarehouseInfoDTO.ZoneInfoDTO.builder()
                            .name(zone.name())
                            .displayName(zone.getDisplayName())
                            .totalSections(zone.getAvailableSections().size())
                            .occupiedSections(usedSections.size())
                            .availableSections(zone.getAvailableSections())
                            .build();
                })
                .collect(Collectors.toList());

        // Sugerir ubicación para el tipo de material
        WarehouseInfoDTO.SuggestedLocationDTO suggestedLocation = null;
        if (materialType != null) {
            WarehouseZone suggestedZone = WarehouseZone.getDefaultZoneForMaterialType(materialType);
            String suggestedSection = getNextAvailableSection(suggestedZone);

            suggestedLocation = WarehouseInfoDTO.SuggestedLocationDTO.builder()
                    .zone(suggestedZone.name())
                    .section(suggestedSection)
                    .level(1)
                    .build();
        }

        // Contar materiales por zona
        Map<String, Long> materialsByZone = java.util.Arrays.stream(WarehouseZone.values())
                .collect(Collectors.toMap(
                        zone -> zone.name(),
                        zone -> materialRepository.countByWarehouseZone(zone)));

        return WarehouseInfoDTO.builder()
                .availableZones(availableZones)
                .suggestedLocation(suggestedLocation)
                .materialsByZone(materialsByZone)
                .totalMaterials(materialRepository.count())
                .build();
    }

    private String getNextAvailableSection(WarehouseZone zone) {
        // Obtener secciones ocupadas en esta zona
        List<String> usedSections = materialRepository.findWarehouseSectionsByZone(zone);

        // Buscar la primera sección disponible de las predefinidas para esta zona
        for (String section : zone.getAvailableSections()) {
            if (!usedSections.contains(section)) {
                return section;
            }
        }

        // Si todas las secciones están ocupadas, devolver la primera (permite múltiples
        // niveles)
        return zone.getAvailableSections().get(0);
    }

    // Método para asignar ubicación automática en la creación
    private void assignWarehouseLocationIfNeeded(Material material, MaterialCreateDTO dto) {
        // Si no se proporcionó zona, asignar por tipo de material
        if (dto.getWarehouseZone() == null) {
            material.setWarehouseZone(WarehouseZone.getDefaultZoneForMaterialType(dto.getType()));
        } else {
            material.setWarehouseZone(dto.getWarehouseZone());
        }

        // Si no se proporcionó sección, asignar la primera disponible
        if (dto.getWarehouseSection() == null || dto.getWarehouseSection().trim().isEmpty()) {
            material.setWarehouseSection(getNextAvailableSection(material.getWarehouseZone()));
        } else {
            // Validar que la sección es válida para la zona
            if (!material.getWarehouseZone().isValidSection(dto.getWarehouseSection())) {
                throw new BadRequestException("La sección " + dto.getWarehouseSection() +
                        " no es válida para la zona " + material.getWarehouseZone().getDisplayName());
            }
            material.setWarehouseSection(dto.getWarehouseSection());
        }

        // Si no se proporcionó nivel, usar nivel 1
        if (dto.getWarehouseLevel() == null) {
            material.setWarehouseLevel(1);
        } else {
            // Validar que el nivel es válido
            if (!WarehouseZone.isValidLevel(dto.getWarehouseLevel())) {
                throw new BadRequestException("El nivel debe estar entre 1 y 3");
            }
            material.setWarehouseLevel(dto.getWarehouseLevel());
        }
    }

    /**
     * Verifica si el DTO contiene datos de ubicación del almacén
     */
    private boolean hasWarehouseLocationData(MaterialUpdateDTO dto) {
        return dto.getWarehouseZone() != null ||
                dto.getWarehouseSection() != null ||
                dto.getWarehouseLevel() != null;
    }

    /**
     * Actualiza la ubicación del material desde el DTO de actualización
     */
    private void updateMaterialLocationFromDTO(Material material, MaterialUpdateDTO dto) {
        // Actualizar zona si se proporciona
        if (dto.getWarehouseZone() != null) {
            material.setWarehouseZone(dto.getWarehouseZone());
        }

        // Actualizar sección si se proporciona
        if (dto.getWarehouseSection() != null) {
            // Validar que la sección es válida para la zona actual
            if (material.getWarehouseZone() != null &&
                    !material.getWarehouseZone().isValidSection(dto.getWarehouseSection())) {
                throw new BadRequestException("La sección " + dto.getWarehouseSection() +
                        " no es válida para la zona " + material.getWarehouseZone().getDisplayName());
            }
            material.setWarehouseSection(dto.getWarehouseSection());
        }

        // Actualizar nivel si se proporciona
        if (dto.getWarehouseLevel() != null) {
            if (!WarehouseZone.isValidLevel(dto.getWarehouseLevel())) {
                throw new BadRequestException("El nivel debe estar entre 1 y 3");
            }
            material.setWarehouseLevel(dto.getWarehouseLevel());
        }

        // Validar ubicación completa si todos los campos están presentes
        if (material.getWarehouseZone() != null &&
                material.getWarehouseSection() != null &&
                material.getWarehouseLevel() != null) {

            if (!warehouseLayoutService.isValidLocation(
                    material.getWarehouseZone(),
                    material.getWarehouseSection(),
                    material.getWarehouseLevel())) {
                throw new BadRequestException("La ubicación especificada no es válida en el mapa del almacén");
            }
        }
    }
}
