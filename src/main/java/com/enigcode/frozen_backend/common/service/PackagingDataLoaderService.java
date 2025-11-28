package com.enigcode.frozen_backend.common.service;

import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.packagings.DTO.PackagingCreateDTO;
import com.enigcode.frozen_backend.packagings.repository.PackagingRepository;
import com.enigcode.frozen_backend.packagings.service.PackagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackagingDataLoaderService {

    private final PackagingRepository packagingRepository;
    private final PackagingService packagingService;
    private final MaterialDataLoaderService materialDataLoaderService;

    public void loadPackagings() {
        if (packagingRepository.count() == 0) {
            log.info("Cargando empaques de ejemplo...");

            PackagingCreateDTO empaque330ml = PackagingCreateDTO.builder()
                    .name("Botella 330ml (equiv. en LT)")
                    .packagingMaterialId(materialDataLoaderService.getBotella330Id())
                    .labelingMaterialId(materialDataLoaderService.getEtiquetaBotellaId())
                    .unitMeasurement(UnitMeasurement.LT)
                    .quantity(0.33)
                    .build();
            packagingService.createPackaging(empaque330ml);

            PackagingCreateDTO empaque20L = PackagingCreateDTO.builder()
                    .name("Barril 20L (equiv. en LT)")
                    .packagingMaterialId(materialDataLoaderService.getBarril20LId())
                    .labelingMaterialId(materialDataLoaderService.getEtiquetaBarrilId())
                    .unitMeasurement(UnitMeasurement.LT)
                    .quantity(20.0)
                    .build();
            packagingService.createPackaging(empaque20L);

            log.info("Empaques cargados.");
        }
    }
}

