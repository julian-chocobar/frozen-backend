package com.enigcode.frozen_backend.product_phases.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseResponseDTO;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseUpdateDTO;
import com.enigcode.frozen_backend.product_phases.mapper.ProductPhaseMapper;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import com.enigcode.frozen_backend.product_phases.repository.ProductPhaseRepository;
import com.enigcode.frozen_backend.products.repository.ProductRepository;
import com.enigcode.frozen_backend.recipes.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductPhaseServiceImplTest {

    @Mock
    private ProductPhaseRepository productPhaseRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private ProductPhaseMapper productPhaseMapper;

    @InjectMocks
    private ProductPhaseServiceImpl productPhaseService;

    private ProductPhase productPhase;
    private ProductPhaseResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productPhase = new ProductPhase();
        productPhase.setId(1L);
        productPhase.setIsReady(false);

        responseDTO = new ProductPhaseResponseDTO();
        responseDTO.setId(1L);
    }

    @Test
    void testUpdateProductPhase_Success() {
        ProductPhaseUpdateDTO updateDTO = new ProductPhaseUpdateDTO();

        when(productPhaseRepository.findById(1L)).thenReturn(Optional.of(productPhase));
        when(productPhaseRepository.save(any(ProductPhase.class))).thenReturn(productPhase);
        when(productPhaseMapper.toResponseDto(productPhase)).thenReturn(responseDTO);

        ProductPhaseResponseDTO result = productPhaseService.updateProductPhase(1L, updateDTO);

        assertNotNull(result);
        verify(productPhaseRepository).save(productPhase);
    }

    @Test
    void testUpdateProductPhase_NotFound() {
        when(productPhaseRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> productPhaseService.updateProductPhase(99L, new ProductPhaseUpdateDTO()));
    }

    @Test
    void testFindAll_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductPhase> page = new PageImpl<>(List.of(productPhase));

        when(productPhaseRepository.findAll(pageable)).thenReturn(page);
        when(productPhaseMapper.toResponseDto(any(ProductPhase.class))).thenReturn(responseDTO);

        Page<ProductPhaseResponseDTO> result = productPhaseService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        verify(productPhaseRepository).findAll(pageable);
    }

    @Test
    void testGetProductPhase_Success() {
        when(productPhaseRepository.findById(1L)).thenReturn(Optional.of(productPhase));
        when(productPhaseMapper.toResponseDto(productPhase)).thenReturn(responseDTO);

        ProductPhaseResponseDTO result = productPhaseService.getProductPhase(1L);

        assertEquals(1L, result.getId());
        verify(productPhaseRepository).findById(1L);
    }

    @Test
    void testGetProductPhase_NotFound() {
        when(productPhaseRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productPhaseService.getProductPhase(2L));
    }

    @Test
    void testGetByProduct_Success() {
        when(productPhaseRepository.findByProductIdOrderByCreationDateAsc(10L))
                .thenReturn(List.of(productPhase));
        when(productPhaseMapper.toResponseDto(any(ProductPhase.class))).thenReturn(responseDTO);

        List<ProductPhaseResponseDTO> result = productPhaseService.getByProduct(10L);

        assertEquals(1, result.size());
        verify(productPhaseRepository).findByProductIdOrderByCreationDateAsc(10L);
    }

    @Test
    void testGetByProduct_EmptyAndProductNotExists() {
        when(productPhaseRepository.findByProductIdOrderByCreationDateAsc(10L))
                .thenReturn(Collections.emptyList());
        when(productRepository.existsById(10L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> productPhaseService.getByProduct(10L));
    }

    @Test
    void testMarkAsReady_Success() {
        productPhase.setIsReady(false);
        // productPhase.setRequiredMaterials(Collections.emptyList());
        when(productPhaseRepository.findById(1L)).thenReturn(Optional.of(productPhase));
        when(productPhaseRepository.save(productPhase)).thenReturn(productPhase);
        when(productPhaseMapper.toResponseDto(productPhase)).thenReturn(responseDTO);

        doReturn(true).when(productPhase).isComplete();

        ProductPhaseResponseDTO result = productPhaseService.markAsReady(1L);

        assertNotNull(result);
        verify(productPhaseRepository).save(productPhase);
        assertTrue(productPhase.getIsReady());
    }

    @Test
    void testMarkAsReady_NotFound() {
        when(productPhaseRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productPhaseService.markAsReady(1L));
    }

    @Test
    void testMarkAsReady_IncompletePhase() {
        when(productPhaseRepository.findById(1L)).thenReturn(Optional.of(productPhase));
        doReturn(false).when(productPhase).isComplete();

        assertThrows(BadRequestException.class, () -> productPhaseService.markAsReady(1L));
    }

    @Test
    void testMarkAsReady_MissingMaterials() {
        when(productPhaseRepository.findById(1L)).thenReturn(Optional.of(productPhase));

        doReturn(true).when(productPhase).isComplete();
        // productPhase.setRequiredMaterials(List.of(MaterialType.AGUA));

        when(recipeRepository.existsByMaterial_Type(MaterialType.AGUA)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> productPhaseService.markAsReady(1L));
    }
}