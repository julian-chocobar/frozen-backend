package com.enigcode.frozen_backend.packagings.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.packagings.DTO.*;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.products.model.Product;
import com.enigcode.frozen_backend.packagings.mapper.PackagingMapper;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.packagings.repository.PackagingRepository;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackagingServiceImplTest {

    @Mock
    private PackagingRepository packagingRepository;
    @Mock
    private MaterialRepository materialRepository;
    @Mock
    private com.enigcode.frozen_backend.products.repository.ProductRepository productRepository;
    @Mock
    private PackagingMapper packagingMapper;

    @InjectMocks
    private PackagingServiceImpl packagingService;

    private Packaging packaging;
    private PackagingResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        packaging = new Packaging();
        packaging.setId(1L);
        packaging.setName("Caja grande");
        packaging.setIsActive(true);
        packaging.setCreationDate(OffsetDateTime.now());

        responseDTO = new PackagingResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("Caja grande");
    }

    @Test
    void testCreatePackaging_Success() {
        PackagingCreateDTO createDTO = new PackagingCreateDTO();
        createDTO.setName("Caja mediana");
        createDTO.setPackagingMaterialId(1L);
        createDTO.setLabelingMaterialId(2L);
    createDTO.setUnitMeasurement(com.enigcode.frozen_backend.materials.model.UnitMeasurement.KG);
        createDTO.setQuantity(1.0);

        when(materialRepository.findById(1L)).thenReturn(Optional.of(new com.enigcode.frozen_backend.materials.model.Material() {{ setId(1L); setType(com.enigcode.frozen_backend.materials.model.MaterialType.ENVASE); }}));
        when(materialRepository.findById(2L)).thenReturn(Optional.of(new com.enigcode.frozen_backend.materials.model.Material() {{ setId(2L); setType(com.enigcode.frozen_backend.materials.model.MaterialType.ETIQUETADO); }}));
        when(packagingMapper.toEntity(createDTO)).thenReturn(packaging);
        when(packagingRepository.saveAndFlush(any(Packaging.class))).thenReturn(packaging);
        when(packagingMapper.toResponseDto(any(Packaging.class))).thenReturn(responseDTO);

        PackagingResponseDTO result = packagingService.createPackaging(createDTO);

        assertNotNull(result);
        assertEquals("Caja grande", result.getName());
        verify(packagingRepository).saveAndFlush(any(Packaging.class));
    }

    @Test
    void testToggleActive_Success() {
        packaging.setIsActive(true);

        when(packagingRepository.findById(1L)).thenReturn(Optional.of(packaging));
        when(packagingRepository.save(any(Packaging.class))).thenReturn(packaging);
        when(packagingMapper.toResponseDto(any(Packaging.class))).thenReturn(responseDTO);

        PackagingResponseDTO result = packagingService.toggleActive(1L);

        assertNotNull(result);
        verify(packagingRepository).save(packaging);
    }

    @Test
    void testToggleActive_NotFound() {
        when(packagingRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> packagingService.toggleActive(99L));
    }

    @Test
    void testFindAll_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Packaging> page = new PageImpl<>(List.of(packaging));

        when(packagingRepository.findAll(pageable)).thenReturn(page);
        when(packagingMapper.toResponseDto(any(Packaging.class))).thenReturn(responseDTO);

        Page<PackagingResponseDTO> result = packagingService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Caja grande", result.getContent().get(0).getName());
    }

    @Test
    void testGetPackaging_Success() {
        when(packagingRepository.findById(1L)).thenReturn(Optional.of(packaging));
        when(packagingMapper.toResponseDto(packaging)).thenReturn(responseDTO);

        PackagingResponseDTO result = packagingService.getPackaging(1L);

        assertNotNull(result);
        verify(packagingRepository).findById(1L);
    }

    @Test
    void testGetPackaging_NotFound() {
        when(packagingRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> packagingService.getPackaging(2L));
    }

    @Test
    void testGetPackagingList_FiltersActivePackagings() {
        // Given
        String searchName = "test";
        Boolean isActive = true;
        Long productId = null;
        
        // Create test data
        Packaging activePackaging = new Packaging();
        activePackaging.setId(1L);
        activePackaging.setName("Test Packaging");
        activePackaging.setIsActive(true);
        
        Packaging inactivePackaging = new Packaging();
        inactivePackaging.setId(2L);
        inactivePackaging.setName("Test Inactive");
        inactivePackaging.setIsActive(false);
        
        // Mock repository response
        when(packagingRepository.findTop10ByNameContainingIgnoreCaseAndIsActiveTrue(searchName))
            .thenReturn(List.of(activePackaging));
        
        // Mock mapper
        PackagingSimpleResponseDTO expectedResponse = new PackagingSimpleResponseDTO();
        expectedResponse.setId(1L);
        expectedResponse.setName("Test Packaging");
        when(packagingMapper.toSimpleResponseDTO(activePackaging)).thenReturn(expectedResponse);
        
        // When
        List<PackagingSimpleResponseDTO> result = packagingService.getPackagingList(searchName, isActive, productId);
        
        // Then
        assertEquals(1, result.size());
        assertEquals(expectedResponse, result.get(0));
        verify(packagingRepository).findTop10ByNameContainingIgnoreCaseAndIsActiveTrue(searchName);
        verify(packagingMapper).toSimpleResponseDTO(activePackaging);
    }

    @Test
    void testUpdatePackaging_Success() {
        PackagingUpdateDTO updateDTO = new PackagingUpdateDTO();
        updateDTO.setName("Caja nueva");

        when(packagingRepository.findById(1L)).thenReturn(Optional.of(packaging));
        when(packagingMapper.partialUpdate(updateDTO, packaging)).thenReturn(packaging);
        when(packagingRepository.save(packaging)).thenReturn(packaging);
        when(packagingMapper.toResponseDto(packaging)).thenReturn(responseDTO);

        PackagingResponseDTO result = packagingService.updatePackaging(1L, updateDTO);

        assertNotNull(result);
        verify(packagingRepository).save(packaging);
    }

    @Test
    void testUpdatePackaging_NotFound() {
        PackagingUpdateDTO updateDTO = new PackagingUpdateDTO();
        when(packagingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> packagingService.updatePackaging(99L, updateDTO));
    }

    // --- Additional tests: validations and branches ---

    @Test
    void testCreatePackaging_PackagingMaterialWrongType_ThrowsBadRequest() {
        PackagingCreateDTO createDTO = new PackagingCreateDTO();
        createDTO.setName("Caja");
        createDTO.setPackagingMaterialId(1L);
        createDTO.setLabelingMaterialId(2L);
        createDTO.setUnitMeasurement(com.enigcode.frozen_backend.materials.model.UnitMeasurement.KG);
        createDTO.setQuantity(1.0);

        com.enigcode.frozen_backend.materials.model.Material wrong = new com.enigcode.frozen_backend.materials.model.Material();
        wrong.setId(1L);
        wrong.setType(com.enigcode.frozen_backend.materials.model.MaterialType.OTROS);

        when(materialRepository.findById(1L)).thenReturn(Optional.of(wrong));
        when(materialRepository.findById(2L)).thenReturn(Optional.of(new com.enigcode.frozen_backend.materials.model.Material() {{ setId(2L); setType(com.enigcode.frozen_backend.materials.model.MaterialType.ETIQUETADO); }}));

        assertThrows(BadRequestException.class, () -> packagingService.createPackaging(createDTO));
    }

    @Test
    void testCreatePackaging_LabelingMaterialWrongType_ThrowsBadRequest() {
        PackagingCreateDTO createDTO = new PackagingCreateDTO();
        createDTO.setName("Caja");
        createDTO.setPackagingMaterialId(1L);
        createDTO.setLabelingMaterialId(2L);
        createDTO.setUnitMeasurement(com.enigcode.frozen_backend.materials.model.UnitMeasurement.KG);
        createDTO.setQuantity(1.0);

        when(materialRepository.findById(1L)).thenReturn(Optional.of(new com.enigcode.frozen_backend.materials.model.Material() {{ setId(1L); setType(com.enigcode.frozen_backend.materials.model.MaterialType.ENVASE); }}));
        com.enigcode.frozen_backend.materials.model.Material wrong = new com.enigcode.frozen_backend.materials.model.Material();
        wrong.setId(2L);
        wrong.setType(com.enigcode.frozen_backend.materials.model.MaterialType.OTROS);
        when(materialRepository.findById(2L)).thenReturn(Optional.of(wrong));

        assertThrows(BadRequestException.class, () -> packagingService.createPackaging(createDTO));
    }

    @Test
    void testCreatePackaging_UnitMeasurementUnidad_ThrowsBadRequest() {
        PackagingCreateDTO createDTO = new PackagingCreateDTO();
        createDTO.setName("Caja");
        createDTO.setPackagingMaterialId(1L);
        createDTO.setLabelingMaterialId(2L);
        createDTO.setUnitMeasurement(com.enigcode.frozen_backend.materials.model.UnitMeasurement.UNIDAD);
        createDTO.setQuantity(1.0);

        when(materialRepository.findById(1L)).thenReturn(Optional.of(new com.enigcode.frozen_backend.materials.model.Material() {{ setId(1L); setType(com.enigcode.frozen_backend.materials.model.MaterialType.ENVASE); }}));
        when(materialRepository.findById(2L)).thenReturn(Optional.of(new com.enigcode.frozen_backend.materials.model.Material() {{ setId(2L); setType(com.enigcode.frozen_backend.materials.model.MaterialType.ETIQUETADO); }}));

        assertThrows(BadRequestException.class, () -> packagingService.createPackaging(createDTO));
    }

    @Test
    void testGetPackagingList_NameNull_ReturnsEmpty() {
        List<com.enigcode.frozen_backend.packagings.DTO.PackagingSimpleResponseDTO> result = packagingService.getPackagingList(null, true, null);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetPackagingList_WithProductId_UsesProductUnitMeasurement() {
        String searchName = "pack";
        Long productId = 5L;

        Product product = new Product();
        product.setId(productId);
        product.setUnitMeasurement(com.enigcode.frozen_backend.materials.model.UnitMeasurement.KG);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        Packaging p = new Packaging();
        p.setId(10L);
        p.setName("Pack prueba");
        p.setUnitMeasurement(com.enigcode.frozen_backend.materials.model.UnitMeasurement.KG);

        when(packagingRepository.findTop10ByUnitMeasurementAndNameContainingIgnoreCase(com.enigcode.frozen_backend.materials.model.UnitMeasurement.KG, searchName))
                .thenReturn(List.of(p));

        com.enigcode.frozen_backend.packagings.DTO.PackagingSimpleResponseDTO dto = new com.enigcode.frozen_backend.packagings.DTO.PackagingSimpleResponseDTO();
        dto.setId(10L);
        dto.setName("Pack prueba");
        when(packagingMapper.toSimpleResponseDTO(p)).thenReturn(dto);

        List<com.enigcode.frozen_backend.packagings.DTO.PackagingSimpleResponseDTO> result = packagingService.getPackagingList(searchName, null, productId);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
        verify(packagingRepository).findTop10ByUnitMeasurementAndNameContainingIgnoreCase(com.enigcode.frozen_backend.materials.model.UnitMeasurement.KG, searchName);
    }

    @Test
    void testUpdatePackaging_PackagingMaterialChangeToNonEnvase_ThrowsBadRequest() {
        PackagingUpdateDTO updateDTO = new PackagingUpdateDTO();
        updateDTO.setPackagingMaterialId(2L);

        Packaging original = new Packaging();
        original.setId(1L);
        original.setPackagingMaterial(new com.enigcode.frozen_backend.materials.model.Material() {{ setId(1L); setType(com.enigcode.frozen_backend.materials.model.MaterialType.ENVASE); }});

        when(packagingRepository.findById(1L)).thenReturn(Optional.of(original));
        com.enigcode.frozen_backend.materials.model.Material wrong = new com.enigcode.frozen_backend.materials.model.Material();
        wrong.setId(2L);
        wrong.setType(com.enigcode.frozen_backend.materials.model.MaterialType.OTROS);
        when(materialRepository.findById(2L)).thenReturn(Optional.of(wrong));

        assertThrows(BadRequestException.class, () -> packagingService.updatePackaging(1L, updateDTO));
    }

    @Test
    void testUpdatePackaging_LabelingMaterialChangeToNonEtiquetado_ThrowsBadRequest() {
        PackagingUpdateDTO updateDTO = new PackagingUpdateDTO();
        updateDTO.setLabelingMaterialId(3L);

        Packaging original = new Packaging();
        original.setId(1L);
        original.setLabelingMaterial(new com.enigcode.frozen_backend.materials.model.Material() {{ setId(2L); setType(com.enigcode.frozen_backend.materials.model.MaterialType.ETIQUETADO); }});

        when(packagingRepository.findById(1L)).thenReturn(Optional.of(original));
        com.enigcode.frozen_backend.materials.model.Material wrong = new com.enigcode.frozen_backend.materials.model.Material();
        wrong.setId(3L);
        wrong.setType(com.enigcode.frozen_backend.materials.model.MaterialType.OTROS);
        when(materialRepository.findById(3L)).thenReturn(Optional.of(wrong));

        assertThrows(BadRequestException.class, () -> packagingService.updatePackaging(1L, updateDTO));
    }

    @Test
    void testUpdatePackaging_UnitMeasurementUnidad_ThrowsBadRequest() {
        PackagingUpdateDTO updateDTO = new PackagingUpdateDTO();
        updateDTO.setUnitMeasurement(com.enigcode.frozen_backend.materials.model.UnitMeasurement.UNIDAD);

        Packaging original = new Packaging();
        original.setId(1L);

        when(packagingRepository.findById(1L)).thenReturn(Optional.of(original));

        assertThrows(BadRequestException.class, () -> packagingService.updatePackaging(1L, updateDTO));
    }
}