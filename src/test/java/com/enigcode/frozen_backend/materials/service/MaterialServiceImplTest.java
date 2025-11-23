package com.enigcode.frozen_backend.materials.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.materials.DTO.*;
import com.enigcode.frozen_backend.materials.mapper.MaterialMapper;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.WarehouseZone;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

import org.mockito.ArgumentMatchers;

@ExtendWith(MockitoExtension.class)
class MaterialServiceImplTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private MaterialMapper materialMapper;

    @Mock
    private com.enigcode.frozen_backend.warehouse.service.WarehouseLayoutService warehouseLayoutService;
    @InjectMocks
    private MaterialServiceImpl materialService;

    private Material material;
    private MaterialResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        material = new Material();
        material.setId(1L);

        material.setName("Cartón");
        material.setType(MaterialType.ENVASE);
        material.setUnitMeasurement(com.enigcode.frozen_backend.materials.model.UnitMeasurement.UNIDAD);

        responseDTO = new MaterialResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("Cartón");
    }

    @Test
    void testSaveMaterial() {
        MaterialCreateDTO createDTO = new MaterialCreateDTO();

        createDTO.setType(MaterialType.ENVASE);
        createDTO.setName("Cartón");
        createDTO.setUnitMeasurement(com.enigcode.frozen_backend.materials.model.UnitMeasurement.UNIDAD);
        createDTO.setThreshold(1.0);
        createDTO.setStock(0.0);
        createDTO.setValue(0.0);

        when(materialMapper.toEntity(createDTO)).thenReturn(material);
        when(materialRepository.save(any(Material.class))).thenReturn(material);
        when(materialRepository.saveAndFlush(any(Material.class))).thenReturn(material);
        when(materialMapper.toResponseDto(any(Material.class))).thenReturn(responseDTO);

        MaterialResponseDTO result = materialService.createMaterial(createDTO);

        assertNotNull(result);
        assertEquals(responseDTO.getId(), result.getId());
        verify(materialRepository, times(1)).save(any(Material.class));
        verify(materialRepository, times(1)).saveAndFlush(any(Material.class));
    }

    @Test
    void testUpdateMaterial_Success() {
        MaterialUpdateDTO updateDTO = new MaterialUpdateDTO();
    updateDTO.setType(MaterialType.ENVASE);

        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(materialMapper.partialUpdate(updateDTO, material)).thenReturn(material);
        when(materialRepository.save(material)).thenReturn(material);
        when(materialMapper.toResponseDto(material)).thenReturn(responseDTO);

        MaterialResponseDTO result = materialService.updateMaterial(1L, updateDTO);

        assertEquals(responseDTO.getId(), result.getId());
        verify(materialRepository).save(material);
    }

    @Test
    void testUpdateMaterial_NotFound() {
        when(materialRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> materialService.updateMaterial(99L, new MaterialUpdateDTO()));
    }

    @Test
    void testToggleActive_Success() {
        material.setIsActive(true);
        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(materialRepository.save(material)).thenReturn(material);
        when(materialMapper.toResponseDto(material)).thenReturn(responseDTO);

        MaterialResponseDTO result = materialService.toggleActive(1L);

        assertNotNull(result);
        verify(materialRepository).save(material);
    }

    @Test
    void testGetMaterial_Success() {
        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(materialMapper.toDetailDto(material)).thenReturn(new MaterialDetailDTO());

        MaterialDetailDTO result = materialService.getMaterial(1L);

        assertNotNull(result);
        verify(materialRepository).findById(1L);
    }

    @Test
    void testFindAll_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Material> page = new PageImpl<>(List.of(material));

        when(materialRepository.findAll(ArgumentMatchers.<Specification<Material>>any(), any(Pageable.class))).thenReturn(page);
        when(materialMapper.toResponseDto(any(Material.class))).thenReturn(responseDTO);

        Page<MaterialResponseDTO> result = materialService.findAll(new MaterialFilterDTO(), pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Cartón", result.getContent().get(0).getName());
    }

    @Test
    void testGetMaterialSimpleList() {
        material.setId(1L);
        material.setCode("MAT-1");
        material.setName("Plástico");
        when(materialRepository.findTop10ByNameContainingIgnoreCase("Pl")).thenReturn(List.of(material));

        List<MaterialSimpleResponseDTO> result = materialService.getMaterialSimpleList("Pl", null, null, null);

        assertEquals(1, result.size());
        assertEquals("Plástico", result.get(0).getName());
    }

        @Test
        void createMaterial_invalidUnitForType_throwsBadRequest() {
        MaterialCreateDTO createDTO = new MaterialCreateDTO();
        createDTO.setType(MaterialType.ENVASE);
        createDTO.setName("Caja");
        createDTO.setUnitMeasurement(com.enigcode.frozen_backend.materials.model.UnitMeasurement.KG);

        Material bad = new Material();
        bad.setType(MaterialType.ENVASE);
        bad.setUnitMeasurement(com.enigcode.frozen_backend.materials.model.UnitMeasurement.KG);

        when(materialMapper.toEntity(createDTO)).thenReturn(bad);

        assertThrows(com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException.class,
            () -> materialService.createMaterial(createDTO));

        verify(materialRepository, never()).save(any());
        }

        @Test
        void updateMaterialLocation_invalidLayout_throwsBadRequest() {
        com.enigcode.frozen_backend.materials.DTO.MaterialLocationUpdateDTO dto = new com.enigcode.frozen_backend.materials.DTO.MaterialLocationUpdateDTO();
        dto.setWarehouseZone(com.enigcode.frozen_backend.materials.model.WarehouseZone.MALTA);
        dto.setWarehouseSection("A1");
        dto.setWarehouseLevel(2);

        Material existing = new Material();
        existing.setId(2L);
        existing.setName("Malta Test");
        existing.setType(com.enigcode.frozen_backend.materials.model.MaterialType.MALTA);
        existing.setUnitMeasurement(com.enigcode.frozen_backend.materials.model.UnitMeasurement.KG);
        existing.setStock(10.0);
        existing.setThreshold(1.0);
        existing.setCreationDate(java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC));

        when(materialRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(warehouseLayoutService.isValidLocation(dto.getWarehouseZone(), dto.getWarehouseSection(), dto.getWarehouseLevel()))
            .thenReturn(false);

        assertThrows(com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException.class,
            () -> materialService.updateMaterialLocation(2L, dto));
        }

        @Test
        void getMaterialSimpleList_withPhase_usesPhaseMapping() {
        Material m = new Material();
        m.setId(5L);
        m.setName("Malta Especial");
        m.setCode("MAL-5");
        m.setType(MaterialType.MALTA);

        when(materialRepository.findTop10ByTypeInAndNameContainingIgnoreCase(anyList(), eq("Mal")))
            .thenReturn(List.of(m));

        List<com.enigcode.frozen_backend.materials.DTO.MaterialSimpleResponseDTO> result =
            materialService.getMaterialSimpleList("Mal", null, com.enigcode.frozen_backend.product_phases.model.Phase.MOLIENDA, null);

        assertEquals(1, result.size());
        assertEquals("Malta Especial", result.get(0).getName());
        verify(materialRepository).findTop10ByTypeInAndNameContainingIgnoreCase(anyList(), eq("Mal"));
        }

    @Test
    void createMaterial_invalidSectionProvided_throwsBadRequest() {
        MaterialCreateDTO dto = new MaterialCreateDTO();
        dto.setName("NewMaterial");
        dto.setType(MaterialType.OTROS);
        dto.setWarehouseZone(com.enigcode.frozen_backend.materials.model.WarehouseZone.ENVASE);
        dto.setWarehouseSection("INVALID_SECTION");

        when(materialMapper.toEntity(dto)).thenAnswer(inv -> {
            Material m = new Material();
            m.setName(dto.getName());
            m.setType(dto.getType());
            return m;
        });

        assertThrows(com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException.class,
            () -> materialService.createMaterial(dto));
    }

    @Test
    void getWarehouseInfo_withMaterialType_returnsSuggestedLocation() {
        when(materialRepository.findWarehouseSectionsByZone(any())).thenReturn(List.of("S1", "S2"));
        when(materialRepository.countByWarehouseZone(any())).thenReturn(2L);
        when(materialRepository.count()).thenReturn(10L);

        com.enigcode.frozen_backend.materials.DTO.WarehouseInfoDTO info = materialService.getWarehouseInfo(MaterialType.OTROS);

        assertNotNull(info);
        assertNotNull(info.getSuggestedLocation());
        assertEquals(com.enigcode.frozen_backend.materials.model.WarehouseZone.getDefaultZoneForMaterialType(MaterialType.OTROS).name(),
            info.getSuggestedLocation().getZone());
        assertTrue(com.enigcode.frozen_backend.materials.model.WarehouseZone.getDefaultZoneForMaterialType(MaterialType.OTROS)
            .getAvailableSections().contains(info.getSuggestedLocation().getSection()));
    }

    @Test
    void updateMaterialLocation_success() {
        com.enigcode.frozen_backend.materials.DTO.MaterialLocationUpdateDTO dto = new com.enigcode.frozen_backend.materials.DTO.MaterialLocationUpdateDTO();
        dto.setWarehouseZone(com.enigcode.frozen_backend.materials.model.WarehouseZone.MALTA);
        dto.setWarehouseSection("A2");
        dto.setWarehouseLevel(2);

        Material existing = new Material();
        existing.setId(3L);
        existing.setName("Malta X");
        existing.setType(MaterialType.MALTA);
        existing.setUnitMeasurement(com.enigcode.frozen_backend.materials.model.UnitMeasurement.KG);
        existing.setStock(5.0);
        existing.setThreshold(1.0);
        existing.setCreationDate(java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC));
        when(materialRepository.findById(3L)).thenReturn(Optional.of(existing));

        when(warehouseLayoutService.isValidLocation(dto.getWarehouseZone(), dto.getWarehouseSection(), dto.getWarehouseLevel()))
            .thenReturn(true);

        when(materialRepository.save(any(Material.class))).thenAnswer(inv -> inv.getArgument(0));
        when(materialMapper.toResponseDto(any(Material.class))).thenReturn(responseDTO);

        MaterialResponseDTO res = materialService.updateMaterialLocation(3L, dto);

        assertNotNull(res);
        assertEquals(responseDTO.getId(), res.getId());
        assertEquals(com.enigcode.frozen_backend.materials.model.WarehouseZone.MALTA, existing.getWarehouseZone());
        assertEquals("A2", existing.getWarehouseSection());
        assertEquals(2, existing.getWarehouseLevel());
        verify(materialRepository).save(existing);
    }

    @Test
    void assignWarehouseLocationIfNeeded_assignsDefaults() {
        MaterialCreateDTO dto = new MaterialCreateDTO();
        dto.setName("AutoLoc");
        dto.setType(MaterialType.MALTA);
        dto.setUnitMeasurement(com.enigcode.frozen_backend.materials.model.UnitMeasurement.KG);
        dto.setThreshold(1.0);

        Material m = new Material();
        m.setType(MaterialType.MALTA);
        when(materialMapper.toEntity(dto)).thenReturn(m);

        when(materialRepository.findWarehouseSectionsByZone(any())).thenReturn(List.of());

        when(materialRepository.save(any(Material.class))).thenAnswer(inv -> {
            Material saved = inv.getArgument(0);
            saved.setId(50L);
            return saved;
        });
        when(materialRepository.saveAndFlush(any(Material.class))).thenAnswer(inv -> inv.getArgument(0));
        when(materialMapper.toResponseDto(any(Material.class))).thenReturn(responseDTO);

        MaterialResponseDTO dtoRes = materialService.createMaterial(dto);

        assertNotNull(dtoRes);
        assertEquals(WarehouseZone.getDefaultZoneForMaterialType(MaterialType.MALTA), m.getWarehouseZone());
        assertNotNull(m.getWarehouseSection());
        assertTrue(WarehouseZone.getDefaultZoneForMaterialType(MaterialType.MALTA).getAvailableSections().contains(m.getWarehouseSection()));
    }

    @Test
    void getWarehouseLocations_mapsCoordinatesAndLevelDisplay() {
        Material mat = new Material();
        mat.setId(7L);
        mat.setName("LocTest");
        mat.setWarehouseZone(com.enigcode.frozen_backend.materials.model.WarehouseZone.MALTA);
        mat.setWarehouseSection("A1");
        mat.setWarehouseLevel(1);

        when(materialRepository.findByWarehouseZoneAndIsActiveTrue(com.enigcode.frozen_backend.materials.model.WarehouseZone.MALTA))
            .thenReturn(List.of(mat));

        com.enigcode.frozen_backend.materials.DTO.MaterialWarehouseLocationDTO dto = new com.enigcode.frozen_backend.materials.DTO.MaterialWarehouseLocationDTO();
        dto.setId(7L);
        dto.setName("LocTest");

        when(materialMapper.toWarehouseLocationDTO(mat)).thenReturn(dto);
        when(warehouseLayoutService.calculateCoordinates(mat.getWarehouseZone(), mat.getWarehouseSection(), mat.getWarehouseLevel()))
            .thenReturn(new Double[]{1.1, 2.2});
        when(warehouseLayoutService.getLevelDisplay(mat.getWarehouseLevel())).thenReturn("L1");

        List<com.enigcode.frozen_backend.materials.DTO.MaterialWarehouseLocationDTO> res = materialService.getWarehouseLocations("MALTA", true);

        assertNotNull(res);
        assertEquals(Double.valueOf(1.1), res.get(0).getWarehouseX());
        assertEquals(Double.valueOf(2.2), res.get(0).getWarehouseY());
        assertEquals("L1", res.get(0).getLevelDisplay());
    }
}
