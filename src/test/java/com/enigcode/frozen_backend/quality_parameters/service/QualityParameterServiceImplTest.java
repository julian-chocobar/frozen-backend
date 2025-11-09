package com.enigcode.frozen_backend.quality_parameters.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterCreateDTO;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterResponseDTO;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterUpdateDTO;
import com.enigcode.frozen_backend.quality_parameters.mapper.QualityParameterMapper;
import com.enigcode.frozen_backend.quality_parameters.model.QualityParameter;
import com.enigcode.frozen_backend.quality_parameters.repository.QualityParameterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QualityParameterServiceImplTest {

    @Mock
    private QualityParameterRepository qualityParameterRepository;

    @Mock
    private QualityParameterMapper qualityParameterMapper;

    @InjectMocks
    private QualityParameterServiceImpl qualityParameterService;

    private QualityParameter qualityParameter;
    private QualityParameterResponseDTO responseDTO;
    private QualityParameterCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        qualityParameter = QualityParameter.builder()
                .id(1L)
                .phase(Phase.MOLIENDA)
                .isCritical(true)
                .name("pH")
                .description("Medición de acidez")
                .isActive(true)
                .build();

        responseDTO = QualityParameterResponseDTO.builder()
                .phase(Phase.MOLIENDA)
                .isCritical(true)
                .name("pH")
                .description("Medición de acidez")
                .isActive(true)
                .build();

        createDTO = QualityParameterCreateDTO.builder()
                .phase(Phase.MOLIENDA)
                .isCritical(true)
                .name("pH")
                .description("Medición de acidez")
                .build();
    }

    @Test
    void testCreateQualityParameter_success() {
        when(qualityParameterMapper.toEntity(createDTO)).thenReturn(qualityParameter);
        when(qualityParameterRepository.saveAndFlush(any(QualityParameter.class))).thenReturn(qualityParameter);
        when(qualityParameterMapper.toResponseDTO(qualityParameter)).thenReturn(responseDTO);

        QualityParameterResponseDTO result = qualityParameterService.createQualityParameter(createDTO);

        assertNotNull(result);
        assertEquals("pH", result.getName());
        assertEquals(Phase.MOLIENDA, result.getPhase());
        assertTrue(result.getIsCritical());
        verify(qualityParameterRepository, times(1)).saveAndFlush(any(QualityParameter.class));
        verify(qualityParameterMapper, times(1)).toEntity(createDTO);
        verify(qualityParameterMapper, times(1)).toResponseDTO(qualityParameter);
    }

    @Test
    void testCreateQualityParameter_withMinimalData() {
        QualityParameterCreateDTO minimalDTO = QualityParameterCreateDTO.builder()
                .phase(Phase.FERMENTACION)
                .isCritical(false)
                .name("Temperatura")
                .build();

        QualityParameter minimalParam = QualityParameter.builder()
                .id(2L)
                .phase(Phase.FERMENTACION)
                .isCritical(false)
                .name("Temperatura")
                .isActive(true)
                .build();

        QualityParameterResponseDTO minimalResponse = QualityParameterResponseDTO.builder()
                .phase(Phase.FERMENTACION)
                .isCritical(false)
                .name("Temperatura")
                .isActive(true)
                .build();

        when(qualityParameterMapper.toEntity(minimalDTO)).thenReturn(minimalParam);
        when(qualityParameterRepository.saveAndFlush(minimalParam)).thenReturn(minimalParam);
        when(qualityParameterMapper.toResponseDTO(minimalParam)).thenReturn(minimalResponse);

        QualityParameterResponseDTO result = qualityParameterService.createQualityParameter(minimalDTO);

        assertNotNull(result);
        assertEquals("Temperatura", result.getName());
        assertFalse(result.getIsCritical());
    }

    @Test
    void testUpdateQualityParameter_success() {
        QualityParameterUpdateDTO updateDTO = QualityParameterUpdateDTO.builder()
                .description("Nueva descripción actualizada")
                .build();

        QualityParameter updatedParameter = QualityParameter.builder()
                .id(1L)
                .phase(Phase.MOLIENDA)
                .isCritical(true)
                .name("pH")
                .description("Nueva descripción actualizada")
                .isActive(true)
                .build();

        QualityParameterResponseDTO updatedResponse = QualityParameterResponseDTO.builder()
                .phase(Phase.MOLIENDA)
                .isCritical(true)
                .name("pH")
                .description("Nueva descripción actualizada")
                .isActive(true)
                .build();

        when(qualityParameterRepository.findById(1L)).thenReturn(Optional.of(qualityParameter));
        when(qualityParameterRepository.save(any(QualityParameter.class))).thenReturn(updatedParameter);
        when(qualityParameterMapper.toResponseDTO(updatedParameter)).thenReturn(updatedResponse);

        QualityParameterResponseDTO result = qualityParameterService.updateQualityParameter(1L, updateDTO);

        assertNotNull(result);
        assertEquals("Nueva descripción actualizada", result.getDescription());
        verify(qualityParameterRepository, times(1)).findById(1L);
        verify(qualityParameterRepository, times(1)).save(any(QualityParameter.class));
    }

    @Test
    void testUpdateQualityParameter_notFound_throwsException() {
        QualityParameterUpdateDTO updateDTO = QualityParameterUpdateDTO.builder()
                .description("Nueva descripción")
                .build();

        when(qualityParameterRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                qualityParameterService.updateQualityParameter(999L, updateDTO)
        );

        verify(qualityParameterRepository, times(1)).findById(999L);
        verify(qualityParameterRepository, never()).save(any(QualityParameter.class));
    }

    @Test
    void testGetQualityParameter_success() {
        when(qualityParameterRepository.findById(1L)).thenReturn(Optional.of(qualityParameter));
        when(qualityParameterMapper.toResponseDTO(qualityParameter)).thenReturn(responseDTO);

        QualityParameterResponseDTO result = qualityParameterService.getQualityParameter(1L);

        assertNotNull(result);
        assertEquals("pH", result.getName());
        assertEquals(Phase.MOLIENDA, result.getPhase());
        verify(qualityParameterRepository, times(1)).findById(1L);
    }

    @Test
    void testGetQualityParameter_notFound_throwsException() {
        when(qualityParameterRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                qualityParameterService.getQualityParameter(999L)
        );

        verify(qualityParameterRepository, times(1)).findById(999L);
    }

    @Test
    void testToggleActive_fromTrueToFalse() {
        when(qualityParameterRepository.findById(1L)).thenReturn(Optional.of(qualityParameter));

        QualityParameter toggledParameter = QualityParameter.builder()
                .id(1L)
                .phase(Phase.MOLIENDA)
                .isCritical(true)
                .name("pH")
                .description("Medición de acidez")
                .isActive(false)
                .build();

        QualityParameterResponseDTO toggledResponse = QualityParameterResponseDTO.builder()
                .phase(Phase.MOLIENDA)
                .isCritical(true)
                .name("pH")
                .description("Medición de acidez")
                .isActive(false)
                .build();

        when(qualityParameterRepository.save(any(QualityParameter.class))).thenReturn(toggledParameter);
        when(qualityParameterMapper.toResponseDTO(toggledParameter)).thenReturn(toggledResponse);

        QualityParameterResponseDTO result = qualityParameterService.toggleActive(1L);

        assertNotNull(result);
        assertFalse(result.getIsActive());
        verify(qualityParameterRepository, times(1)).findById(1L);
        verify(qualityParameterRepository, times(1)).save(any(QualityParameter.class));
    }

    @Test
    void testToggleActive_fromFalseToTrue() {
        QualityParameter inactiveParameter = QualityParameter.builder()
                .id(2L)
                .phase(Phase.FERMENTACION)
                .isCritical(false)
                .name("Temperatura")
                .isActive(false)
                .build();

        when(qualityParameterRepository.findById(2L)).thenReturn(Optional.of(inactiveParameter));

        QualityParameter toggledParameter = QualityParameter.builder()
                .id(2L)
                .phase(Phase.FERMENTACION)
                .isCritical(false)
                .name("Temperatura")
                .isActive(true)
                .build();

        QualityParameterResponseDTO toggledResponse = QualityParameterResponseDTO.builder()
                .phase(Phase.FERMENTACION)
                .isCritical(false)
                .name("Temperatura")
                .isActive(true)
                .build();

        when(qualityParameterRepository.save(any(QualityParameter.class))).thenReturn(toggledParameter);
        when(qualityParameterMapper.toResponseDTO(toggledParameter)).thenReturn(toggledResponse);

        QualityParameterResponseDTO result = qualityParameterService.toggleActive(2L);

        assertNotNull(result);
        assertTrue(result.getIsActive());
    }

    @Test
    void testToggleActive_notFound_throwsException() {
        when(qualityParameterRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                qualityParameterService.toggleActive(999L)
        );

        verify(qualityParameterRepository, times(1)).findById(999L);
        verify(qualityParameterRepository, never()).save(any(QualityParameter.class));
    }

    @Test
    void testGetQualityParameters_returnsAll() {
        QualityParameter param1 = QualityParameter.builder()
                .id(1L)
                .phase(Phase.MOLIENDA)
                .isCritical(true)
                .name("pH")
                .isActive(true)
                .build();

        QualityParameter param2 = QualityParameter.builder()
                .id(2L)
                .phase(Phase.FERMENTACION)
                .isCritical(false)
                .name("Temperatura")
                .isActive(true)
                .build();

        List<QualityParameter> parameters = Arrays.asList(param1, param2);

        QualityParameterResponseDTO response1 = QualityParameterResponseDTO.builder()
                .phase(Phase.MOLIENDA)
                .isCritical(true)
                .name("pH")
                .isActive(true)
                .build();

        QualityParameterResponseDTO response2 = QualityParameterResponseDTO.builder()
                .phase(Phase.FERMENTACION)
                .isCritical(false)
                .name("Temperatura")
                .isActive(true)
                .build();

        when(qualityParameterRepository.findAll()).thenReturn(parameters);
        when(qualityParameterMapper.toResponseDTO(param1)).thenReturn(response1);
        when(qualityParameterMapper.toResponseDTO(param2)).thenReturn(response2);

        List<QualityParameterResponseDTO> result = qualityParameterService.getQualityParameters();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("pH", result.get(0).getName());
        assertEquals("Temperatura", result.get(1).getName());
        verify(qualityParameterRepository, times(1)).findAll();
    }

    @Test
    void testGetQualityParameters_emptyList() {
        when(qualityParameterRepository.findAll()).thenReturn(Arrays.asList());

        List<QualityParameterResponseDTO> result = qualityParameterService.getQualityParameters();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(qualityParameterRepository, times(1)).findAll();
    }

    @Test
    void testIsActiveDefault_shouldBeTrue() {
        // Este test verifica que el modelo tenga el comportamiento esperado
        QualityParameter newParameter = new QualityParameter();
        newParameter.setPhase(Phase.MOLIENDA);
        newParameter.setName("Test");
        newParameter.setCritical(true);
        
        // Simular el @PrePersist
        newParameter.setIsActive();
        
        assertTrue(newParameter.getIsActive(), "isActive debería ser true por defecto");
    }

    @Test
    void testCriticalParameter_creation() {
        QualityParameterCreateDTO criticalDTO = QualityParameterCreateDTO.builder()
                .phase(Phase.COCCION)
                .isCritical(true)
                .name("Densidad Crítica")
                .description("Parámetro crítico de control")
                .build();

        QualityParameter criticalParam = QualityParameter.builder()
                .id(3L)
                .phase(Phase.COCCION)
                .isCritical(true)
                .name("Densidad Crítica")
                .description("Parámetro crítico de control")
                .isActive(true)
                .build();

        QualityParameterResponseDTO criticalResponse = QualityParameterResponseDTO.builder()
                .phase(Phase.COCCION)
                .isCritical(true)
                .name("Densidad Crítica")
                .description("Parámetro crítico de control")
                .isActive(true)
                .build();

        when(qualityParameterMapper.toEntity(criticalDTO)).thenReturn(criticalParam);
        when(qualityParameterRepository.saveAndFlush(criticalParam)).thenReturn(criticalParam);
        when(qualityParameterMapper.toResponseDTO(criticalParam)).thenReturn(criticalResponse);

        QualityParameterResponseDTO result = qualityParameterService.createQualityParameter(criticalDTO);

        assertNotNull(result);
        assertTrue(result.getIsCritical(), "El parámetro debe ser marcado como crítico");
    }
}
