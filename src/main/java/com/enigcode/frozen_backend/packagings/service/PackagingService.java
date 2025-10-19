package com.enigcode.frozen_backend.packagings.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.enigcode.frozen_backend.packagings.DTO.PackagingCreateDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingResponseDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingSimpleResponseDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingUpdateDTO;

public interface PackagingService {
    PackagingResponseDTO createPackaging(PackagingCreateDTO packagingCreateDTO);
    PackagingResponseDTO toggleActive(Long id);
    Page<PackagingResponseDTO> findAll(Pageable pageable);
    PackagingResponseDTO getPackaging(Long id);
    List<PackagingSimpleResponseDTO> getPackagingList(String name, Boolean active, Long productId);
    PackagingResponseDTO updatePackaging(Long id, PackagingUpdateDTO packagingUpdateDTO);
}
