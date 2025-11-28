package com.enigcode.frozen_backend.common.service;

import com.enigcode.frozen_backend.materials.DTO.MaterialCreateDTO;
import com.enigcode.frozen_backend.materials.DTO.MaterialResponseDTO;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.materials.model.WarehouseZone;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.enigcode.frozen_backend.materials.service.MaterialService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialDataLoaderService {

    private final MaterialRepository materialRepository;
    private final MaterialService materialService;

    @Getter
    private Long maltaPaleId;
    @Getter
    private Long maltaCrystalId;
    @Getter
    private Long maltaChocolateId;
    @Getter
    private Long lupuloCitraId;
    @Getter
    private Long lupuloSimcoeId;
    @Getter
    private Long levaduraAleId;
    @Getter
    private Long levaduraLagerId;
    @Getter
    private Long aguaId;
    @Getter
    private Long clarificanteId;
    @Getter
    private Long co2Id;
    @Getter
    private Long adsorbenteId;
    @Getter
    private Long botella330Id;
    @Getter
    private Long barril20LId;
    @Getter
    private Long etiquetaBotellaId;
    @Getter
    private Long etiquetaBarrilId;

    public void loadMaterials() {
        if (materialRepository.count() == 0) {
            log.info("Cargando materiales de ejemplo con stock suficiente para múltiples órdenes de producción...");

            // Maltas - Stock suficiente para múltiples órdenes
            MaterialCreateDTO maltaPale = MaterialCreateDTO.builder()
                    .name("Malta Pale")
                    .type(MaterialType.MALTA)
                    .supplier("Molino San Martín")
                    .value(2.50)
                    .stock(20000.0)
                    .unitMeasurement(UnitMeasurement.KG)
                    .threshold(50.0)
                    .warehouseZone(WarehouseZone.MALTA)
                    .warehouseSection("A1")
                    .warehouseLevel(2)
                    .build();
            MaterialResponseDTO maltaPaleResponse = materialService.createMaterial(maltaPale);
            maltaPaleId = maltaPaleResponse.getId();

            MaterialCreateDTO maltaCrystal = MaterialCreateDTO.builder()
                    .name("Malta Crystal")
                    .type(MaterialType.MALTA)
                    .supplier("Molino San Martín")
                    .value(3.00)
                    .stock(10000.0)
                    .unitMeasurement(UnitMeasurement.KG)
                    .threshold(10.0)
                    .warehouseZone(WarehouseZone.MALTA)
                    .warehouseSection("A3")
                    .warehouseLevel(1)
                    .build();
            MaterialResponseDTO maltaCrystalResponse = materialService.createMaterial(maltaCrystal);
            maltaCrystalId = maltaCrystalResponse.getId();

            MaterialCreateDTO maltaChocolate = MaterialCreateDTO.builder()
                    .name("Malta Chocolate")
                    .type(MaterialType.MALTA)
                    .supplier("Molino San Martín")
                    .value(4.00)
                    .stock(8000.0)
                    .unitMeasurement(UnitMeasurement.KG)
                    .threshold(5.0)
                    .warehouseZone(WarehouseZone.MALTA)
                    .warehouseSection("B2")
                    .warehouseLevel(3)
                    .build();
            MaterialResponseDTO maltaChocolateResponse = materialService.createMaterial(maltaChocolate);
            maltaChocolateId = maltaChocolateResponse.getId();

            // Lúpulos
            MaterialCreateDTO lupuloCitra = MaterialCreateDTO.builder()
                    .name("Lúpulo Citra")
                    .type(MaterialType.LUPULO)
                    .supplier("HopsCo")
                    .value(15.00)
                    .stock(2000.0)
                    .unitMeasurement(UnitMeasurement.KG)
                    .threshold(1.0)
                    .warehouseZone(WarehouseZone.LUPULO)
                    .warehouseSection("A2")
                    .warehouseLevel(2)
                    .build();
            MaterialResponseDTO lupuloCitraResponse = materialService.createMaterial(lupuloCitra);
            lupuloCitraId = lupuloCitraResponse.getId();

            MaterialCreateDTO lupuloSimcoe = MaterialCreateDTO.builder()
                    .name("Lúpulo Simcoe")
                    .type(MaterialType.LUPULO)
                    .supplier("HopsCo")
                    .value(18.00)
                    .stock(1500.0)
                    .unitMeasurement(UnitMeasurement.KG)
                    .threshold(1.0)
                    .warehouseZone(WarehouseZone.LUPULO)
                    .warehouseSection("B3")
                    .warehouseLevel(1)
                    .build();
            MaterialResponseDTO lupuloSimcoeResponse = materialService.createMaterial(lupuloSimcoe);
            lupuloSimcoeId = lupuloSimcoeResponse.getId();

            // Levaduras
            MaterialCreateDTO levaduraAle = MaterialCreateDTO.builder()
                    .name("Levadura Ale")
                    .type(MaterialType.LEVADURA)
                    .supplier("Fermentos AR")
                    .value(8.00)
                    .stock(2000.0)
                    .unitMeasurement(UnitMeasurement.KG)
                    .threshold(0.2)
                    .warehouseZone(WarehouseZone.LEVADURA)
                    .warehouseSection("A1")
                    .warehouseLevel(2)
                    .build();
            MaterialResponseDTO levaduraAleResponse = materialService.createMaterial(levaduraAle);
            levaduraAleId = levaduraAleResponse.getId();

            MaterialCreateDTO levaduraLager = MaterialCreateDTO.builder()
                    .name("Levadura Lager")
                    .type(MaterialType.LEVADURA)
                    .supplier("Fermentos AR")
                    .value(10.00)
                    .stock(1500.0)
                    .unitMeasurement(UnitMeasurement.KG)
                    .threshold(0.2)
                    .warehouseZone(WarehouseZone.LEVADURA)
                    .warehouseSection("B2")
                    .warehouseLevel(3)
                    .build();
            MaterialResponseDTO levaduraLagerResponse = materialService.createMaterial(levaduraLager);
            levaduraLagerId = levaduraLagerResponse.getId();

            // Agua
            MaterialCreateDTO agua = MaterialCreateDTO.builder()
                    .name("Agua potable")
                    .type(MaterialType.AGUA)
                    .supplier("Acueducto Local")
                    .value(0.50)
                    .stock(1000000.0)
                    .unitMeasurement(UnitMeasurement.LT)
                    .threshold(500.0)
                    .warehouseZone(WarehouseZone.AGUA)
                    .warehouseSection("B1")
                    .warehouseLevel(1)
                    .build();
            MaterialResponseDTO aguaResponse = materialService.createMaterial(agua);
            aguaId = aguaResponse.getId();

            // Otros materiales
            MaterialCreateDTO clarificante = MaterialCreateDTO.builder()
                    .name("Clarificante")
                    .type(MaterialType.OTROS)
                    .supplier("Química Brews")
                    .value(25.00)
                    .stock(5000.0)
                    .unitMeasurement(UnitMeasurement.LT)
                    .threshold(2.0)
                    .warehouseZone(WarehouseZone.OTROS)
                    .warehouseSection("A1")
                    .warehouseLevel(1)
                    .build();
            MaterialResponseDTO clarificanteResponse = materialService.createMaterial(clarificante);
            clarificanteId = clarificanteResponse.getId();

            MaterialCreateDTO co2 = MaterialCreateDTO.builder()
                    .name("CO2")
                    .type(MaterialType.OTROS)
                    .supplier("Gases SRL")
                    .value(3.50)
                    .stock(50000.0)
                    .unitMeasurement(UnitMeasurement.KG)
                    .threshold(5.0)
                    .warehouseZone(WarehouseZone.OTROS)
                    .warehouseSection("A2")
                    .warehouseLevel(1)
                    .build();
            MaterialResponseDTO co2Response = materialService.createMaterial(co2);
            co2Id = co2Response.getId();

            MaterialCreateDTO adsorbente = MaterialCreateDTO.builder()
                    .name("Adsorbente columna")
                    .type(MaterialType.OTROS)
                    .supplier("Química Brews")
                    .value(45.00)
                    .stock(5000.0)
                    .unitMeasurement(UnitMeasurement.KG)
                    .threshold(1.0)
                    .warehouseZone(WarehouseZone.OTROS)
                    .warehouseSection("B1")
                    .warehouseLevel(1)
                    .build();
            MaterialResponseDTO adsorbenteResponse = materialService.createMaterial(adsorbente);
            adsorbenteId = adsorbenteResponse.getId();

            // Envases
            MaterialCreateDTO botella330 = MaterialCreateDTO.builder()
                    .name("Botella 330ml")
                    .type(MaterialType.ENVASE)
                    .supplier("Envases SA")
                    .value(0.25)
                    .stock(500000.0)
                    .unitMeasurement(UnitMeasurement.UNIDAD)
                    .threshold(200.0)
                    .warehouseZone(WarehouseZone.ENVASE)
                    .warehouseSection("A1")
                    .warehouseLevel(2)
                    .build();
            MaterialResponseDTO botella330Response = materialService.createMaterial(botella330);
            botella330Id = botella330Response.getId();

            MaterialCreateDTO barril20L = MaterialCreateDTO.builder()
                    .name("Barril 20L")
                    .type(MaterialType.ENVASE)
                    .supplier("Envases SA")
                    .value(15.00)
                    .stock(5000.0)
                    .unitMeasurement(UnitMeasurement.UNIDAD)
                    .threshold(5.0)
                    .warehouseZone(WarehouseZone.ENVASE)
                    .warehouseSection("C1")
                    .warehouseLevel(1)
                    .build();
            MaterialResponseDTO barril20LResponse = materialService.createMaterial(barril20L);
            barril20LId = barril20LResponse.getId();

            // Etiquetado
            MaterialCreateDTO etiquetaBotella = MaterialCreateDTO.builder()
                    .name("Etiqueta para Botella 330ml")
                    .type(MaterialType.ETIQUETADO)
                    .supplier("Etiquetas SRL")
                    .value(0.05)
                    .stock(500000.0)
                    .unitMeasurement(UnitMeasurement.UNIDAD)
                    .threshold(200.0)
                    .warehouseZone(WarehouseZone.ETIQUETADO)
                    .warehouseSection("A2")
                    .warehouseLevel(3)
                    .build();
            MaterialResponseDTO etiquetaBotellaResponse = materialService.createMaterial(etiquetaBotella);
            etiquetaBotellaId = etiquetaBotellaResponse.getId();

            MaterialCreateDTO etiquetaBarril = MaterialCreateDTO.builder()
                    .name("Etiqueta para Barril 20L")
                    .type(MaterialType.ETIQUETADO)
                    .supplier("Etiquetas SRL")
                    .value(1.00)
                    .stock(5000.0)
                    .unitMeasurement(UnitMeasurement.UNIDAD)
                    .threshold(5.0)
                    .warehouseZone(WarehouseZone.ETIQUETADO)
                    .warehouseSection("B1")
                    .warehouseLevel(2)
                    .build();
            MaterialResponseDTO etiquetaBarrilResponse = materialService.createMaterial(etiquetaBarril);
            etiquetaBarrilId = etiquetaBarrilResponse.getId();

            log.info("Materiales cargados con stock suficiente para múltiples órdenes de producción.");
        }
    }
}

