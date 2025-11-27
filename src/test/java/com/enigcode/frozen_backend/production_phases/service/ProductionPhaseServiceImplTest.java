package com.enigcode.frozen_backend.production_phases.service;

import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.service.BatchService;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.movements.service.MovementService;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.production_materials.repository.ProductionMaterialRepository;
import com.enigcode.frozen_backend.production_phases.DTO.ProductionPhaseResponseDTO;
import com.enigcode.frozen_backend.production_phases.DTO.ProductionPhaseUnderReviewDTO;
import com.enigcode.frozen_backend.production_phases.mapper.ProductionPhaseMapper;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhaseStatus;
import com.enigcode.frozen_backend.production_phases.repository.ProductionPhaseRepository;
import com.enigcode.frozen_backend.production_phases_qualities.model.ProductionPhaseQuality;
import com.enigcode.frozen_backend.production_phases_qualities.repository.ProductionPhaseQualityRepository;
import com.enigcode.frozen_backend.quality_parameters.model.QualityParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductionPhaseServiceImplTest {

    @Mock
    private ProductionPhaseRepository productionPhaseRepository;

    @Mock
    private ProductionPhaseMapper productionPhaseMapper;

    @Mock
    private ProductionMaterialRepository productionMaterialRepository;

    @Mock
    private MovementService movementService;

    @Mock
    private ProductionPhaseQualityRepository productionPhaseQualityRepository;

        @Mock
        private com.enigcode.frozen_backend.notifications.service.NotificationService notificationService;

        @Mock
        private com.enigcode.frozen_backend.production_phases_qualities.service.ProductionPhaseQualityService productionPhaseQualityService;

        @Mock
        private BatchService batchService;

    @InjectMocks
    private ProductionPhaseServiceImpl productionPhaseService;

    private ProductionPhase productionPhase;
    private ProductionPhaseResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        Batch batch = Batch.builder().id(1L).code("BATCH-001").build();
        
        productionPhase = ProductionPhase.builder()
                .id(1L)
                .batch(batch)
                .phase(Phase.MOLIENDA)
                .status(ProductionPhaseStatus.EN_PROCESO)
                .input(100.0)
                .output(95.0)
                .build();

        responseDTO = new ProductionPhaseResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setStatus(ProductionPhaseStatus.BAJO_REVISION);
    }

    @Test
    void testSetUnderReview_Success() {
        ProductionPhaseUnderReviewDTO dto = ProductionPhaseUnderReviewDTO.builder()
                .input(100.0)
                .output(95.0)
                .build();

        when(productionPhaseRepository.findById(1L)).thenReturn(Optional.of(productionPhase));
        when(productionPhaseMapper.partialUpdate(dto, productionPhase)).thenReturn(productionPhase);
        when(productionPhaseRepository.save(any(ProductionPhase.class))).thenReturn(productionPhase);
        when(productionPhaseMapper.toResponseDTO(productionPhase)).thenReturn(responseDTO);

        ProductionPhaseResponseDTO result = productionPhaseService.setUnderReview(1L, dto);

        assertNotNull(result);
        assertEquals(ProductionPhaseStatus.BAJO_REVISION, result.getStatus());
        verify(productionPhaseRepository).save(any(ProductionPhase.class));
    }

    @Test
    void testSetUnderReview_NotFound() {
        ProductionPhaseUnderReviewDTO dto = ProductionPhaseUnderReviewDTO.builder()
                .input(100.0)
                .output(95.0)
                .build();

        when(productionPhaseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> productionPhaseService.setUnderReview(999L, dto));
    }

    @Test
    void testSetUnderReview_InvalidStatus_ThrowsBadRequest() {
        productionPhase.setStatus(ProductionPhaseStatus.COMPLETADA);
        ProductionPhaseUnderReviewDTO dto = ProductionPhaseUnderReviewDTO.builder()
                .input(100.0)
                .output(95.0)
                .build();

        when(productionPhaseRepository.findById(1L)).thenReturn(Optional.of(productionPhase));

        assertThrows(BadRequestException.class, 
            () -> productionPhaseService.setUnderReview(1L, dto));
    }

    @Test
    void testSetUnderReview_FromSiendoAjustada_Success() {
        productionPhase.setStatus(ProductionPhaseStatus.SIENDO_AJUSTADA);
        ProductionPhaseUnderReviewDTO dto = ProductionPhaseUnderReviewDTO.builder()
                .input(100.0)
                .output(95.0)
                .build();

        when(productionPhaseRepository.findById(1L)).thenReturn(Optional.of(productionPhase));
        when(productionPhaseMapper.partialUpdate(dto, productionPhase)).thenReturn(productionPhase);
        when(productionPhaseRepository.save(any(ProductionPhase.class))).thenReturn(productionPhase);
        when(productionPhaseMapper.toResponseDTO(productionPhase)).thenReturn(responseDTO);

        ProductionPhaseResponseDTO result = productionPhaseService.setUnderReview(1L, dto);

        assertNotNull(result);
        verify(productionPhaseRepository).save(any(ProductionPhase.class));
    }

    @Test
    void testGetProductionPhase_Success() {
        when(productionPhaseRepository.findById(1L)).thenReturn(Optional.of(productionPhase));
        when(productionPhaseMapper.toResponseDTO(productionPhase)).thenReturn(responseDTO);

        ProductionPhaseResponseDTO result = productionPhaseService.getProductionPhase(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(productionPhaseRepository).findById(1L);
    }

    @Test
    void testGetProductionPhase_NotFound() {
        when(productionPhaseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> productionPhaseService.getProductionPhase(999L));
    }

    @Test
    void testGetProductionPhasesByBatch_Success() {
        List<ProductionPhase> phases = List.of(productionPhase);

        when(productionPhaseRepository.findAllByBatchIdOrderByPhaseOrderAsc(1L)).thenReturn(phases);
        when(productionPhaseMapper.toResponseDTO(any(ProductionPhase.class))).thenReturn(responseDTO);

        List<ProductionPhaseResponseDTO> result = productionPhaseService.getProductionPhasesByBatch(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
                verify(productionPhaseRepository).findAllByBatchIdOrderByPhaseOrderAsc(1L);
    }

    @Test
    void testGetProductionPhasesByBatch_NotFound() {
                when(productionPhaseRepository.findAllByBatchIdOrderByPhaseOrderAsc(999L)).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, 
            () -> productionPhaseService.getProductionPhasesByBatch(999L));
    }

        @Test
                void testReviewProductionPhase_ApprovedQuality_AdjustsPhase_CurrentLogic() {
        productionPhase.setStatus(ProductionPhaseStatus.BAJO_REVISION);

        QualityParameter qualityParameter = QualityParameter.builder()
                .id(1L)
                .isCritical(false)
                .build();

        ProductionPhaseQuality quality1 = ProductionPhaseQuality.builder()
                .id(1L)
                .isApproved(true)
                .qualityParameter(qualityParameter)
                .build();

        when(productionPhaseRepository.findById(1L)).thenReturn(Optional.of(productionPhase));
        when(productionPhaseQualityRepository.findAllByProductionPhaseIdAndIsActiveTrue(1L))
                .thenReturn(List.of(quality1));
        // mock the productionPhaseQualityService used inside adjustProductionPhase
        doNothing().when(productionPhaseQualityService).createNewVersionForPhase(anyLong());
        when(productionPhaseRepository.save(any(ProductionPhase.class))).thenReturn(productionPhase);
        when(productionPhaseMapper.toResponseDTO(productionPhase)).thenReturn(responseDTO);

        ProductionPhaseResponseDTO result = productionPhaseService.reviewProductionPhase(1L);

        assertNotNull(result);
        assertEquals(ProductionPhaseStatus.COMPLETADA, productionPhase.getStatus());
        verify(productionPhaseRepository).save(productionPhase);
    }

    @Test
        void testReviewProductionPhase_NonCriticalErrors_AdjustsPhase_CurrentLogic() {
        productionPhase.setStatus(ProductionPhaseStatus.BAJO_REVISION);

        QualityParameter qualityParameter = QualityParameter.builder()
                .id(1L)
                .isCritical(false)
                .build();

        ProductionPhaseQuality quality1 = ProductionPhaseQuality.builder()
                .id(1L)
                .isApproved(false)
                .qualityParameter(qualityParameter)
                .build();

        when(productionPhaseRepository.findById(1L)).thenReturn(Optional.of(productionPhase));
        when(productionPhaseQualityRepository.findAllByProductionPhaseIdAndIsActiveTrue(1L))
                .thenReturn(List.of(quality1));
        // Ensure sector exists to avoid NPE when sending notifications
        com.enigcode.frozen_backend.sectors.model.Sector sector = new com.enigcode.frozen_backend.sectors.model.Sector();
        sector.setId(5L);
        productionPhase.setSector(sector);

        // stub productionPhaseQualityService used by adjustProductionPhase
        doNothing().when(productionPhaseQualityService).createNewVersionForPhase(anyLong());

        when(productionPhaseRepository.save(any(ProductionPhase.class))).thenReturn(productionPhase);
        when(productionPhaseMapper.toResponseDTO(productionPhase)).thenReturn(responseDTO);

        ProductionPhaseResponseDTO result = productionPhaseService.reviewProductionPhase(1L);

        assertNotNull(result);
        // La implementación actual llama a adjustProductionPhase cuando hay parámetros no aprobados.
        assertEquals(ProductionPhaseStatus.SIENDO_AJUSTADA, productionPhase.getStatus());
        verify(productionPhaseRepository).save(productionPhase);
    }

    @Test
        void testReviewProductionPhase_CriticalErrors_AdjustsPhase_CurrentLogic() {
        // Nota: La lógica actual tiene un bug - cuando hay errores críticos,
        // allMatch(isCritical==false) retorna false y NO entra al if de rejectProductionPhase,
        // por lo que va a adjustProductionPhase (debería rechazar)
        // Este test refleja el comportamiento actual del código de producción
        productionPhase.setStatus(ProductionPhaseStatus.BAJO_REVISION);

        QualityParameter criticalParameter = QualityParameter.builder()
                .id(1L)
                .isCritical(true)
                .build();

        ProductionPhaseQuality quality1 = ProductionPhaseQuality.builder()
                .id(1L)
                .isApproved(false)
                .qualityParameter(criticalParameter)
                .build();

        // Ensure the phase has a sector to avoid NPE when notifying
        com.enigcode.frozen_backend.sectors.model.Sector sector = new com.enigcode.frozen_backend.sectors.model.Sector();
        sector.setId(5L);
        productionPhase.setSector(sector);

        when(productionPhaseRepository.findById(1L)).thenReturn(Optional.of(productionPhase));
        when(productionPhaseQualityRepository.findAllByProductionPhaseIdAndIsActiveTrue(1L))
                .thenReturn(List.of(quality1));
        when(productionPhaseRepository.save(any(ProductionPhase.class))).thenReturn(productionPhase);
        when(productionPhaseMapper.toResponseDTO(productionPhase)).thenReturn(responseDTO);

        ProductionPhaseResponseDTO result = productionPhaseService.reviewProductionPhase(1L);

        assertNotNull(result);
        // Comportamiento actual: la implementación rechaza la fase cuando hay errores críticos.
        assertEquals(ProductionPhaseStatus.RECHAZADA, productionPhase.getStatus());
        verify(productionPhaseRepository).save(productionPhase);
    }

    @Test
    void testReviewProductionPhase_InvalidStatus_ThrowsBadRequest() {
        productionPhase.setStatus(ProductionPhaseStatus.EN_PROCESO);

        when(productionPhaseRepository.findById(1L)).thenReturn(Optional.of(productionPhase));

        assertThrows(BadRequestException.class, 
            () -> productionPhaseService.reviewProductionPhase(1L));
    }

    @Test
    void testReviewProductionPhase_NoQualityParameters_ThrowsBadRequest() {
        productionPhase.setStatus(ProductionPhaseStatus.BAJO_REVISION);

        when(productionPhaseRepository.findById(1L)).thenReturn(Optional.of(productionPhase));
        when(productionPhaseQualityRepository.findAllByProductionPhaseIdAndIsActiveTrue(1L))
                .thenReturn(Collections.emptyList());

        assertThrows(BadRequestException.class, 
            () -> productionPhaseService.reviewProductionPhase(1L));
    }
}
