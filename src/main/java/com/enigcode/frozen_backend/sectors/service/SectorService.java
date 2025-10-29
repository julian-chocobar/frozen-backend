package com.enigcode.frozen_backend.sectors.service;

import com.enigcode.frozen_backend.sectors.DTO.SectorCreateDTO;
import com.enigcode.frozen_backend.sectors.DTO.SectorResponseDTO;
import com.enigcode.frozen_backend.sectors.DTO.SectorUpdateDTO;
import jakarta.validation.Valid;

public interface SectorService {
    SectorResponseDTO createSector(SectorCreateDTO sectorCreateDTO);

    SectorResponseDTO getSector(Long id);

    SectorResponseDTO updateDTO(SectorUpdateDTO sectorUpdateDTO, Long id);
}
