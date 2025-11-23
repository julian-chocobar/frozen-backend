package com.enigcode.frozen_backend.movements.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.enigcode.frozen_backend.movements.DTO.MovementCreateDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementDetailDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementFilterDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementResponseDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementSimpleCreateDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementInternalCreateDTO;
import com.enigcode.frozen_backend.movements.mapper.MovementMapper;
import com.enigcode.frozen_backend.movements.model.Movement;
import com.enigcode.frozen_backend.movements.model.MovementType;
import com.enigcode.frozen_backend.movements.repository.MovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentMatchers;

@ExtendWith(MockitoExtension.class)
class MovementServiceImplTest {

    @Mock
    private MovementRepository movementRepository;
    @Mock
    private MaterialRepository materialRepository;
    @Mock
    private MovementMapper movementMapper;
    @Mock
    private com.enigcode.frozen_backend.users.service.UserService userService;
        @Mock
        private com.enigcode.frozen_backend.notifications.service.NotificationService notificationService;

    @InjectMocks
    private MovementServiceImpl movementService;

    private Material material;
    private Movement movement;
    private MovementResponseDTO responseDTO;
    private Movement pendingMovement;
    private Movement inProcessMovement;
    private Movement egresoMovementLowStock;

    @BeforeEach
    void setUp() {
        material = new Material();
        material.setId(1L);
        material.setStock(50.0);
        material.setReservedStock(0.0);
        material.setThreshold(10.0); // Para pruebas de low stock notification

        movement = Movement.builder()
                .id(1L)
                .type(MovementType.INGRESO)
                .stock(10.0)
                .material(material)
                .build();

    // Movimiento pendiente para pruebas de completeMovement
    pendingMovement = Movement.builder()
        .id(2L)
        .type(MovementType.INGRESO)
        .stock(5.0)
        .material(material)
        .status(com.enigcode.frozen_backend.movements.model.MovementStatus.PENDIENTE)
        .build();

    // Movimiento en proceso para pruebas de toggle y completion
    inProcessMovement = Movement.builder()
        .id(3L)
        .type(MovementType.INGRESO)
        .stock(7.0)
        .material(material)
        .status(com.enigcode.frozen_backend.movements.model.MovementStatus.EN_PROCESO)
        .inProgressByUserId(1L)
        .build();

    // Movimiento EGRESO que dejará el stock bajo el umbral
    egresoMovementLowStock = Movement.builder()
        .id(4L)
        .type(MovementType.EGRESO)
        .stock(45.0) // dejará stock en 5 (< threshold 10)
        .material(material)
        .status(com.enigcode.frozen_backend.movements.model.MovementStatus.PENDIENTE)
        .build();

        responseDTO = new MovementResponseDTO();
        responseDTO.setId(1L);

        // Mock user for createMovement
        com.enigcode.frozen_backend.users.model.User mockUser = new com.enigcode.frozen_backend.users.model.User();
        mockUser.setId(1L);
        org.mockito.Mockito.lenient().when(userService.getCurrentUser()).thenReturn(mockUser);
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

        when(movementRepository.findAll(ArgumentMatchers.<Specification<Movement>>any(), any(Pageable.class))).thenReturn(page);
        when(movementMapper.toResponseDto(any(Movement.class))).thenReturn(responseDTO);

        Page<MovementResponseDTO> result = movementService.findAll(new MovementFilterDTO(), pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getId());
    }

    @Test
    void testCreateReserveOrReturn_Reserva_Success() {
        Material material1 = new Material();
        material1.setId(1L);
        material1.setStock(100.0);
        material1.setReservedStock(0.0);

        MovementSimpleCreateDTO dto = new MovementSimpleCreateDTO();
        dto.setMaterial(material1);
        dto.setStock(20.0);

        List<MovementSimpleCreateDTO> materials = List.of(dto);

        when(movementRepository.saveAllAndFlush(anyList())).thenReturn(new ArrayList<>());

        movementService.createReserveOrReturn(MovementType.RESERVA, materials);

        assertEquals(80.0, material1.getStock()); // stock disponible reducido
        assertEquals(20.0, material1.getReservedStock()); // stock reservado incrementado
        verify(movementRepository).saveAllAndFlush(anyList());
    }

    @Test
    void testCreateReserveOrReturn_Reserva_StockInsuficiente() {
        Material material1 = new Material();
        material1.setId(1L);
        material1.setStock(10.0);
        material1.setReservedStock(0.0);

        MovementSimpleCreateDTO dto = new MovementSimpleCreateDTO();
        dto.setMaterial(material1);
        dto.setStock(20.0); // más de lo disponible

        List<MovementSimpleCreateDTO> materials = List.of(dto);

        assertThrows(BadRequestException.class, 
            () -> movementService.createReserveOrReturn(MovementType.RESERVA, materials));
    }

