package com.enigcode.frozen_backend.packagings.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.packagings.DTO.*;
import com.enigcode.frozen_backend.packagings.mapper.PackagingMapper;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.packagings.repository.PackagingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PackagingServiceImplTest {

    @Mock
    private PackagingRepository packagingRepository;
    @Mock
    private PackagingMapper packagingMapper;

    @InjectMocks
    private PackagingServiceImpl packagingService;

    private Packaging packaging;
    private PackagingResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

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
    void testGetActivePackagingList() {
        Packaging inactive = new Packaging();
        inactive.setId(2L);
        inactive.setIsActive(false);

        when(packagingRepository.findAll()).thenReturn(List.of(packaging, inactive));
        when(packagingMapper.toSimpleResponseDTO(packaging)).thenReturn(new PackagingSimpleResponseDTO());

        List<PackagingSimpleResponseDTO> result = packagingService.getActivePackagingList();

        assertEquals(1, result.size());
        verify(packagingRepository).findAll();
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
}