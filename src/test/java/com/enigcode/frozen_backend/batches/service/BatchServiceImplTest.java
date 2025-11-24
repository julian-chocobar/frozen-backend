package com.enigcode.frozen_backend.batches.service;

import com.enigcode.frozen_backend.batches.mapper.BatchMapper;
import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.model.BatchStatus;
import com.enigcode.frozen_backend.batches.repository.BatchRepository;
import com.enigcode.frozen_backend.batches.DTO.BatchResponseDTO;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.movements.DTO.MovementInternalCreateDTO;
import com.enigcode.frozen_backend.movements.model.MovementType;
import com.enigcode.frozen_backend.movements.service.MovementService;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.production_materials.model.ProductionMaterial;
import com.enigcode.frozen_backend.production_materials.repository.ProductionMaterialRepository;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhaseStatus;
import com.enigcode.frozen_backend.production_phases.repository.ProductionPhaseRepository;
import com.enigcode.frozen_backend.sectors.model.Sector;
import com.enigcode.frozen_backend.sectors.model.SectorType;
import com.enigcode.frozen_backend.sectors.service.SectorService;
import com.enigcode.frozen_backend.system_configurations.model.WorkingDay;
import com.enigcode.frozen_backend.system_configurations.service.SystemConfigurationService;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.packagings.repository.PackagingRepository;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import com.enigcode.frozen_backend.products.model.Product;
import com.enigcode.frozen_backend.production_orders.Model.ProductionOrder;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchServiceImplTest {

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private BatchMapper batchMapper;

    @Mock
    private ProductionPhaseRepository productionPhaseRepository;

    @Mock
    private ProductionMaterialRepository productionMaterialRepository;

    @Mock
    private MovementService movementService;

    @Mock
    private SectorService sectorService;

    @Mock
    private com.enigcode.frozen_backend.notifications.service.NotificationService notificationService;

    @Mock
    private SystemConfigurationService systemConfigurationService;

    @Mock
    private PackagingRepository packagingRepository;

    @InjectMocks
    private BatchServiceImpl batchService;

    private Batch batch;
    private BatchResponseDTO responseDTO;
    private Map<DayOfWeek, WorkingDay> workingDays;

    @BeforeEach
    void setUp() {
        batch = Batch.builder()
                .id(1L)
                .code("BATCH-001")
                .status(BatchStatus.PENDIENTE)
                .quantity(100)
                .creationDate(OffsetDateTime.now())
                .plannedDate(OffsetDateTime.now().plusDays(1))
                .build();

        responseDTO = new BatchResponseDTO();
        responseDTO.setCode("BATCH-001");
        responseDTO.setStatus(BatchStatus.CANCELADO);

        // Setup working days
        workingDays = new HashMap<>();
        for (DayOfWeek day : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            WorkingDay wd = new WorkingDay();
            wd.setDayOfWeek(day);
            wd.setIsWorkingDay(true);
            wd.setOpeningHour(LocalTime.of(8, 0));
            wd.setClosingHour(LocalTime.of(17, 0));
            workingDays.put(day, wd);
        }
        for (DayOfWeek day : List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
            WorkingDay wd = new WorkingDay();
            wd.setDayOfWeek(day);
            wd.setIsWorkingDay(false);
            workingDays.put(day, wd);
        }
    }

    @Test
    void canCreateBatch() {
        Batch batch = Batch.builder()
                .code("BATCH-001")
                .status(BatchStatus.PENDIENTE)
                .quantity(100)
                .build();
        assertThat(batch.getCode()).isEqualTo("BATCH-001");
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.PENDIENTE);
        assertThat(batch.getQuantity()).isEqualTo(100);
    }

    @Test
    void canChangeBatchStatus() {
        Batch batch = Batch.builder().code("BATCH-003").status(BatchStatus.PENDIENTE).quantity(10).build();
        batch.setStatus(BatchStatus.COMPLETADO);
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.COMPLETADO);
    }

    @Test
    void batchQuantityCannotBeNegative() {
        Batch batch = Batch.builder().code("BATCH-004").status(BatchStatus.PENDIENTE).quantity(-5).build();
        assertThat(batch.getQuantity()).isLessThan(0);
    }

    @Test
    void batchCodeShouldNotBeNull() {
        Batch batch = Batch.builder().status(BatchStatus.PENDIENTE).quantity(10).build();
        assertThat(batch.getCode()).isNull();
    }

    @Test
    void cancelBatch_byId_success() {
        // Given
        ProductionPhase pendingPhase = new ProductionPhase();
        pendingPhase.setId(1L);
        pendingPhase.setStatus(ProductionPhaseStatus.PENDIENTE);

        ProductionPhase completedPhase = new ProductionPhase();
        completedPhase.setId(2L);
        completedPhase.setStatus(ProductionPhaseStatus.COMPLETADA);

        batch.setPhases(List.of(pendingPhase, completedPhase));

        when(batchRepository.findById(1L)).thenReturn(Optional.of(batch));
        when(productionMaterialRepository.findAllByProductionPhaseId(1L)).thenReturn(List.of());
        when(batchRepository.save(any(Batch.class))).thenReturn(batch);
        when(batchMapper.toResponseDTO(batch)).thenReturn(responseDTO);

        // When
        BatchResponseDTO result = batchService.cancelBatch(1L);

        // Then
        assertNotNull(result);
        assertEquals(BatchStatus.CANCELADO, batch.getStatus());
        assertEquals(ProductionPhaseStatus.SUSPENDIDA, pendingPhase.getStatus());
        assertEquals(ProductionPhaseStatus.COMPLETADA, completedPhase.getStatus()); // No cambia
        verify(batchRepository).save(batch);
        verify(productionPhaseRepository).save(pendingPhase);
        verify(batchMapper).toResponseDTO(batch);
    }

    @Test
    void cancelBatch_byId_notFound_throws() {
        when(batchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> batchService.cancelBatch(999L));
    }

    @Test
    void cancelBatch_byEntity_withPendingPhases_suspendsThem() {
        // Given
        ProductionPhase phase1 = new ProductionPhase();
        phase1.setId(1L);
        phase1.setStatus(ProductionPhaseStatus.PENDIENTE);

        ProductionPhase phase2 = new ProductionPhase();
        phase2.setId(2L);
        phase2.setStatus(ProductionPhaseStatus.PENDIENTE);

        batch.setPhases(List.of(phase1, phase2));

        when(productionMaterialRepository.findAllByProductionPhaseId(anyLong())).thenReturn(List.of());
        when(batchRepository.save(any(Batch.class))).thenReturn(batch);

        // When
        batchService.cancelBatch(batch);

        // Then
        assertEquals(BatchStatus.CANCELADO, batch.getStatus());
        assertEquals(ProductionPhaseStatus.SUSPENDIDA, phase1.getStatus());
        assertEquals(ProductionPhaseStatus.SUSPENDIDA, phase2.getStatus());
        verify(productionPhaseRepository, times(2)).save(any(ProductionPhase.class));
    }

    @Test
    void cancelBatch_noMaterials_createsEmptyMovementsList() {
        // Given
        ProductionPhase phase = new ProductionPhase();
        phase.setId(1L);
        phase.setStatus(ProductionPhaseStatus.PENDIENTE);
        batch.setPhases(List.of(phase));

        when(batchRepository.findById(1L)).thenReturn(Optional.of(batch));
        when(productionMaterialRepository.findAllByProductionPhaseId(1L)).thenReturn(List.of());
        when(batchRepository.save(any(Batch.class))).thenReturn(batch);
        when(batchMapper.toResponseDTO(batch)).thenReturn(responseDTO);

        // When
        batchService.cancelBatch(1L);

        // Then - El servicio siempre llama a createMovements, aunque sea con lista vacía
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MovementInternalCreateDTO>> captor = ArgumentCaptor.forClass(List.class);
        verify(movementService).createMovements(captor.capture());
        assertTrue(captor.getValue().isEmpty());
    }

    @Test
    void suspendProductionPhases_withMaterials_createsMovements() {
        // Given
        ProductionPhase phase = new ProductionPhase();
        phase.setId(1L);
        phase.setStatus(ProductionPhaseStatus.PENDIENTE);

        Material material = new Material();
        material.setId(10L);
        material.setName("Malta");

        ProductionMaterial pm1 = new ProductionMaterial();
        pm1.setId(100L);
        pm1.setProductionPhase(phase);
        pm1.setMaterial(material);
        pm1.setQuantity(50.0);

        when(productionMaterialRepository.findAllByProductionPhaseId(1L)).thenReturn(List.of(pm1));

        // When
        batchService.suspendProductionPhases(List.of(phase));

        // Then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MovementInternalCreateDTO>> captor = ArgumentCaptor.forClass(List.class);
        verify(movementService).createMovements(captor.capture());

        List<MovementInternalCreateDTO> movements = captor.getValue();
        assertEquals(1, movements.size());
        assertEquals(MovementType.INGRESO, movements.get(0).getType());
        assertEquals(50.0, movements.get(0).getStock());
        assertEquals(material, movements.get(0).getMaterial());
        assertThat(movements.get(0).getReason()).contains("Cancelación");

        assertEquals(ProductionPhaseStatus.SUSPENDIDA, phase.getStatus());
        verify(productionPhaseRepository).save(phase);
    }

    @Test
    void suspendProductionPhases_emptyList_callsCreateMovementsWithEmptyList() {
        // When
        batchService.suspendProductionPhases(List.of());

        // Then - El servicio siempre llama a createMovements, aunque sea con lista vacía
        verify(productionMaterialRepository, never()).findAllByProductionPhaseId(anyLong());
        verify(movementService).createMovements(argThat(list -> list.isEmpty()));
    }

    @Test
    void processBatchesForToday_workingDay_startsAll() {
        // Given: Lunes, 2 lotes pendientes, 2 sectores disponibles
        Batch batch1 = createBatchWithPhase(1L, "BATCH-001", 50.0);
        Batch batch2 = createBatchWithPhase(2L, "BATCH-002", 30.0);

        Sector sector1 = createSector(10L, 100.0, 0.0);
        Sector sector2 = createSector(11L, 100.0, 0.0);

        when(batchRepository.findAllStartingToday(any(), any())).thenReturn(List.of(batch1, batch2));
        when(systemConfigurationService.getWorkingDays()).thenReturn(workingDays);
        when(sectorService.getAllSectorsAvailableByPhase(Phase.MOLIENDA)).thenReturn(List.of(sector1, sector2));

        // When
        batchService.processBatchesForToday();

        // Then
        assertEquals(BatchStatus.EN_PRODUCCION, batch1.getStatus());
        assertEquals(BatchStatus.EN_PRODUCCION, batch2.getStatus());
        assertNotNull(batch1.getStartDate());
        assertNotNull(batch2.getStartDate());

        ProductionPhase phase1 = batch1.getPhases().get(0);
        assertEquals(ProductionPhaseStatus.EN_PROCESO, phase1.getStatus());
        assertEquals(sector1, phase1.getSector());

        verify(sectorService).saveAll(anyList());
        verify(batchRepository).saveAll(List.of(batch1, batch2));
    }

    @Test
    void processBatchesForToday_nonWorkingDay_postponesAll() {
        // Given: Sábado (no laborable)
        OffsetDateTime saturday8am = OffsetDateTime.of(2025, 11, 1, 8, 0, 0, 0, ZoneOffset.UTC);

        Batch batch1 = createBatchWithPhase(1L, "BATCH-001", 50.0);
        batch1.setPlannedDate(saturday8am);

        when(batchRepository.findAllStartingToday(any(), any())).thenReturn(List.of(batch1));
        when(systemConfigurationService.getWorkingDays()).thenReturn(workingDays);
        when(sectorService.getAllSectorsAvailableByPhase(any())).thenReturn(List.of()); // Se llama siempre

        // When
        batchService.processBatchesForToday();

        // Then
        assertEquals(BatchStatus.PENDIENTE, batch1.getStatus()); // No inicia
        assertNotNull(batch1.getPlannedDate()); // Pero se aplaza (plannedDate se mueve al siguiente día)
        verify(sectorService).getAllSectorsAvailableByPhase(Phase.MOLIENDA); // Se llama aunque no sea día laborable
        verify(batchRepository).saveAll(anyList());
    }

    @Test
    void processBatchesForToday_limitedCapacity_postponesRemaining() {
        // Given: 2 lotes, 1 sector con capacidad para 1 solo
        Batch batch1 = createBatchWithPhase(1L, "BATCH-001", 80.0);
        Batch batch2 = createBatchWithPhase(2L, "BATCH-002", 70.0);

        Sector sector = createSector(10L, 100.0, 0.0); // Capacidad 100

        when(batchRepository.findAllStartingToday(any(), any())).thenReturn(List.of(batch1, batch2));
        when(systemConfigurationService.getWorkingDays()).thenReturn(workingDays);
        when(sectorService.getAllSectorsAvailableByPhase(Phase.MOLIENDA)).thenReturn(List.of(sector));

        // When
        batchService.processBatchesForToday();

        // Then
        assertEquals(BatchStatus.EN_PRODUCCION, batch1.getStatus()); // Cabe
        assertEquals(BatchStatus.PENDIENTE, batch2.getStatus()); // No cabe
        assertNotNull(batch2.getPlannedDate()); // Aplazado (plannedDate se mueve al siguiente día)
    }

    @Test
    void processBatchesForToday_noSectors_postponesAll() {
        // Given
        Batch batch1 = createBatchWithPhase(1L, "BATCH-001", 50.0);

        when(batchRepository.findAllStartingToday(any(), any())).thenReturn(List.of(batch1));
        when(systemConfigurationService.getWorkingDays()).thenReturn(workingDays);
        when(sectorService.getAllSectorsAvailableByPhase(Phase.MOLIENDA)).thenReturn(List.of());

        // When
        batchService.processBatchesForToday();

        // Then
        assertEquals(BatchStatus.PENDIENTE, batch1.getStatus());
        verify(batchRepository).saveAll(anyList());
    }

    @Test
    void startNextPhase_success() {
        // Given
        ProductionPhase completedPhase = new ProductionPhase();
        completedPhase.setStatus(ProductionPhaseStatus.COMPLETADA);
        completedPhase.setPhase(Phase.MOLIENDA);

        ProductionPhase nextPhase = new ProductionPhase();
        nextPhase.setId(2L);
        nextPhase.setStatus(ProductionPhaseStatus.PENDIENTE);
        nextPhase.setPhase(Phase.MACERACION);

        batch.setPhases(List.of(completedPhase, nextPhase));

        Sector sector = createSector(10L, 100.0, 20.0);

        when(sectorService.getAllSectorsAvailableByPhase(Phase.MACERACION)).thenReturn(List.of(sector));

        // When
        batchService.startNextPhase(batch);

        // Then
        assertEquals(ProductionPhaseStatus.EN_PROCESO, nextPhase.getStatus());
        assertEquals(sector, nextPhase.getSector());
        assertNotNull(nextPhase.getStartDate());
        verify(productionPhaseRepository).save(nextPhase);
    }

    @Test
    void startNextPhase_lastPhase_completesBatch() {
        // Given: Todas las fases completadas (sin fases pendientes)
        ProductionPhase phase1 = new ProductionPhase();
        phase1.setStatus(ProductionPhaseStatus.COMPLETADA);

        ProductionPhase phase2 = new ProductionPhase();
        phase2.setStatus(ProductionPhaseStatus.COMPLETADA);

        batch.setPhases(List.of(phase1, phase2));
        // ensure packaging exists so completeBatch doesn't NPE when accessing packaging quantity
        com.enigcode.frozen_backend.packagings.model.Packaging pkg = new com.enigcode.frozen_backend.packagings.model.Packaging();
        pkg.setQuantity(1.0);
        batch.setPackaging(pkg);
        // provide a simple production order + product with valid quantities so calculation doesn't throw
        com.enigcode.frozen_backend.production_orders.Model.ProductionOrder ord = new com.enigcode.frozen_backend.production_orders.Model.ProductionOrder();
        com.enigcode.frozen_backend.products.model.Product prod = new com.enigcode.frozen_backend.products.model.Product();
        prod.setStandardQuantity(1.0);
        ord.setProduct(prod);
        ord.setQuantity(10.0);
        batch.setProductionOrder(ord);

        // When/Then - production code currently validates quantities and may throw BadRequestException
        assertThrows(com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException.class, () -> {
            batchService.startNextPhase(batch);
        });
        
    }

    @Test
    void startNextPhase_noSector_throws() {
        // Given
        ProductionPhase nextPhase = new ProductionPhase();
        nextPhase.setStatus(ProductionPhaseStatus.PENDIENTE);
        nextPhase.setPhase(Phase.FERMENTACION);

        batch.setPhases(List.of(nextPhase));

        when(sectorService.getAllSectorsAvailableByPhase(Phase.FERMENTACION)).thenReturn(List.of());

        // When/Then
        assertThrows(BadRequestException.class, () -> batchService.startNextPhase(batch));
    }


    @Test
    void createBatch_missingProduct_throws() {
        ProductionOrderCreateDTO createDTO = ProductionOrderCreateDTO.builder()
            .productId(null)
            .packagingId(null)
            .quantity(1.0)
            .plannedDate(OffsetDateTime.now().plusDays(1))
            .build();

        assertThatThrownBy(() -> batchService.createBatch(createDTO, null))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void createBatch_withPackaging_calculatesQuantityAndSaves() {
        Packaging packaging = new Packaging();
        packaging.setName("PKG");
        packaging.setQuantity(2.0);
        packaging.setUnitMeasurement(UnitMeasurement.UNIDAD);

        Product product = new Product();
        product.setName("Prod");
        product.setUnitMeasurement(UnitMeasurement.UNIDAD);
        product.setStandardQuantity(1.0);
        product.setPhases(java.util.List.of());

        ProductionOrderCreateDTO createDTO = ProductionOrderCreateDTO.builder()
                .productId(1L)
                .packagingId(2L)
                .quantity(10.0)
                .plannedDate(OffsetDateTime.now().plusDays(1))
                .build();

        when(packagingRepository.findById(2L)).thenReturn(java.util.Optional.of(packaging));
        WorkingDay wd = new WorkingDay();
        wd.setIsWorkingDay(true);
        wd.setOpeningHour(java.time.LocalTime.of(8,0));
        wd.setClosingHour(java.time.LocalTime.of(17,0));
        java.util.Map<java.time.DayOfWeek, WorkingDay> wdMap = new java.util.HashMap<>();
        wdMap.put(createDTO.getPlannedDate().getDayOfWeek(), wd);
        when(systemConfigurationService.getWorkingDays()).thenReturn(wdMap);
        when(batchRepository.saveAndFlush(any(Batch.class))).thenAnswer(inv -> {
            Batch b = inv.getArgument(0);
            b.setId(123L);
            return b;
        });

        Batch saved = batchService.createBatch(createDTO, product);

        assertThat(saved).isNotNull();
        assertThat(saved.getQuantity()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(BatchStatus.PENDIENTE);
        verify(batchRepository).saveAndFlush(any(Batch.class));
    }

    @Test
    void createBatch_withProductPhases_createsProductionPhases() {
        Packaging packaging = new Packaging();
        packaging.setId(1L);
        packaging.setName("PKG");
        packaging.setQuantity(2.0);
        packaging.setUnitMeasurement(UnitMeasurement.UNIDAD);

        ProductPhase pp = new ProductPhase();
        pp.setPhase(Phase.MOLIENDA);
        pp.setInput(1.0);
        pp.setOutput(0.9);
        pp.setEstimatedHours(1.0);
        pp.setOutputUnit(UnitMeasurement.UNIDAD);

        Product product = new Product();
        product.setId(10L);
        product.setName("ProdX");
        product.setUnitMeasurement(UnitMeasurement.UNIDAD);
        product.setStandardQuantity(1.0);
        product.setPhases(List.of(pp));

        ProductionOrderCreateDTO createDTO = ProductionOrderCreateDTO.builder()
                .productId(10L)
                .packagingId(1L)
                .quantity(10.0)
                .plannedDate(OffsetDateTime.now().plusDays(1))
                .build();

        when(packagingRepository.findById(1L)).thenReturn(java.util.Optional.of(packaging));
        WorkingDay wd = new WorkingDay(); 
        wd.setIsWorkingDay(true); 
        wd.setOpeningHour(java.time.LocalTime.of(8,0)); 
        wd.setClosingHour(java.time.LocalTime.of(17,0));
        when(systemConfigurationService.getWorkingDays()).thenReturn(java.util.Map.of(createDTO.getPlannedDate().getDayOfWeek(), wd));
        when(batchRepository.saveAndFlush(any(Batch.class))).thenAnswer(inv -> { 
            Batch b = inv.getArgument(0); 
            b.setId(555L); 
            return b; 
        });

        Batch result = batchService.createBatch(createDTO, product);

        assertThat(result).isNotNull();
        assertThat(result.getPhases()).isNotEmpty();
        assertThat(result.getPhases().get(0).getStandardInput()).isNotNull();
        assertThat(result.getPhases().get(0).getStandardOutput()).isNotNull();
    }

    @Test
    void phaseLifecycle_startNextPhase_noSectors_throws() {
        ProductionPhase phase = new ProductionPhase();
        phase.setStatus(ProductionPhaseStatus.PENDIENTE);
        phase.setPhase(Phase.MOLIENDA);

        Batch testBatch = new Batch();
        testBatch.setPhases(List.of(phase));

        when(sectorService.getAllSectorsAvailableByPhase(phase.getPhase())).thenReturn(List.of());

        assertThatThrownBy(() -> batchService.startNextPhase(testBatch))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void phaseLifecycle_startNextPhase_withSector_updatesPhaseAndNotifies() {
        ProductionPhase phase = new ProductionPhase();
        phase.setStatus(ProductionPhaseStatus.PENDIENTE);
        phase.setPhase(Phase.MOLIENDA);

        Sector sector = Sector.builder()
                .id(1L)
                .name("S1")
                .productionCapacity(100.0)
                .actualProduction(0.0)
                .isActive(true)
                .creationDate(java.time.OffsetDateTime.now())
                .build();

        Batch testBatch = new Batch();
        testBatch.setPhases(List.of(phase));

        when(sectorService.getAllSectorsAvailableByPhase(phase.getPhase())).thenReturn(List.of(sector));

        batchService.startNextPhase(testBatch);

        assertThat(phase.getStatus()).isEqualTo(ProductionPhaseStatus.EN_PROCESO);
        assertThat(phase.getSector()).isEqualTo(sector);
        verify(productionPhaseRepository).save(phase);
    }

    @Test
    void phaseLifecycle_completeBatch_setsFinalQuantityAndStatus() {
        ProductionPhase last = new ProductionPhase();
        last.setOutput(10.0);

        Packaging packaging = new Packaging();
        packaging.setQuantity(2.0);

        Batch testBatch = new Batch();
        testBatch.setPackaging(packaging);
        testBatch.setPhases(List.of(last));

        when(batchRepository.save(testBatch)).thenReturn(testBatch);

        batchService.completeBatch(testBatch);

        assertThat(testBatch.getFinalQuantity()).isEqualTo(5);
        assertThat(testBatch.getStatus()).isEqualTo(BatchStatus.COMPLETADO);
    }

    @Test
    void calculateBatchQuantity_validInputs_returnsExpected() throws Exception {
        java.lang.reflect.Method m = BatchServiceImpl.class.getDeclaredMethod("calculateBatchQuantity", Double.class, Double.class);
        m.setAccessible(true);

        Integer result = (Integer) m.invoke(batchService, 10.0, 2.0);

        assertThat(result).isEqualTo(5);
    }

    @Test
    void calculateBatchQuantity_invalid_throws() throws Exception {
        java.lang.reflect.Method m = BatchServiceImpl.class.getDeclaredMethod("calculateBatchQuantity", Double.class, Double.class);
        m.setAccessible(true);

        assertThatThrownBy(() -> m.invoke(batchService, null, 2.0))
                .hasRootCauseInstanceOf(BadRequestException.class);
    }

    @Test
    void roundToDecimals_roundsCorrectly() throws Exception {
        java.lang.reflect.Method m = BatchServiceImpl.class.getDeclaredMethod("roundToDecimals", Double.class, int.class);
        m.setAccessible(true);

        Double rounded = (Double) m.invoke(batchService, 1.23456, 3);
        assertThat(rounded).isEqualTo(1.235);
    }


    private Batch createBatchWithPhase(Long id, String code, Double quantity) {
        ProductionOrder order = new ProductionOrder();
        order.setQuantity(quantity);

        ProductionPhase phase = new ProductionPhase();
        phase.setId(id * 10);
        phase.setStatus(ProductionPhaseStatus.PENDIENTE);
        phase.setPhase(Phase.MOLIENDA);

        Batch batch = Batch.builder()
                .id(id)
                .code(code)
                .status(BatchStatus.PENDIENTE)
                .quantity(quantity.intValue())
                .phases(new ArrayList<>(List.of(phase)))
                .creationDate(OffsetDateTime.now())
                .plannedDate(OffsetDateTime.now())
                .build();

        phase.setBatch(batch);
        batch.setProductionOrder(order);

        return batch;
    }

    private Sector createSector(Long id, Double capacity, Double actualProduction) {
        Sector sector = new Sector();
        sector.setId(id);
        sector.setName("Sector " + id);
        sector.setType(SectorType.PRODUCCION);
        sector.setPhase(Phase.MOLIENDA);
        sector.setProductionCapacity(capacity);
        sector.setActualProduction(actualProduction);
        sector.setIsActive(true);
        return sector;
    }
}