    @Test
    void testCreateReserveOrReturn_Devuelto_Success() {
        Material material1 = new Material();
        material1.setId(1L);
        material1.setStock(80.0);
        material1.setReservedStock(20.0);

        MovementSimpleCreateDTO dto = new MovementSimpleCreateDTO();
        dto.setMaterial(material1);
        dto.setStock(15.0);

        List<MovementSimpleCreateDTO> materials = List.of(dto);

        when(movementRepository.saveAllAndFlush(anyList())).thenReturn(new ArrayList<>());

        movementService.createReserveOrReturn(MovementType.DEVUELTO, materials);

        assertEquals(95.0, material1.getStock()); // stock disponible incrementado
        assertEquals(5.0, material1.getReservedStock()); // stock reservado reducido
        verify(movementRepository).saveAllAndFlush(anyList());
    }

    @Test
    void testCreateReserveOrReturn_Devuelto_ReservedStockInsuficiente() {
        Material material1 = new Material();
        material1.setId(1L);
        material1.setStock(80.0);
        material1.setReservedStock(5.0);

        MovementSimpleCreateDTO dto = new MovementSimpleCreateDTO();
        dto.setMaterial(material1);
        dto.setStock(15.0); // más de lo reservado

        List<MovementSimpleCreateDTO> materials = List.of(dto);

        assertThrows(BadRequestException.class, 
            () -> movementService.createReserveOrReturn(MovementType.DEVUELTO, materials));
    }

    @Test
    void testCreateReserveOrReturn_TipoInvalido() {
        Material material1 = new Material();
        material1.setId(1L);
        material1.setStock(100.0);
        material1.setReservedStock(0.0);

        MovementSimpleCreateDTO dto = new MovementSimpleCreateDTO();
        dto.setMaterial(material1);
        dto.setStock(20.0);

        List<MovementSimpleCreateDTO> materials = List.of(dto);

        assertThrows(BadRequestException.class, 
            () -> movementService.createReserveOrReturn(MovementType.INGRESO, materials));
    }

    @Test
    void testConfirmReservation_Success() {
        Material material1 = new Material();
        material1.setId(1L);
        material1.setStock(80.0);
        material1.setReservedStock(20.0);

        MovementSimpleCreateDTO dto = new MovementSimpleCreateDTO();
        dto.setMaterial(material1);
        dto.setStock(15.0);

        List<MovementSimpleCreateDTO> materials = List.of(dto);

        when(movementRepository.saveAllAndFlush(anyList())).thenReturn(new ArrayList<>());

        movementService.confirmReservation(materials);

        assertEquals(80.0, material1.getStock()); // stock disponible no cambia
        assertEquals(5.0, material1.getReservedStock()); // stock reservado reducido
        verify(movementRepository).saveAllAndFlush(anyList());
    }

    @Test
    void testConfirmReservation_ReservedStockInsuficiente() {
        Material material1 = new Material();
        material1.setId(1L);
        material1.setStock(80.0);
        material1.setReservedStock(10.0);

        MovementSimpleCreateDTO dto = new MovementSimpleCreateDTO();
        dto.setMaterial(material1);
        dto.setStock(15.0); // más de lo reservado

        List<MovementSimpleCreateDTO> materials = List.of(dto);

        assertThrows(BadRequestException.class, 
            () -> movementService.confirmReservation(materials));
    }

    // --- Nuevos tests para completeMovement y toggleInProgressPending ---

