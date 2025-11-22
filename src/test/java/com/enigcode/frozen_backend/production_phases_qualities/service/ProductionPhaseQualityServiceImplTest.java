package com.enigcode.frozen_backend.production_phases_qualities.service;

import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.repository.BatchRepository;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import com.enigcode.frozen_backend.production_phases.repository.ProductionPhaseRepository;
import com.enigcode.frozen_backend.production_phases_qualities.DTO.ProductionPhaseQualityCreateDTO;
import com.enigcode.frozen_backend.production_phases_qualities.DTO.ProductionPhaseQualityResponseDTO;
import com.enigcode.frozen_backend.production_phases_qualities.DTO.ProductionPhaseQualityUpdateDTO;
import com.enigcode.frozen_backend.production_phases_qualities.mapper.ProductionPhaseQualityMapper;
import com.enigcode.frozen_backend.production_phases_qualities.model.ProductionPhaseQuality;
import com.enigcode.frozen_backend.production_phases_qualities.repository.ProductionPhaseQualityRepository;
import com.enigcode.frozen_backend.quality_parameters.model.QualityParameter;
import com.enigcode.frozen_backend.quality_parameters.repository.QualityParameterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductionPhaseQualityServiceImplTest {

    @Mock
    private ProductionPhaseQualityMapper mapper;
    @Mock
    private ProductionPhaseQualityRepository repository;
    @Mock
    private ProductionPhaseRepository productionPhaseRepository;
    @Mock
    private QualityParameterRepository qualityParameterRepository;
    @Mock
    private BatchRepository batchRepository;
    @Mock
    private com.enigcode.frozen_backend.notifications.service.NotificationService notificationService;

    @InjectMocks
    private ProductionPhaseQualityServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createProductionPhaseQuality_success() {
        ProductionPhaseQualityCreateDTO dto = ProductionPhaseQualityCreateDTO.builder()
                .productionPhaseId(1L)
                .qualityParameterId(2L)
                .value("OK")
                .isApproved(true)
                .build();

        ProductionPhase phase = ProductionPhase.builder().id(1L).phase(Phase.MOLIENDA).build();
        // El servicio usa información del batch asociado a la phase (p.ej. código),
        // aseguramos que la phase tenga un batch para evitar NPE en el test.
        com.enigcode.frozen_backend.batches.model.Batch batch = com.enigcode.frozen_backend.batches.model.Batch.builder()
            .id(100L)
            .code("BATCH-TEST")
            .build();
        phase.setBatch(batch);
        QualityParameter qp = QualityParameter.builder().id(2L).phase(Phase.MOLIENDA).name("pH").build();

        ProductionPhaseQuality entity = ProductionPhaseQuality.builder().value("OK").isApproved(true).build();
        ProductionPhaseQuality saved = ProductionPhaseQuality.builder().id(10L).value("OK").isApproved(true)
                .productionPhase(phase).qualityParameter(qp).build();
        ProductionPhaseQualityResponseDTO response = ProductionPhaseQualityResponseDTO.builder().id(10L).build();

        when(productionPhaseRepository.findById(1L)).thenReturn(Optional.of(phase));
        when(qualityParameterRepository.findById(2L)).thenReturn(Optional.of(qp));
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponseDTO(saved)).thenReturn(response);

        ProductionPhaseQualityResponseDTO result = service.createProductionPhaseQuality(dto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(repository).save(entity);
        // Relaciones seteadas por el servicio
        assertSame(phase, entity.getProductionPhase());
        assertSame(qp, entity.getQualityParameter());
    }

    @Test
    void createProductionPhaseQuality_phaseMismatch_throwsBadRequest() {
        ProductionPhaseQualityCreateDTO dto = ProductionPhaseQualityCreateDTO.builder()
                .productionPhaseId(1L)
                .qualityParameterId(2L)
                .value("OK")
                .isApproved(true)
                .build();

        ProductionPhase phase = ProductionPhase.builder().id(1L).phase(Phase.MOLIENDA).build();
        QualityParameter qp = QualityParameter.builder().id(2L).phase(Phase.COCCION).build();

        when(productionPhaseRepository.findById(1L)).thenReturn(Optional.of(phase));
        when(qualityParameterRepository.findById(2L)).thenReturn(Optional.of(qp));

        assertThrows(BadRequestException.class, () -> service.createProductionPhaseQuality(dto));
        verify(repository, never()).save(any());
    }

    @Test
    void createProductionPhaseQuality_phaseNotFound_throws() {
        ProductionPhaseQualityCreateDTO dto = ProductionPhaseQualityCreateDTO.builder()
                .productionPhaseId(99L).qualityParameterId(2L).value("OK").isApproved(true).build();
        when(productionPhaseRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.createProductionPhaseQuality(dto));
    }

    @Test
    void createProductionPhaseQuality_qualityParamNotFound_throws() {
        ProductionPhaseQualityCreateDTO dto = ProductionPhaseQualityCreateDTO.builder()
                .productionPhaseId(1L).qualityParameterId(99L).value("OK").isApproved(true).build();
        when(productionPhaseRepository.findById(1L))
                .thenReturn(Optional.of(ProductionPhase.builder().id(1L).phase(Phase.MOLIENDA).build()));
        when(qualityParameterRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.createProductionPhaseQuality(dto));
    }

    @Test
    void updateProductionPhaseQuality_success() {
        ProductionPhaseQualityUpdateDTO dto = ProductionPhaseQualityUpdateDTO.builder()
                .value("Nuevo valor").isApproved(false).build();
        ProductionPhaseQuality existing = ProductionPhaseQuality.builder().id(5L).value("Viejo").isApproved(true)
                .build();
        ProductionPhaseQuality updated = ProductionPhaseQuality.builder().id(5L).value("Nuevo valor").isApproved(false)
                .build();
        ProductionPhaseQualityResponseDTO response = ProductionPhaseQualityResponseDTO.builder().id(5L)
                .value("Nuevo valor").isApproved(false).build();

        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(mapper.partialUpdate(dto, existing)).thenReturn(updated);
        when(repository.save(updated)).thenReturn(updated);
        when(mapper.toResponseDTO(updated)).thenReturn(response);

        ProductionPhaseQualityResponseDTO result = service.updateProductionPhaseQuality(5L, dto);
        assertEquals("Nuevo valor", result.getValue());
        assertFalse(result.getIsApproved());
    }

    @Test
    void updateProductionPhaseQuality_notFound_throws() {
        when(repository.findById(123L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.updateProductionPhaseQuality(123L, new ProductionPhaseQualityUpdateDTO()));
        verify(repository, never()).save(any());
    }

    @Test
    void getProductionPhaseQuality_success() {
        ProductionPhaseQuality entity = ProductionPhaseQuality.builder().id(7L).build();
        ProductionPhaseQualityResponseDTO response = ProductionPhaseQualityResponseDTO.builder().id(7L).build();
        when(repository.findById(7L)).thenReturn(Optional.of(entity));
        when(mapper.toResponseDTO(entity)).thenReturn(response);
        ProductionPhaseQualityResponseDTO result = service.getProductionPhaseQuality(7L);
        assertEquals(7L, result.getId());
    }

    @Test
    void getProductionPhaseQuality_notFound_throws() {
        when(repository.findById(7L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getProductionPhaseQuality(7L));
    }

    @Test
    void getByPhase_success() {
        ProductionPhase phase = ProductionPhase.builder().id(1L).phase(Phase.MOLIENDA).build();
        ProductionPhaseQuality pq = ProductionPhaseQuality.builder().id(1L).productionPhase(phase).build();
        ProductionPhaseQualityResponseDTO dto = ProductionPhaseQualityResponseDTO.builder().id(1L).build();

        when(productionPhaseRepository.findById(1L)).thenReturn(Optional.of(phase));
        when(repository.findAllByProductionPhaseId(1L)).thenReturn(List.of(pq));
        when(mapper.toResponseDTO(pq)).thenReturn(dto);

        var list = service.getProductionPhaseQualityByPhase(1L);
        assertEquals(1, list.size());
    }

    @Test
    void getByPhase_notFound_throws() {
        when(productionPhaseRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getProductionPhaseQualityByPhase(1L));
    }

    @Test
    void getByBatch_success() {
        Batch batch = Batch.builder().id(10L).build();
        ProductionPhaseQuality pq = ProductionPhaseQuality.builder().id(2L).build();
        ProductionPhaseQualityResponseDTO dto = ProductionPhaseQualityResponseDTO.builder().id(2L).build();

        when(batchRepository.findById(10L)).thenReturn(Optional.of(batch));
        when(repository.findAllByProductionPhase_Batch_Id(10L)).thenReturn(List.of(pq));
        when(mapper.toResponseDTO(pq)).thenReturn(dto);

        var list = service.getProductionPhaseQualityByBatch(10L);
        assertEquals(1, list.size());
    }

    @Test
    void getByBatch_notFound_throws() {
        when(batchRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getProductionPhaseQualityByBatch(10L));
    }

    @Test
    void approveProductionPhaseQuality_success() {
        ProductionPhaseQuality entity = ProductionPhaseQuality.builder().id(50L).isApproved(false).build();
        ProductionPhaseQuality saved = ProductionPhaseQuality.builder().id(50L).isApproved(true).build();
        ProductionPhaseQualityResponseDTO response = ProductionPhaseQualityResponseDTO.builder().id(50L).isApproved(true).build();

        when(repository.findById(50L)).thenReturn(Optional.of(entity));
        when(repository.save(any(ProductionPhaseQuality.class))).thenReturn(saved);
        when(mapper.toResponseDTO(saved)).thenReturn(response);

        ProductionPhaseQualityResponseDTO result = service.approveProductionPhaseQuality(50L);
        assertNotNull(result);
        assertTrue(result.getIsApproved());
        verify(repository).save(argThat(p -> Boolean.TRUE.equals(p.getIsApproved())));
    }

    @Test
    void approveProductionPhaseQuality_notFound_throws() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.approveProductionPhaseQuality(999L));
    }

    @Test
    void disapproveProductionPhaseQuality_success() {
        ProductionPhaseQuality entity = ProductionPhaseQuality.builder().id(51L).isApproved(true).build();
        ProductionPhaseQuality saved = ProductionPhaseQuality.builder().id(51L).isApproved(false).build();
        ProductionPhaseQualityResponseDTO response = ProductionPhaseQualityResponseDTO.builder().id(51L).isApproved(false).build();

        when(repository.findById(51L)).thenReturn(Optional.of(entity));
        when(repository.save(any(ProductionPhaseQuality.class))).thenReturn(saved);
        when(mapper.toResponseDTO(saved)).thenReturn(response);

        ProductionPhaseQualityResponseDTO result = service.disapproveProductionPhaseQuality(51L);
        assertNotNull(result);
        assertFalse(result.getIsApproved());
        verify(repository).save(argThat(p -> Boolean.FALSE.equals(p.getIsApproved())));
    }

    @Test
    void getCurrentVersionForPhase_noVersions_returnsOne() {
        when(repository.findMaxVersionByProductionPhaseId(1L)).thenReturn(null);
        Integer v = service.getCurrentVersionForPhase(1L);
        assertEquals(1, v.intValue());
    }

    @Test
    void getCurrentVersionForPhase_activeQualitiesReturnsSameVersion() {
        when(repository.findMaxVersionByProductionPhaseId(2L)).thenReturn(3);
        when(repository.findAllByProductionPhaseIdAndIsActiveTrue(2L)).thenReturn(List.of(ProductionPhaseQuality.builder().id(1L).build()));
        Integer v = service.getCurrentVersionForPhase(2L);
        assertEquals(3, v.intValue());
    }

    @Test
    void getCurrentVersionForPhase_noActiveQualities_incrementsVersion() {
        when(repository.findMaxVersionByProductionPhaseId(3L)).thenReturn(4);
        when(repository.findAllByProductionPhaseIdAndIsActiveTrue(3L)).thenReturn(List.of());
        Integer v = service.getCurrentVersionForPhase(3L);
        assertEquals(5, v.intValue());
    }

    @Test
    void createNewVersionForPhase_marksHistoricalAndSaves() {
        ProductionPhase phase = ProductionPhase.builder().id(7L).phase(com.enigcode.frozen_backend.product_phases.model.Phase.MOLIENDA).build();
        when(productionPhaseRepository.findById(7L)).thenReturn(Optional.of(phase));

        ProductionPhaseQuality q1 = ProductionPhaseQuality.builder().id(100L).isActive(true).build();
        ProductionPhaseQuality q2 = ProductionPhaseQuality.builder().id(101L).isActive(true).build();
        when(repository.findAllByProductionPhaseIdAndIsActiveTrue(7L)).thenReturn(List.of(q1, q2));

        service.createNewVersionForPhase(7L);

        // both should have been marked historical and saved
        verify(repository, times(2)).save(argThat(p -> Boolean.FALSE.equals(p.getIsActive())));
    }

    @Test
    void getActiveProductionPhaseQualityByPhase_success() {
        when(productionPhaseRepository.findById(8L)).thenReturn(Optional.of(ProductionPhase.builder().id(8L).build()));
        ProductionPhaseQuality pq = ProductionPhaseQuality.builder().id(201L).build();
        when(repository.findAllByProductionPhaseIdAndIsActiveTrue(8L)).thenReturn(List.of(pq));
        when(mapper.toResponseDTO(pq)).thenReturn(ProductionPhaseQualityResponseDTO.builder().id(201L).build());

        var list = service.getActiveProductionPhaseQualityByPhase(8L);
        assertEquals(1, list.size());
    }

    @Test
    void getActiveProductionPhaseQualityByBatch_success() {
        when(batchRepository.findById(20L)).thenReturn(Optional.of(Batch.builder().id(20L).build()));
        ProductionPhaseQuality pq = ProductionPhaseQuality.builder().id(301L).build();
        when(repository.findAllByProductionPhase_Batch_IdAndIsActiveTrue(20L)).thenReturn(List.of(pq));
        when(mapper.toResponseDTO(pq)).thenReturn(ProductionPhaseQualityResponseDTO.builder().id(301L).build());

        var list = service.getActiveProductionPhaseQualityByBatch(20L);
        assertEquals(1, list.size());
    }
}
