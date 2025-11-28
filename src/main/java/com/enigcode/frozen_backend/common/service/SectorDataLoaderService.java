package com.enigcode.frozen_backend.common.service;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.sectors.DTO.SectorCreateDTO;
import com.enigcode.frozen_backend.sectors.model.SectorType;
import com.enigcode.frozen_backend.sectors.repository.SectorRepository;
import com.enigcode.frozen_backend.sectors.service.SectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SectorDataLoaderService {

    private final SectorRepository sectorRepository;
    private final SectorService sectorService;
    private final UserDataLoaderService userDataLoaderService;

    public void loadSectors() {
        if (sectorRepository.count() == 0) {
            log.info("Cargando sectores de ejemplo...");

            SectorCreateDTO almacen = SectorCreateDTO.builder()
                    .name("Almacén Principal")
                    .supervisorId(userDataLoaderService.getSupervisorAlmacenId())
                    .type(SectorType.ALMACEN)
                    .build();
            sectorService.createSector(almacen);

            double highCapacity = 5000.0;
            for (Phase phase : Phase.values()) {
                if (Phase.MADURACION.equals(phase) || Phase.FERMENTACION.equals(phase)) {
                    createProductionSector(
                            "Sector " + capitalize(phase.name().toLowerCase()),
                            userDataLoaderService.getSuperUserId(),
                            phase,
                            6000.0);
                } else {
                    createProductionSector(
                            "Sector " + capitalize(phase.name().toLowerCase()),
                            userDataLoaderService.getSuperUserId(),
                            phase,
                            highCapacity);
                }
            }

            SectorCreateDTO calidad = SectorCreateDTO.builder()
                    .name("Control de Calidad Central")
                    .supervisorId(userDataLoaderService.getSupervisorCalidadId())
                    .type(SectorType.CALIDAD)
                    .build();
            sectorService.createSector(calidad);

            log.info("Sectores cargados.");
        }
    }

    private void createProductionSector(String name, Long supervisorId, Phase phase, Double capacity) {
        SectorCreateDTO dto = SectorCreateDTO.builder()
                .name(name)
                .supervisorId(supervisorId)
                .type(SectorType.PRODUCCION)
                .phase(phase)
                .productionCapacity(capacity)
                .isTimeActive(phase.getIsTimeActive())
                .build();
        sectorService.createSector(dto);
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

}