    @Test
    void testCompleteMovement_Ingreso_Success() {
        when(movementRepository.findById(2L)).thenReturn(Optional.of(pendingMovement));
        when(movementMapper.toResponseDto(any(Movement.class))).thenReturn(responseDTO);
        when(materialRepository.save(any(Material.class))).thenReturn(material);
        when(movementRepository.save(any(Movement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MovementResponseDTO result = movementService.completeMovement(2L);

        assertNotNull(result);
        assertEquals(1L, responseDTO.getId());
        assertEquals(55.0, material.getStock()); // ingreso aumenta stock
        verify(notificationService, never()).createLowStockNotification(anyLong(), anyString(), anyDouble(), anyDouble());
    }

    @Test
    void testCompleteMovement_Egreso_TriggersLowStockNotification() {
        when(movementRepository.findById(4L)).thenReturn(Optional.of(egresoMovementLowStock));
        when(movementMapper.toResponseDto(any(Movement.class))).thenReturn(responseDTO);
        when(materialRepository.save(any(Material.class))).thenReturn(material);
        when(movementRepository.save(any(Movement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        movementService.completeMovement(4L);

        assertEquals(5.0, material.getStock()); // egreso reduce stock
        verify(notificationService).createLowStockNotification(material.getId(), material.getName(), 5.0, 10.0);
    }

    @Test
    void testCompleteMovement_Egreso_StockInsuficienteAtCompletion() {
        // Ajustamos material para que stock sea insuficiente al completar
        material.setStock(40.0);
        when(movementRepository.findById(4L)).thenReturn(Optional.of(egresoMovementLowStock));

        assertThrows(BadRequestException.class, () -> movementService.completeMovement(4L));
    }

    @Test
    void testToggleInProgress_FromPendienteToEnProceso() {
        when(movementRepository.findById(2L)).thenReturn(Optional.of(pendingMovement));
        when(movementRepository.save(any(Movement.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movementMapper.toResponseDto(any(Movement.class))).thenReturn(responseDTO);

        MovementResponseDTO result = movementService.toggleInProgressPending(2L);

        assertNotNull(result);
        assertEquals(com.enigcode.frozen_backend.movements.model.MovementStatus.EN_PROCESO, pendingMovement.getStatus());
        assertEquals(1L, pendingMovement.getInProgressByUserId());
    }

    @Test
    void testToggleInProgress_FromEnProcesoToPendiente_SameUser() {
        when(movementRepository.findById(3L)).thenReturn(Optional.of(inProcessMovement));
        when(movementRepository.save(any(Movement.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movementMapper.toResponseDto(any(Movement.class))).thenReturn(responseDTO);

        MovementResponseDTO result = movementService.toggleInProgressPending(3L);

        assertNotNull(result);
        assertEquals(com.enigcode.frozen_backend.movements.model.MovementStatus.PENDIENTE, inProcessMovement.getStatus());
        assertNull(inProcessMovement.getInProgressByUserId());
    }

    @Test
    void testToggleInProgress_CompletedMovementThrows() {
        Movement completed = Movement.builder()
                .id(5L)
                .type(MovementType.INGRESO)
                .stock(5.0)
                .material(material)
                .status(com.enigcode.frozen_backend.movements.model.MovementStatus.COMPLETADO)
                .build();
        when(movementRepository.findById(5L)).thenReturn(Optional.of(completed));

        assertThrows(BadRequestException.class, () -> movementService.toggleInProgressPending(5L));
    }

    @Test
    void testToggleInProgress_RevertByDifferentUserThrows() {
        inProcessMovement.setInProgressByUserId(2L); // movimiento tomado por otro usuario
        com.enigcode.frozen_backend.users.model.User mockUser = new com.enigcode.frozen_backend.users.model.User();
        mockUser.setId(1L); // usuario actual distinto
        when(userService.getCurrentUser()).thenReturn(mockUser);
        when(movementRepository.findById(3L)).thenReturn(Optional.of(inProcessMovement));

        assertThrows(BadRequestException.class, () -> movementService.toggleInProgressPending(3L));
    }

    @Test
    void testCreateMovements_bulkCreate_assignsCreatedBy() {
        com.enigcode.frozen_backend.users.model.User mockUser = new com.enigcode.frozen_backend.users.model.User();
        mockUser.setId(42L);
        when(userService.getCurrentUser()).thenReturn(mockUser);

        com.enigcode.frozen_backend.materials.model.Material mat1 = new com.enigcode.frozen_backend.materials.model.Material();
        mat1.setId(11L);
        MovementInternalCreateDTO dto = new MovementInternalCreateDTO();
        dto.setMaterial(mat1);
        dto.setStock(3.0);
        dto.setType(MovementType.INGRESO);

        when(movementRepository.saveAll(anyList())).thenReturn(List.of());

        movementService.createMovements(List.of(dto));

        // verify that saveAll was invoked
        verify(movementRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testCompleteMovement_EnProceso_OtherUserThrows() {
        // movement in EN_PROCESO by user 2
        Movement m = Movement.builder()
                .id(99L)
                .type(MovementType.INGRESO)
                .stock(1.0)
                .material(material)
                .status(com.enigcode.frozen_backend.movements.model.MovementStatus.EN_PROCESO)
                .inProgressByUserId(2L)
                .build();

        com.enigcode.frozen_backend.users.model.User current = new com.enigcode.frozen_backend.users.model.User();
        current.setId(3L); // different user
        when(userService.getCurrentUser()).thenReturn(current);

        when(movementRepository.findById(99L)).thenReturn(Optional.of(m));

        assertThrows(BadRequestException.class, () -> movementService.completeMovement(99L));
    }
}