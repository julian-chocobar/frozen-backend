package com.enigcode.frozen_backend.production_materials.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.production_materials.DTO.ProductionMaterialResponseDTO;
import com.enigcode.frozen_backend.production_materials.mapper.ProductionMaterialMapper;
import com.enigcode.frozen_backend.production_materials.model.ProductionMaterial;
import com.enigcode.frozen_backend.production_materials.repository.ProductionMaterialRepository;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductionMaterialServiceImplTest {

    @Mock
    private ProductionMaterialRepository repository;

    @Mock
    private ProductionMaterialMapper mapper;

    @InjectMocks
    private ProductionMaterialServiceImpl service;

    private ProductionMaterial entity;
    private ProductionMaterialResponseDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        entity = ProductionMaterial.builder()
                .id(5L)
                .material(Material.builder().id(11L).code("MAT-001").build())
                .productionPhase(ProductionPhase.builder().id(22L).build())
                .quantity(12.5)
                .build();

        dto = ProductionMaterialResponseDTO.builder()
                .id(5L)
                .materialId(11L)
                .materialCode("MAT-001")
                .productionPhaseId(22L)
                .quantity(12.5)
                .build();
    }

    @Test
    void getProductionMaterial_success() {
        when(repository.findById(5L)).thenReturn(Optional.of(entity));
        when(mapper.toResponseDTO(entity)).thenReturn(dto);

        ProductionMaterialResponseDTO result = service.getProductionMaterial(5L);

        assertNotNull(result);
        assertEquals(11L, result.getMaterialId());
        verify(repository).findById(5L);
        verify(mapper).toResponseDTO(entity);
    }

    @Test
    void getProductionMaterial_notFound_throws() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getProductionMaterial(999L));
        verify(repository).findById(999L);
        verify(mapper, never()).toResponseDTO(any());
    }

    @Test
    void getProductionMaterialByPhase_success() {
        ProductionMaterial entity2 = ProductionMaterial.builder()
                .id(6L)
                .material(Material.builder().id(12L).code("MAT-002").build())
                .productionPhase(ProductionPhase.builder().id(22L).build())
                .quantity(7.0)
                .build();

        ProductionMaterialResponseDTO dto2 = ProductionMaterialResponseDTO.builder()
                .id(6L)
                .materialId(12L)
                .materialCode("MAT-002")
                .productionPhaseId(22L)
                .quantity(7.0)
                .build();

        when(repository.findAllByProductionPhaseId(22L)).thenReturn(List.of(entity, entity2));
        when(mapper.toResponseDTO(entity)).thenReturn(dto);
        when(mapper.toResponseDTO(entity2)).thenReturn(dto2);

        List<ProductionMaterialResponseDTO> list = service.getProductionMaterialByPhase(22L);
        assertEquals(2, list.size());
        assertEquals("MAT-001", list.get(0).getMaterialCode());
        assertEquals("MAT-002", list.get(1).getMaterialCode());
    }

    @Test
    void getProductionMaterialByPhase_empty_returnsEmptyList() {
        when(repository.findAllByProductionPhaseId(123L)).thenReturn(List.of());
        List<ProductionMaterialResponseDTO> list = service.getProductionMaterialByPhase(123L);
        assertNotNull(list);
        assertTrue(list.isEmpty());
        verify(mapper, never()).toResponseDTO(any());
    }

    @Test
    void getProductionMaterialByBatch_success() {
        when(repository.findAllByBatchId(10L)).thenReturn(List.of(entity));
        when(mapper.toResponseDTO(entity)).thenReturn(dto);
        List<ProductionMaterialResponseDTO> list = service.getProductionMaterialByBatch(10L);
        assertEquals(1, list.size());
        assertEquals(11L, list.get(0).getMaterialId());
    }

    @Test
    void getProductionMaterialByBatch_empty_returnsEmptyList() {
        when(repository.findAllByBatchId(77L)).thenReturn(List.of());
        List<ProductionMaterialResponseDTO> list = service.getProductionMaterialByBatch(77L);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }
}
