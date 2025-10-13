package com.enigcode.frozen_backend.packagings.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.packagings.DTO.PackagingCreateDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingResponseDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingSimpleResponseDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingUpdateDTO;
import com.enigcode.frozen_backend.packagings.mapper.PackagingMapper;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.packagings.repository.PackagingRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PackagingServiceImpl implements PackagingService{

    final PackagingRepository packagingRepository;
    final PackagingMapper packagingMapper;

    /**
     * Crea un nuevo packaging en la base de datos segun DTO
     * @param packagingCreateDTO
     * @return PackagingResponseDTO
     */
    @Override
    public PackagingResponseDTO createPackaging(@Valid PackagingCreateDTO packagingCreateDTO) {
        Packaging packaging = packagingMapper.toEntity(packagingCreateDTO);
        packaging.setCreationDate(OffsetDateTime.now());
        packaging.setIsActive(Boolean.TRUE);

        Packaging savedPackaging = packagingRepository.saveAndFlush(packaging);

        return packagingMapper.toResponseDto(savedPackaging);
    }

    @Override
    public PackagingResponseDTO toggleActive(Long id) {
        return null;
    }

    @Override
    public Page<PackagingResponseDTO> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public PackagingResponseDTO getPackaging(Long id) {
        return null;
    }

    @Override
    public List<PackagingSimpleResponseDTO> getActivePackagingList() {
        return List.of();
    }

    @Override
    public PackagingResponseDTO updatePackaging(Long id,@Valid PackagingUpdateDTO packagingUpdateDTO) {
        Packaging originalPackaging = packagingRepository.findById(id)
                        .orElseThrow(()-> new ResourceNotFoundException("No se encontro packaging con id "+ id));

        Packaging updatedPackaging = packagingMapper.partialUpdate(packagingUpdateDTO, originalPackaging);
       
        Packaging savedPackaging = packagingRepository.save(updatedPackaging);


        return packagingMapper.toResponseDto(savedPackaging);
    }
}
