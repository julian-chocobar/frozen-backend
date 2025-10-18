package com.enigcode.frozen_backend.materials.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.materials.DTO.*;
import com.enigcode.frozen_backend.materials.mapper.MaterialMapper;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
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

        List<MaterialSimpleResponseDTO> result = materialService.getMaterialSimpleList("Pl", null);

        assertEquals(1, result.size());
        assertEquals("Plástico", result.get(0).getName());
    }
}
