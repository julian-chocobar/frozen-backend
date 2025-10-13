package com.enigcode.frozen_backend.packagings.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.enigcode.frozen_backend.packagings.DTO.PackagingCreateDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingResponseDTO;
import com.enigcode.frozen_backend.packagings.DTO.PackagingSimpleResponseDTO;
import com.enigcode.frozen_backend.packagings.service.PackagingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackagingServiceImpl implements PackagingService {

    @Override
    @Transactional
    public PackagingResponseDTO savePackaging(PackagingCreateDTO packagingCreateDTO) {
        // TODO: Implementar lógica de guardado
        throw new UnsupportedOperationException("Método no implementado aún");
    }

    @Override
    @Transactional
    public PackagingResponseDTO toggleActive(Long id) {
        // TODO: Implementar lógica de cambio de estado
        throw new UnsupportedOperationException("Método no implementado aún");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PackagingResponseDTO> findAll(Pageable pageable) {
        // TODO: Implementar lógica de búsqueda paginada
        throw new UnsupportedOperationException("Método no implementado aún");
    }

    @Override
    @Transactional(readOnly = true)
    public PackagingResponseDTO getPackaging(Long id) {
        // TODO: Implementar lógica de búsqueda por ID
        throw new UnsupportedOperationException("Método no implementado aún");
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackagingSimpleResponseDTO> getActivePackagingList() {
        // TODO: Implementar lógica de búsqueda de empaques activos
        throw new UnsupportedOperationException("Método no implementado aún");
    }
}
