package com.enigcode.frozen_backend.sectors.service;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.sectors.DTO.SectorCreateDTO;
import com.enigcode.frozen_backend.sectors.DTO.SectorResponseDTO;
import com.enigcode.frozen_backend.sectors.DTO.SectorUpdateDTO;
import com.enigcode.frozen_backend.sectors.model.Sector;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SectorService {
    SectorResponseDTO createSector(SectorCreateDTO sectorCreateDTO);

    SectorResponseDTO getSector(Long id);

    SectorResponseDTO updateDTO(SectorUpdateDTO sectorUpdateDTO, Long id);

    Page<SectorResponseDTO> findAll(Pageable pageable);

    List<Sector> getAllSectorsAvailableByPhase(Phase phase);

    void saveAll(List<Sector> sectors);
}
