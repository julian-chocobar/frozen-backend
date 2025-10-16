package com.enigcode.frozen_backend.movements.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.enigcode.frozen_backend.movements.DTO.MovementCreateDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementDetailDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementFilterDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementResponseDTO;
import com.enigcode.frozen_backend.movements.mapper.MovementMapper;
import com.enigcode.frozen_backend.movements.model.Movement;
import com.enigcode.frozen_backend.movements.model.MovementType;
import com.enigcode.frozen_backend.movements.repository.MovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovementServiceImplTest {

    @Mock
    private MovementRepository movementRepository;
    @Mock
    private MaterialRepository materialRepository;
    @Mock
    private MovementMapper movementMapper;

    @InjectMocks
    private MovementServiceImpl movementService;

    private Material material;
    private Movement movement;
    private MovementResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        material = new Material();
        material.setId(1L);
        material.setStock(50.0);

        movement = Movement.builder()
                .id(1L)
                .type(MovementType.INGRESO)
                .stock(10.0)
                .material(material)
                .build();

        responseDTO = new MovementResponseDTO();
        responseDTO.setId(1L);
    }

    @Test
    void testCreateMovement_Ingreso_Success() {
        MovementCreateDTO createDTO = new MovementCreateDTO();
        createDTO.setMaterialId(1L);
        createDTO.setStock(10.0);
        createDTO.setType(MovementType.INGRESO);
        createDTO.setReason("Compra");

        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(movementRepository.saveAndFlush(any(Movement.class))).thenReturn(movement);
        when(movementMapper.toResponseDto(any(Movement.class))).thenReturn(responseDTO);

        MovementResponseDTO result = movementService.createMovement(createDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(materialRepository).save(material);
        verify(movementRepository).saveAndFlush(any(Movement.class));
    }

    @Test
    void testCreateMovement_Egreso_Success() {
        MovementCreateDTO createDTO = new MovementCreateDTO();
        createDTO.setMaterialId(1L);
        createDTO.setStock(20.0);
        createDTO.setType(MovementType.EGRESO);
        createDTO.setReason("Venta");

        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(movementRepository.saveAndFlush(any(Movement.class))).thenReturn(movement);
        when(movementMapper.toResponseDto(any(Movement.class))).thenReturn(responseDTO);

        MovementResponseDTO result = movementService.createMovement(createDTO);

        assertNotNull(result);
        verify(materialRepository).save(material);
        verify(movementRepository).saveAndFlush(any(Movement.class));
    }

    @Test
    void testCreateMovement_Egreso_StockInsuficiente() {
        MovementCreateDTO createDTO = new MovementCreateDTO();
        createDTO.setMaterialId(1L);
        createDTO.setStock(100.0); // mayor al stock actual
        createDTO.setType(MovementType.EGRESO);

        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));

        assertThrows(BadRequestException.class, () -> movementService.createMovement(createDTO));
    }

    @Test
    void testCreateMovement_MaterialNoEncontrado() {
        MovementCreateDTO createDTO = new MovementCreateDTO();
        createDTO.setMaterialId(999L);
        createDTO.setType(MovementType.INGRESO);

        when(materialRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movementService.createMovement(createDTO));
    }

    @Test
    void testGetMovement_Success() {
        when(movementRepository.findById(1L)).thenReturn(Optional.of(movement));
        when(movementMapper.toDetailDTO(movement)).thenReturn(new MovementDetailDTO());

        MovementDetailDTO result = movementService.getMovement(1L);

        assertNotNull(result);
        verify(movementRepository).findById(1L);
    }

    @Test
    void testGetMovement_NotFound() {
        when(movementRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> movementService.getMovement(2L));
    }

    @Test
    void testFindAll_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movement> page = new PageImpl<>(List.of(movement));

        when(movementRepository.findAll(any(), eq(pageable))).thenReturn(page);
        when(movementMapper.toResponseDto(any(Movement.class))).thenReturn(responseDTO);

        Page<MovementResponseDTO> result = movementService.findAll(new MovementFilterDTO(), pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getId());
    }
}