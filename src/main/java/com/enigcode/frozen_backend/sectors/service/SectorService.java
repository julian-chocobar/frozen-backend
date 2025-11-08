package com.enigcode.frozen_backend.sectors.service;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.sectors.DTO.SectorCreateDTO;
import com.enigcode.frozen_backend.sectors.DTO.SectorResponseDTO;
import com.enigcode.frozen_backend.sectors.DTO.SectorUpdateDTO;
import com.enigcode.frozen_backend.sectors.model.Sector;

import java.util.List;

public interface SectorService {
    SectorResponseDTO createSector(SectorCreateDTO sectorCreateDTO);

    SectorResponseDTO getSector(Long id);

    SectorResponseDTO updateDTO(SectorUpdateDTO sectorUpdateDTO, Long id);

    List<Sector> getAllSectorsAvailableByPhase(Phase phase);

    void saveAll(List<Sector> sectors);
}
