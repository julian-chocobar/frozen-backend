package com.enigcode.frozen_backend.packagings.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.enigcode.frozen_backend.packagings.DTO.PackagingCreateDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingResponseDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingSimpleResponseDTO;

public interface PackagingService {
    PackagingResponseDTO createPackaging(PackagingCreateDTO packagingCreateDTO);
    PackagingResponseDTO toggleActive(Long id);
    Page<PackagingResponseDTO> findAll(Pageable pageable);
    PackagingResponseDTO getPackaging(Long id);
    List<PackagingSimpleResponseDTO> getActivePackagingList();
}
