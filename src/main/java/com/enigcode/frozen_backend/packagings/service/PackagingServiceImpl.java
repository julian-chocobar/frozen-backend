package com.enigcode.frozen_backend.packagings.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.packagings.DTO.PackagingCreateDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingResponseDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingSimpleResponseDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingUpdateDTO;
import com.enigcode.frozen_backend.packagings.mapper.PackagingMapper;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.packagings.repository.PackagingRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    @Transactional
    public PackagingResponseDTO createPackaging(@Valid PackagingCreateDTO packagingCreateDTO) {
        Packaging packaging = packagingMapper.toEntity(packagingCreateDTO);
        packaging.setCreationDate(OffsetDateTime.now());
        packaging.setIsActive(Boolean.TRUE);

        Packaging savedPackaging = packagingRepository.saveAndFlush(packaging);

        return packagingMapper.toResponseDto(savedPackaging);
    }

    /**
     * Alterna el estado del paquete 
     * @param id
     * @return PackagingResponseDTO
     */
    @Override
    @Transactional
    public PackagingResponseDTO toggleActive(Long id) {
        Packaging packaging = packagingRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Packaging no encontrado con ID: " + id));
        
        packaging.toggleActive();

        Packaging savedPackaging = packagingRepository.save(packaging);

        return packagingMapper.toResponseDto(savedPackaging);
    }

    @Override
    @Transactional
    public Page<PackagingResponseDTO> findAll(Pageable pageable) {
        Pageable pageRequest = PageRequest.of(
            pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());
        Page<Packaging> packagings = packagingRepository.findAll(pageRequest);

        return packagings.map(packagingMapper::toResponseDto);
    }

    /**
     * Funcion para mostrar un paquete especifico segun id
     * 
     * @param id
     * @retutn Vista detallada de los elementos del paquete
     */
    @Override
    @Transactional
    public PackagingResponseDTO getPackaging(Long id) {
        Packaging packaging = packagingRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Packaging no encontrado con ID: " + id));
                
        return packagingMapper.toResponseDto(packaging);
    }

     /**
     * Funcion para mostrar a todos los paquetes activos
     *
     * @retutn Vista detallada de los paquetes activos
     */
    @Override
    @Transactional
    public List<PackagingSimpleResponseDTO> getActivePackagingList() {

        List<PackagingSimpleResponseDTO> activePackagings = packagingRepository.findAll().stream()
            .filter(packaging -> Boolean.TRUE.equals(packaging.getIsActive())).map(packagingMapper :: toSimpleResponseDTO).toList();
               
        return activePackagings;
    }

    /**
     * Funcion que cambia ciertos parametros de un paquete preexistente
     * @param id
     * @param packagingUpdateDTO
     * @return PackagingResponseDTO
     */
    @Override
    @Transactional
    public PackagingResponseDTO updatePackaging(Long id,@Valid PackagingUpdateDTO packagingUpdateDTO) {
        Packaging originalPackaging = packagingRepository.findById(id)
                        .orElseThrow(()-> new ResourceNotFoundException("No se encontro packaging con id "+ id));

        Packaging updatedPackaging = packagingMapper.partialUpdate(packagingUpdateDTO, originalPackaging);
       
        Packaging savedPackaging = packagingRepository.save(updatedPackaging);


        return packagingMapper.toResponseDto(savedPackaging);
    }
}
