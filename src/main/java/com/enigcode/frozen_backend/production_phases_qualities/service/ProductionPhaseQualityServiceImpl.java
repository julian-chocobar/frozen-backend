package com.enigcode.frozen_backend.production_phases_qualities.service;

import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.repository.BatchRepository;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionPhaseQualityServiceImpl implements ProductionPhaseQualityService {
        private final ProductionPhaseQualityMapper productionPhaseQualityMapper;
        private final ProductionPhaseQualityRepository productionPhaseQualityRepository;
        private final ProductionPhaseRepository productionPhaseRepository;
        private final QualityParameterRepository qualityParameterRepository;
        private final BatchRepository batchRepository;

        @Override
        @Transactional
        public ProductionPhaseQualityResponseDTO createProductionPhaseQuality(ProductionPhaseQualityCreateDTO dto) {
                ProductionPhase productionPhase = productionPhaseRepository.findById(dto.getProductionPhaseId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No se encontró production phase con id "
                                                                + dto.getProductionPhaseId()));

                QualityParameter qualityParameter = qualityParameterRepository.findById(dto.getQualityParameterId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No se encontró quality parameter con id "
                                                                + dto.getQualityParameterId()));

                if (!productionPhase.getPhase().equals(qualityParameter.getPhase()))
                        throw new BadRequestException("La fase de la production phase: " + productionPhase.getPhase() +
                                        "no coincide con la fase del parámetro: " + qualityParameter.getPhase());

                ProductionPhaseQuality productionPhaseQuality = productionPhaseQualityMapper.toEntity(dto);
                productionPhaseQuality.setProductionPhase(productionPhase);
                productionPhaseQuality.setQualityParameter(qualityParameter);

                // Si no se especifica versión, obtener la versión actual para la fase
                if (dto.getVersion() == null) {
                        Integer currentVersion = getCurrentVersionForPhase(dto.getProductionPhaseId());
                        productionPhaseQuality.setVersion(currentVersion);
                }

                ProductionPhaseQuality savedPhaseQuality = productionPhaseQualityRepository
                                .save(productionPhaseQuality);
                return productionPhaseQualityMapper.toResponseDTO(savedPhaseQuality);
        }

        @Override
        @Transactional
        public ProductionPhaseQualityResponseDTO updateProductionPhaseQuality(Long id,
                        ProductionPhaseQualityUpdateDTO dto) {
                ProductionPhaseQuality productionPhaseQuality = productionPhaseQualityRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No se encontró fase de calidad con id " + id));

                ProductionPhaseQuality updatedPhaseQuality = productionPhaseQualityMapper.partialUpdate(dto,
                                productionPhaseQuality);

                ProductionPhaseQuality savedPhaseQuality = productionPhaseQualityRepository.save(updatedPhaseQuality);

                return productionPhaseQualityMapper.toResponseDTO(savedPhaseQuality);
        }

        @Override
        @Transactional
        public List<ProductionPhaseQualityResponseDTO> getProductionPhaseQualityByPhase(Long id) {
                ProductionPhase productionPhase = productionPhaseRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No se encontró production phase con id " + id));

                List<ProductionPhaseQuality> productionPhaseQualities = productionPhaseQualityRepository
                                .findAllByProductionPhaseId(id);

                return productionPhaseQualities.stream().map(productionPhaseQualityMapper::toResponseDTO).toList();
        }

        @Override
        @Transactional
        public List<ProductionPhaseQualityResponseDTO> getProductionPhaseQualityByBatch(Long id) {
                Batch batch = batchRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("No se encontró batch con id " + id));

                List<ProductionPhaseQuality> productionPhaseQualities = productionPhaseQualityRepository
                                .findAllByProductionPhase_Batch_Id(id);

                return productionPhaseQualities.stream().map(productionPhaseQualityMapper::toResponseDTO).toList();
        }

        @Override
        @Transactional
        public ProductionPhaseQualityResponseDTO getProductionPhaseQuality(Long id) {
                ProductionPhaseQuality productionPhaseQuality = productionPhaseQualityRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No se encontró fase de calidad con id " + id));

                return productionPhaseQualityMapper.toResponseDTO(productionPhaseQuality);
        }

        @Override
        @Transactional
        public List<ProductionPhaseQualityResponseDTO> getActiveProductionPhaseQualityByPhase(Long phaseId) {
                productionPhaseRepository.findById(phaseId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No se encontró production phase con id " + phaseId));

                List<ProductionPhaseQuality> activeQualities = productionPhaseQualityRepository
                                .findAllByProductionPhaseIdAndIsActiveTrue(phaseId);

                return activeQualities.stream()
                                .map(productionPhaseQualityMapper::toResponseDTO)
                                .toList();
        }

        @Override
        @Transactional
        public List<ProductionPhaseQualityResponseDTO> getActiveProductionPhaseQualityByBatch(Long batchId) {
                batchRepository.findById(batchId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No se encontró batch con id " + batchId));

                List<ProductionPhaseQuality> activeQualities = productionPhaseQualityRepository
                                .findAllByProductionPhase_Batch_IdAndIsActiveTrue(batchId);

                return activeQualities.stream()
                                .map(productionPhaseQualityMapper::toResponseDTO)
                                .toList();
        }

        @Override
        @Transactional
        public void createNewVersionForPhase(Long phaseId) {
                // Verificar que la fase existe
                productionPhaseRepository.findById(phaseId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No se encontró production phase con id " + phaseId));

                // Marcar todos los parámetros actuales como históricos (no activos)
                List<ProductionPhaseQuality> currentActiveQualities = productionPhaseQualityRepository
                                .findAllByProductionPhaseIdAndIsActiveTrue(phaseId);

                currentActiveQualities.forEach(quality -> {
                        quality.markAsHistorical();
                        productionPhaseQualityRepository.save(quality);
                });

                // El siguiente parámetro que se cree tendrá version = currentVersion + 1
                // esto se maneja automáticamente en createProductionPhaseQuality
        }

        @Override
        @Transactional
        public Integer getCurrentVersionForPhase(Long phaseId) {
                Integer maxVersion = productionPhaseQualityRepository.findMaxVersionByProductionPhaseId(phaseId);

                // Si no hay parámetros, la primera versión es 1
                if (maxVersion == null) {
                        return 1;
                }

                // Si hay parámetros activos, mantener la versión actual
                // Si no hay parámetros activos, crear nueva versión
                List<ProductionPhaseQuality> activeQualities = productionPhaseQualityRepository
                                .findAllByProductionPhaseIdAndIsActiveTrue(phaseId);

                if (activeQualities.isEmpty()) {
                        return maxVersion + 1; // Nueva versión
                } else {
                        return maxVersion; // Versión actual
                }
        }

        @Override
        @Transactional
        public ProductionPhaseQualityResponseDTO approveProductionPhaseQuality(Long id) {
                ProductionPhaseQuality productionPhaseQuality = productionPhaseQualityRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No se encontró parámetro de calidad con id " + id));

                productionPhaseQuality.setIsApproved(true);
                ProductionPhaseQuality saved = productionPhaseQualityRepository.save(productionPhaseQuality);

                return productionPhaseQualityMapper.toResponseDTO(saved);
        }

        @Override
        @Transactional
        public ProductionPhaseQualityResponseDTO disapproveProductionPhaseQuality(Long id) {
                ProductionPhaseQuality productionPhaseQuality = productionPhaseQualityRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No se encontró parámetro de calidad con id " + id));

                productionPhaseQuality.setIsApproved(false);
                ProductionPhaseQuality saved = productionPhaseQualityRepository.save(productionPhaseQuality);

                return productionPhaseQualityMapper.toResponseDTO(saved);
        }

}
