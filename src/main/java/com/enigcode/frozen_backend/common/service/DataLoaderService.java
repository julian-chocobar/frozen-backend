package com.enigcode.frozen_backend.common.service;

import com.enigcode.frozen_backend.materials.DTO.MaterialCreateDTO;
import com.enigcode.frozen_backend.materials.DTO.MaterialResponseDTO;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.materials.model.WarehouseZone;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.enigcode.frozen_backend.materials.service.MaterialService;
import com.enigcode.frozen_backend.packagings.DTO.PackagingCreateDTO;
import com.enigcode.frozen_backend.packagings.repository.PackagingRepository;
import com.enigcode.frozen_backend.packagings.service.PackagingService;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseResponseDTO;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseUpdateDTO;
import com.enigcode.frozen_backend.product_phases.service.ProductPhaseService;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.products.DTO.ProductCreateDTO;
import com.enigcode.frozen_backend.products.DTO.ProductResponseDTO;
import com.enigcode.frozen_backend.products.repository.ProductRepository;
import com.enigcode.frozen_backend.products.service.ProductService;
import com.enigcode.frozen_backend.recipes.DTO.RecipeCreateDTO;
import com.enigcode.frozen_backend.recipes.service.RecipeService;
import com.enigcode.frozen_backend.production_orders.Repository.ProductionOrderRepository;
import com.enigcode.frozen_backend.sectors.DTO.SectorCreateDTO;
import com.enigcode.frozen_backend.sectors.model.SectorType;
import com.enigcode.frozen_backend.sectors.repository.SectorRepository;
import com.enigcode.frozen_backend.sectors.service.SectorService;
import com.enigcode.frozen_backend.quality_parameters.repository.QualityParameterRepository;
import com.enigcode.frozen_backend.quality_parameters.service.QualityParameterService;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterCreateDTO;
import com.enigcode.frozen_backend.users.DTO.UserCreateDTO;
import com.enigcode.frozen_backend.users.DTO.UserResponseDTO;
import com.enigcode.frozen_backend.users.repository.UserRepository;
import com.enigcode.frozen_backend.users.service.UserService;
import com.enigcode.frozen_backend.system_configurations.service.SystemConfigurationService;
import com.enigcode.frozen_backend.system_configurations.DTO.WorkingDayUpdateDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataLoaderService {

        private final UserRepository userRepository;
        private final MaterialRepository materialRepository;
        private final ProductRepository productRepository;
        private final PackagingRepository packagingRepository;
        private final SectorRepository sectorRepository;
        private final ProductionOrderRepository productionOrderRepository;
        private final QualityParameterRepository qualityParameterRepository;

        private final UserService userService;
        private final MaterialService materialService;
        private final ProductService productService;
        private final ProductPhaseService productPhaseService;
        private final RecipeService recipeService;
        private final PackagingService packagingService;
        private final SectorService sectorService;
        private final SystemConfigurationService systemConfigurationService;
        private final QualityParameterService qualityParameterService;

        // IDs de materiales creados para usar en recipes
        private Long maltaPaleId;
        private Long maltaCrystalId;
        @SuppressWarnings("unused")
        private Long maltaChocolateId;
        private Long lupuloCitraId;
        @SuppressWarnings("unused")
        private Long lupuloSimcoeId;
        private Long levaduraAleId;
        @SuppressWarnings("unused")
        private Long levaduraLagerId;
        private Long aguaId;
        private Long clarificanteId;
        private Long co2Id;
        private Long adsorbenteId;
        private Long botella330Id;
        private Long barril20LId;
        private Long etiquetaBotellaId;
        private Long etiquetaBarrilId;

        // IDs de usuarios
        @SuppressWarnings("unused")
        private Long adminId;

        @SuppressWarnings("unused")
        private Long gerenteGeneralId;
        @SuppressWarnings("unused")
        private Long gerentePlantaId;

        private Long supervisorProduccionId;
        private Long supervisorCalidadId;
        private Long supervisorAlmacenId;

        @SuppressWarnings("unused")
        private Long operarioProduccionId;
        @SuppressWarnings("unused")
        private Long operarioCalidadId;
        @SuppressWarnings("unused")
        private Long operarioAlmacenId;
        @SuppressWarnings("unused")
        private Long superUserId;

        public void loadSampleDataIfEmpty() {
                log.info("Verificando si es necesario cargar datos de ejemplo...");

                boolean needsData = userRepository.count() == 0 ||
                                materialRepository.count() == 0 ||
                                productRepository.count() == 0 ||
                                packagingRepository.count() == 0 ||
                                sectorRepository.count() == 0 ||
                                productionOrderRepository.count() == 0;

                if (needsData) {
                        log.info("Cargando datos de ejemplo siguiendo reglas de negocio...");

                        try {
                                // Paso 1: Crear usuarios
                                log.info("Iniciando carga de usuarios...");
                                loadUsers();
                                log.info("Usuarios cargados exitosamente.");

                                // Paso 2: Crear materiales (sin movimientos automáticos)
                                log.info("Iniciando carga de materiales...");
                                loadMaterials();
                                log.info("Materiales cargados exitosamente.");

                                // Paso 3: Crear productos (inicialmente no ready, con fases automáticas)
                                log.info("Iniciando carga de productos...");
                                List<ProductResponseDTO> products = loadProducts();
                                log.info("Productos cargados exitosamente: {}", products.size());

                                // Paso 4: Configurar datos técnicos de las fases
                                log.info("Configurando datos técnicos de las fases...");
                                configureProductPhasesData(products);
                                log.info("Datos técnicos de las fases configurados exitosamente.");

                                // Paso 5: Crear recipes para las fases de cada producto
                                log.info("Iniciando carga de recipes...");
                                loadRecipes(products);
                                log.info("Recipes cargadas exitosamente.");

                                // Paso 6: Marcar todas las fases como listas (esto marcará automáticamente los
                                // productos como listos)
                                log.info("Marcando todas las fases de productos como listas...");
                                markAllPhasesAsReady(products);
                                log.info("Todas las fases marcadas como listas y productos marcados como listos exitosamente.");

                                // Paso 7: Crear packagings
                                log.info("Iniciando carga de packagings...");
                                loadPackagings();
                                log.info("Packagings cargados exitosamente.");

                                // Paso 8: Crear SystemConfiguration con días laborales
                                log.info("Iniciando configuración de días laborales...");
                                loadSystemConfiguration();
                                log.info("SystemConfiguration creada/actualizada exitosamente.");

                                // Paso 9: Crear sectores de producción disponibles
                                log.info("Iniciando carga de sectores de producción...");
                                loadSectors();
                                log.info("Sectores cargados exitosamente.");

                                // Paso 10: Crear parámetros de calidad para las fases
                                log.info("Iniciando carga de parámetros de calidad...");
                                loadQualityParameters();
                                log.info("Parámetros de calidad cargados exitosamente.");
                                log.info("Datos de ejemplo cargados exitosamente siguiendo reglas de negocio!");

                        } catch (Exception e) {
                                log.error("Error durante la carga de datos de ejemplo: {}", e.getMessage(), e);
                                throw new RuntimeException("Falló la carga de datos de ejemplo: " + e.getMessage(), e);
                        }
                } else {
                        log.info("Los datos ya existen, omitiendo carga de datos de ejemplo.");
                }
        }

        private void loadUsers() {
                if (userRepository.count() == 0) {
                        log.info("Cargando usuarios de ejemplo...");

                        // Admin
                        UserCreateDTO admin = UserCreateDTO.builder()
                                        .username("admin")
                                        .password("EnigCode123")
                                        .name("Administrador")
                                        .email("admin@frozen.com")
                                        .phoneNumber("1234567890")
                                        .roles(Set.of("ADMIN"))
                                        .build();
                        UserResponseDTO adminResponse = userService.createUser(admin);
                        adminId = adminResponse.getId();

                        // Gerente General
                        UserCreateDTO gerenteGeneral = UserCreateDTO.builder()
                                        .username("gerentegeneral")
                                        .password("EnigCode123")
                                        .name("Laura Gerente")
                                        .email("gerentegeneral@frozen.com")
                                        .phoneNumber("1234567894")
                                        .roles(Set.of("GERENTE_GENERAL"))
                                        .build();
                        UserResponseDTO gerenteGeneralResponse = userService.createUser(gerenteGeneral);
                        gerenteGeneralId = gerenteGeneralResponse.getId();

                        // Gerente de Planta
                        UserCreateDTO gerentePlanta = UserCreateDTO.builder()
                                        .username("gerenteplanta")
                                        .password("EnigCode123")
                                        .name("Carlos Planta")
                                        .email("gerenteplanta@frozen.com")
                                        .phoneNumber("1234567895")
                                        .roles(Set.of("GERENTE_DE_PLANTA"))
                                        .build();
                        UserResponseDTO gerentePlantaResponse = userService.createUser(gerentePlanta);
                        gerentePlantaId = gerentePlantaResponse.getId();

                        // Supervisor de Producción
                        UserCreateDTO supervisorProduccion = UserCreateDTO.builder()
                                        .username("supervisorproduccion")
                                        .password("EnigCode123")
                                        .name("Ana Producción")
                                        .email("supervisorproduccion@frozen.com")
                                        .phoneNumber("1234567896")
                                        .roles(Set.of("SUPERVISOR_DE_PRODUCCION"))
                                        .build();
                        UserResponseDTO supervisorProduccionResponse = userService.createUser(supervisorProduccion);
                        supervisorProduccionId = supervisorProduccionResponse.getId();

                        // Supervisor de Calidad
                        UserCreateDTO supervisorCalidad = UserCreateDTO.builder()
                                        .username("supervisorcalidad")
                                        .password("EnigCode123")
                                        .name("Miguel Calidad")
                                        .email("supervisorcalidad@frozen.com")
                                        .phoneNumber("1234567897")
                                        .roles(Set.of("SUPERVISOR_DE_CALIDAD"))
                                        .build();
                        UserResponseDTO supervisorCalidadResponse = userService.createUser(supervisorCalidad);
                        supervisorCalidadId = supervisorCalidadResponse.getId();

                        // Supervisor de Almacén
                        UserCreateDTO supervisorAlmacen = UserCreateDTO.builder()
                                        .username("supervisoralmacen")
                                        .password("EnigCode123")
                                        .name("Juan Supervisor Almacen")
                                        .email("supervisoralmacen@frozen.com")
                                        .phoneNumber("1234567891")
                                        .roles(Set.of("SUPERVISOR_DE_ALMACEN"))
                                        .build();
                        UserResponseDTO supervisorResponse = userService.createUser(supervisorAlmacen);
                        supervisorAlmacenId = supervisorResponse.getId();

                        // Operario de Producción
                        UserCreateDTO operarioProduccion = UserCreateDTO.builder()
                                        .username("operarioproduccion")
                                        .password("EnigCode123")
                                        .name("Luis Operario")
                                        .email("operarioproduccion@frozen.com")
                                        .phoneNumber("1234567896")
                                        .roles(Set.of("OPERARIO_DE_PRODUCCION"))
                                        .build();
                        UserResponseDTO operarioProduccionResponse = userService.createUser(operarioProduccion);
                        operarioProduccionId = operarioProduccionResponse.getId();

                        // Operario de Calidad
                        UserCreateDTO operarioCalidad = UserCreateDTO.builder()
                                        .username("operariocalidad")
                                        .password("EnigCode123")
                                        .name("Sofía Operaria")
                                        .email("operariocalidad@frozen.com")
                                        .phoneNumber("1234567897")
                                        .roles(Set.of("OPERARIO_DE_CALIDAD"))
                                        .build();
                        UserResponseDTO operarioCalidadResponse = userService.createUser(operarioCalidad);
                        operarioCalidadId = operarioCalidadResponse.getId();

                        // Operario de Almacén
                        UserCreateDTO operarioAlmacen = UserCreateDTO.builder()
                                        .username("operarioalmacen")
                                        .password("EnigCode123")
                                        .name("Pedro Operario")
                                        .email("operarioalmacen@frozen.com")
                                        .phoneNumber("1234567892")
                                        .roles(Set.of("OPERARIO_DE_ALMACEN"))
                                        .build();
                        UserResponseDTO operarioAlmacenResponse = userService.createUser(operarioAlmacen);
                        operarioAlmacenId = operarioAlmacenResponse.getId();

                        // Super Usuario
                        UserCreateDTO superUser = UserCreateDTO.builder()
                                        .username("super")
                                        .password("EnigCode123")
                                        .name("Super Usuario")
                                        .email("super@frozen.com")
                                        .phoneNumber("1234567890")
                                        .roles(Set.of("ADMIN", "GERENTE_GENERAL", "GERENTE_DE_PLANTA",
                                                        "SUPERVISOR_DE_PRODUCCION", "SUPERVISOR_DE_CALIDAD",
                                                        "SUPERVISOR_DE_ALMACEN", "OPERARIO_DE_PRODUCCION",
                                                        "OPERARIO_DE_CALIDAD", "OPERARIO_DE_ALMACEN"))
                                        .build();
                        UserResponseDTO superUserResponse = userService.createUser(superUser);
                        superUserId = superUserResponse.getId();

                        log.info("Usuarios cargados.");
                }
        }

        private void loadMaterials() {
                if (materialRepository.count() == 0) {
                        log.info("Cargando materiales de ejemplo con stock suficiente para múltiples órdenes de producción...");

                        // Maltas - Stock suficiente para múltiples órdenes
                        MaterialCreateDTO maltaPale = MaterialCreateDTO.builder()
                                        .name("Malta Pale")
                                        .type(MaterialType.MALTA)
                                        .supplier("Molino San Martín")
                                        .value(2.50)
                                        .stock(20000.0) // Stock amplio para múltiples órdenes
                                        .unitMeasurement(UnitMeasurement.KG)
                                        .threshold(50.0)
                                        .warehouseZone(WarehouseZone.MALTA)
                                        .warehouseSection("A1")
                                        .warehouseLevel(2) // Nivel medio para fácil acceso
                                        .build();
                        MaterialResponseDTO maltaPaleResponse = materialService.createMaterial(maltaPale);
                        maltaPaleId = maltaPaleResponse.getId();

                        MaterialCreateDTO maltaCrystal = MaterialCreateDTO.builder()
                                        .name("Malta Crystal")
                                        .type(MaterialType.MALTA)
                                        .supplier("Molino San Martín")
                                        .value(3.00)
                                        .stock(10000.0) // Stock amplio
                                        .unitMeasurement(UnitMeasurement.KG)
                                        .threshold(10.0)
                                        .warehouseZone(WarehouseZone.MALTA)
                                        .warehouseSection("A3")
                                        .warehouseLevel(1) // Nivel bajo para stock mayor
                                        .build();
                        MaterialResponseDTO maltaCrystalResponse = materialService.createMaterial(maltaCrystal);
                        maltaCrystalId = maltaCrystalResponse.getId();

                        MaterialCreateDTO maltaChocolate = MaterialCreateDTO.builder()
                                        .name("Malta Chocolate")
                                        .type(MaterialType.MALTA)
                                        .supplier("Molino San Martín")
                                        .value(4.00)
                                        .stock(8000.0) // Para productos especiales como Stout
                                        .unitMeasurement(UnitMeasurement.KG)
                                        .threshold(5.0)
                                        .warehouseZone(WarehouseZone.MALTA)
                                        .warehouseSection("B2")
                                        .warehouseLevel(3) // Nivel alto, menor rotación
                                        .build();
                        MaterialResponseDTO maltaChocolateResponse = materialService.createMaterial(maltaChocolate);
                        maltaChocolateId = maltaChocolateResponse.getId();

                        // Lúpulos - Stock suficiente para múltiples órdenes (zona refrigerada)
                        MaterialCreateDTO lupuloCitra = MaterialCreateDTO.builder()
                                        .name("Lúpulo Citra")
                                        .type(MaterialType.LUPULO)
                                        .supplier("HopsCo")
                                        .value(15.00)
                                        .stock(2000.0) // Stock suficiente para muchas órdenes
                                        .unitMeasurement(UnitMeasurement.KG)
                                        .threshold(1.0)
                                        .warehouseZone(WarehouseZone.LUPULO)
                                        .warehouseSection("A2")
                                        .warehouseLevel(2) // Nivel medio, refrigerado
                                        .build();
                        MaterialResponseDTO lupuloCitraResponse = materialService.createMaterial(lupuloCitra);
                        lupuloCitraId = lupuloCitraResponse.getId();

                        MaterialCreateDTO lupuloSimcoe = MaterialCreateDTO.builder()
                                        .name("Lúpulo Simcoe")
                                        .type(MaterialType.LUPULO)
                                        .supplier("HopsCo")
                                        .value(18.00)
                                        .stock(1500.0) // Para productos específicos
                                        .unitMeasurement(UnitMeasurement.KG)
                                        .threshold(1.0)
                                        .warehouseZone(WarehouseZone.LUPULO)
                                        .warehouseSection("B3")
                                        .warehouseLevel(1) // Nivel bajo para conservación
                                        .build();
                        MaterialResponseDTO lupuloSimcoeResponse = materialService.createMaterial(lupuloSimcoe);
                        lupuloSimcoeId = lupuloSimcoeResponse.getId();

                        // Levaduras - Stock suficiente para múltiples fermentaciones (zona controlada)
                        MaterialCreateDTO levaduraAle = MaterialCreateDTO.builder()
                                        .name("Levadura Ale")
                                        .type(MaterialType.LEVADURA)
                                        .supplier("Fermentos AR")
                                        .value(8.00)
                                        .stock(2000.0) // Stock amplio para múltiples fermentaciones
                                        .unitMeasurement(UnitMeasurement.KG)
                                        .threshold(0.2)
                                        .warehouseZone(WarehouseZone.LEVADURA)
                                        .warehouseSection("A1")
                                        .warehouseLevel(2) // Nivel medio, fácil acceso
                                        .build();
                        MaterialResponseDTO levaduraAleResponse = materialService.createMaterial(levaduraAle);
                        levaduraAleId = levaduraAleResponse.getId();

                        MaterialCreateDTO levaduraLager = MaterialCreateDTO.builder()
                                        .name("Levadura Lager")
                                        .type(MaterialType.LEVADURA)
                                        .supplier("Fermentos AR")
                                        .value(10.00)
                                        .stock(1500.0) // Para diferentes tipos de fermentación
                                        .unitMeasurement(UnitMeasurement.KG)
                                        .threshold(0.2)
                                        .warehouseZone(WarehouseZone.LEVADURA)
                                        .warehouseSection("B2")
                                        .warehouseLevel(3) // Nivel alto, menor rotación
                                        .build();
                        MaterialResponseDTO levaduraLagerResponse = materialService.createMaterial(levaduraLager);
                        levaduraLagerId = levaduraLagerResponse.getId();

                        // Agua - Stock muy grande, recurso principal (tanques grandes)
                        MaterialCreateDTO agua = MaterialCreateDTO.builder()
                                        .name("Agua potable")
                                        .type(MaterialType.AGUA)
                                        .supplier("Acueducto Local")
                                        .value(0.50)
                                        .stock(1000000.0) // Stock masivo de agua
                                        .unitMeasurement(UnitMeasurement.LT)
                                        .threshold(500.0)
                                        .warehouseZone(WarehouseZone.AGUA)
                                        .warehouseSection("B1")
                                        .warehouseLevel(1) // Nivel bajo, tanques grandes
                                        .build();
                        MaterialResponseDTO aguaResponse = materialService.createMaterial(agua);
                        aguaId = aguaResponse.getId();

                        // Otros materiales - Aditivos y clarificantes
                        MaterialCreateDTO clarificante = MaterialCreateDTO.builder()
                                        .name("Clarificante")
                                        .type(MaterialType.OTROS)
                                        .supplier("Química Brews")
                                        .value(25.00)
                                        .stock(5000.0) // Stock suficiente para múltiples lotes
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
                                        .stock(50000.0) // Stock masivo de CO2 para gasificación
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
                                        .stock(5000.0) // Para procesos de desalcoholización
                                        .unitMeasurement(UnitMeasurement.KG)
                                        .threshold(1.0)
                                        .warehouseZone(WarehouseZone.OTROS)
                                        .warehouseSection("B1")
                                        .warehouseLevel(1)
                                        .build();
                        MaterialResponseDTO adsorbenteResponse = materialService.createMaterial(adsorbente);
                        adsorbenteId = adsorbenteResponse.getId();

                        // Envases - Stock masivo para múltiples órdenes
                        MaterialCreateDTO botella330 = MaterialCreateDTO.builder()
                                        .name("Botella 330ml")
                                        .type(MaterialType.ENVASE)
                                        .supplier("Envases SA")
                                        .value(0.25)
                                        .stock(500000.0) // Stock masivo de botellas
                                        .unitMeasurement(UnitMeasurement.UNIDAD)
                                        .threshold(200.0)
                                        .warehouseZone(WarehouseZone.ENVASE)
                                        .warehouseSection("A1")
                                        .warehouseLevel(2) // Nivel medio, alta rotación
                                        .build();
                        MaterialResponseDTO botella330Response = materialService.createMaterial(botella330);
                        botella330Id = botella330Response.getId();

                        MaterialCreateDTO barril20L = MaterialCreateDTO.builder()
                                        .name("Barril 20L")
                                        .type(MaterialType.ENVASE)
                                        .supplier("Envases SA")
                                        .value(15.00)
                                        .stock(5000.0) // Stock suficiente de barriles
                                        .unitMeasurement(UnitMeasurement.UNIDAD)
                                        .threshold(5.0)
                                        .warehouseZone(WarehouseZone.ENVASE)
                                        .warehouseSection("C1")
                                        .warehouseLevel(1) // Nivel bajo, más pesados
                                        .build();
                        MaterialResponseDTO barril20LResponse = materialService.createMaterial(barril20L);
                        barril20LId = barril20LResponse.getId();

                        // Etiquetado - Materiales ligeros
                        MaterialCreateDTO etiquetaBotella = MaterialCreateDTO.builder()
                                        .name("Etiqueta para Botella 330ml")
                                        .type(MaterialType.ETIQUETADO)
                                        .supplier("Etiquetas SRL")
                                        .value(0.05)
                                        .stock(500000.0) // Stock masivo de etiquetas
                                        .unitMeasurement(UnitMeasurement.UNIDAD)
                                        .threshold(200.0)
                                        .warehouseZone(WarehouseZone.ETIQUETADO)
                                        .warehouseSection("A2")
                                        .warehouseLevel(3) // Nivel alto, material ligero
                                        .build();
                        MaterialResponseDTO etiquetaBotellaResponse = materialService.createMaterial(etiquetaBotella);
                        etiquetaBotellaId = etiquetaBotellaResponse.getId();

                        MaterialCreateDTO etiquetaBarril = MaterialCreateDTO.builder()
                                        .name("Etiqueta para Barril 20L")
                                        .type(MaterialType.ETIQUETADO)
                                        .supplier("Etiquetas SRL")
                                        .value(1.00)
                                        .stock(5000.0) // Stock suficiente de etiquetas para barriles
                                        .unitMeasurement(UnitMeasurement.UNIDAD)
                                        .threshold(5.0)
                                        .warehouseZone(WarehouseZone.ETIQUETADO)
                                        .warehouseSection("B1")
                                        .warehouseLevel(2) // Nivel medio
                                        .build();

                        MaterialResponseDTO etiquetaBarrilResponse = materialService.createMaterial(etiquetaBarril);
                        etiquetaBarrilId = etiquetaBarrilResponse.getId();

                        log.info("Materiales cargados con stock suficiente para múltiples órdenes de producción.");
                }
        }

        private List<ProductResponseDTO> loadProducts() {
                if (productRepository.count() == 0) {
                        log.info("Cargando productos de ejemplo (inicialmente no ready)...");

                        ProductCreateDTO paleAle = ProductCreateDTO.builder()
                                        .name("Pale Ale Clásica")
                                        .isAlcoholic(true)
                                        .standardQuantity(1000.0)
                                        .unitMeasurement(UnitMeasurement.LT)
                                        .build();
                        ProductResponseDTO product1 = productService.createProduct(paleAle);

                        ProductCreateDTO stout = ProductCreateDTO.builder()
                                        .name("Stout Intensa")
                                        .isAlcoholic(true)
                                        .standardQuantity(800.0)
                                        .unitMeasurement(UnitMeasurement.LT)
                                        .build();
                        ProductResponseDTO product2 = productService.createProduct(stout);

                        ProductCreateDTO paleSinAlcohol = ProductCreateDTO.builder()
                                        .name("Pale Sin Alcohol")
                                        .isAlcoholic(false)
                                        .standardQuantity(1000.0)
                                        .unitMeasurement(UnitMeasurement.LT)
                                        .build();
                        ProductResponseDTO product3 = productService.createProduct(paleSinAlcohol);

                        log.info("Productos cargados (fases automáticamente creadas).");
                        return List.of(product1, product2, product3);
                }
                return List.of();
        }

        private void configureProductPhasesData(List<ProductResponseDTO> products) {
                log.info("Configurando datos técnicos para las fases de productos...");

                for (ProductResponseDTO product : products) {
                        List<ProductPhaseResponseDTO> phases = productPhaseService.getByProduct(product.getId());

                        for (ProductPhaseResponseDTO phase : phases) {
                                configurePhaseData(product, phase);
                        }
                }
        }

        private void configurePhaseData(ProductResponseDTO product, ProductPhaseResponseDTO phase) {
                String phaseName = phase.getPhase().name();
                String productName = product.getName();

                ProductPhaseUpdateDTO updateData = new ProductPhaseUpdateDTO();

                // Configurar datos según el tipo de producto y fase
                switch (productName) {
                        case "Pale Ale Clásica":
                                configurePaleAlePhase(phaseName, updateData);
                                break;
                        case "Stout Intensa":
                                configureStoutPhase(phaseName, updateData);
                                break;
                        case "Pale Sin Alcohol":
                                configurePaleSinAlcoholPhase(phaseName, updateData);
                                break;
                }

                // Aplicar la actualización a la fase
                try {
                        productPhaseService.updateProductPhase(phase.getId(), updateData);
                        log.debug("Fase {} del producto {} configurada con datos técnicos", phaseName, productName);
                } catch (Exception e) {
                        log.error("Error configurando fase {} del producto {}: {}", phaseName, productName,
                                        e.getMessage());
                }
        }

        private void configurePaleAlePhase(String phaseName, ProductPhaseUpdateDTO updateData) {
                switch (phaseName) {
                        case "MOLIENDA":
                                updateData.setEstimatedHours(2.0);
                                updateData.setInput(240.0);
                                updateData.setOutput(238.0);
                                updateData.setOutputUnit(UnitMeasurement.KG);
                                break;
                        case "MACERACION":
                                updateData.setEstimatedHours(5.0);
                                updateData.setInput(1200.0);
                                updateData.setOutput(1050.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                        case "FILTRACION":
                                updateData.setEstimatedHours(2.0);
                                updateData.setInput(1050.0);
                                updateData.setOutput(1000.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                        case "COCCION":
                                updateData.setEstimatedHours(2.5);
                                updateData.setInput(1000.0);
                                updateData.setOutput(950.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                        case "FERMENTACION":
                                updateData.setEstimatedHours(168.0); // 7 días
                                updateData.setInput(950.0);
                                updateData.setOutput(920.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                        case "MADURACION":
                                updateData.setEstimatedHours(240.0); // 10 días
                                updateData.setInput(920.0);
                                updateData.setOutput(900.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                        case "GASIFICACION":
                                updateData.setEstimatedHours(3.0);
                                updateData.setInput(900.0);
                                updateData.setOutput(900.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                        case "ENVASADO":
                                updateData.setEstimatedHours(8.0);
                                updateData.setInput(900.0);
                                updateData.setOutput(895.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                }
        }

        private void configureStoutPhase(String phaseName, ProductPhaseUpdateDTO updateData) {
                switch (phaseName) {
                        case "MOLIENDA":
                                updateData.setEstimatedHours(2.5);
                                updateData.setInput(220.0);
                                updateData.setOutput(218.0);
                                updateData.setOutputUnit(UnitMeasurement.KG);
                                break;
                        case "MACERACION":
                                updateData.setEstimatedHours(6.0);
                                updateData.setInput(980.0);
                                updateData.setOutput(850.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                        case "FILTRACION":
                                updateData.setEstimatedHours(2.5);
                                updateData.setInput(850.0);
                                updateData.setOutput(800.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                        case "COCCION":
                                updateData.setEstimatedHours(3.0);
                                updateData.setInput(800.0);
                                updateData.setOutput(750.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                        case "FERMENTACION":
                                updateData.setEstimatedHours(192.0); // 8 días
                                updateData.setInput(750.0);
                                updateData.setOutput(720.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                        case "MADURACION":
                                updateData.setEstimatedHours(336.0); // 14 días
                                updateData.setInput(720.0);
                                updateData.setOutput(705.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                        case "GASIFICACION":
                                updateData.setEstimatedHours(3.0);
                                updateData.setInput(705.0);
                                updateData.setOutput(705.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                        case "ENVASADO":
                                updateData.setEstimatedHours(10.0);
                                updateData.setInput(705.0);
                                updateData.setOutput(700.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                }
        }

        private void configurePaleSinAlcoholPhase(String phaseName, ProductPhaseUpdateDTO updateData) {
                switch (phaseName) {
                        case "MOLIENDA":
                                updateData.setEstimatedHours(2.0);
                                updateData.setInput(225.0);
                                updateData.setOutput(223.0);
                                updateData.setOutputUnit(UnitMeasurement.KG);
                                break;
                        case "MACERACION":
                                updateData.setEstimatedHours(5.0);
                                updateData.setInput(1200.0);
                                updateData.setOutput(1050.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                        case "FILTRACION":
                                updateData.setEstimatedHours(2.0);
                                updateData.setInput(1050.0);
                                updateData.setOutput(1000.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                        case "COCCION":
                                updateData.setEstimatedHours(2.5);
                                updateData.setInput(1000.0);
                                updateData.setOutput(950.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                        case "FERMENTACION":
                                updateData.setEstimatedHours(144.0); // 6 días
                                updateData.setInput(950.0);
                                updateData.setOutput(920.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                        case "DESALCOHOLIZACION":
                                updateData.setEstimatedHours(48.0); // 2 días
                                updateData.setInput(920.0);
                                updateData.setOutput(900.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                        case "MADURACION":
                                updateData.setEstimatedHours(120.0); // 5 días
                                updateData.setInput(900.0);
                                updateData.setOutput(890.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                        case "GASIFICACION":
                                updateData.setEstimatedHours(4.0);
                                updateData.setInput(890.0);
                                updateData.setOutput(890.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                        case "ENVASADO":
                                updateData.setEstimatedHours(8.0);
                                updateData.setInput(890.0);
                                updateData.setOutput(885.0);
                                updateData.setOutputUnit(UnitMeasurement.LT);
                                break;
                }
        }

        private void loadRecipes(List<ProductResponseDTO> products) {
                log.info("Cargando recipes para las fases de cada producto...");

                // Para cada producto, obtener sus fases y crear recipes apropiadas
                for (ProductResponseDTO product : products) {
                        List<ProductPhaseResponseDTO> phases = productPhaseService.getByProduct(product.getId());

                        for (ProductPhaseResponseDTO phase : phases) {
                                createRecipesForPhase(phase);
                        }
                }

                log.info("Recipes cargadas.");
        }

        private void createRecipesForPhase(ProductPhaseResponseDTO phase) {
                String phaseName = phase.getPhase().name();
                Long phaseId = phase.getId();

                switch (phaseName) {
                        case "MOLIENDA":
                                // Necesita malta
                                RecipeCreateDTO maltaRecipe = RecipeCreateDTO.builder()
                                                .productPhaseId(phaseId)
                                                .materialId(maltaPaleId) // Malta Pale
                                                .quantity(180.0)
                                                .build();
                                recipeService.createRecipe(maltaRecipe);

                                RecipeCreateDTO maltaCrystalRecipe = RecipeCreateDTO.builder()
                                                .productPhaseId(phaseId)
                                                .materialId(maltaCrystalId) // Malta Crystal
                                                .quantity(45.0)
                                                .build();
                                recipeService.createRecipe(maltaCrystalRecipe);
                                break;

                        case "MACERACION":
                                // Necesita agua
                                RecipeCreateDTO aguaRecipe = RecipeCreateDTO.builder()
                                                .productPhaseId(phaseId)
                                                .materialId(aguaId) // Agua
                                                .quantity(1200.0)
                                                .build();
                                recipeService.createRecipe(aguaRecipe);
                                break;

                        case "FILTRACION":
                                // Necesita clarificante
                                RecipeCreateDTO clarificanteRecipe = RecipeCreateDTO.builder()
                                                .productPhaseId(phaseId)
                                                .materialId(clarificanteId) // Clarificante
                                                .quantity(2.0)
                                                .build();
                                recipeService.createRecipe(clarificanteRecipe);
                                break;

                        case "COCCION":
                                // Necesita lúpulo
                                RecipeCreateDTO lupuloRecipe = RecipeCreateDTO.builder()
                                                .productPhaseId(phaseId)
                                                .materialId(lupuloCitraId) // Lúpulo Citra
                                                .quantity(6.0)
                                                .build();

                                // Necesita agua
                                RecipeCreateDTO aguaRecipe2 = RecipeCreateDTO.builder()
                                                .productPhaseId(phaseId)
                                                .materialId(aguaId) // Agua
                                                .quantity(50.0)
                                                .build();
                                recipeService.createRecipe(lupuloRecipe);
                                recipeService.createRecipe(aguaRecipe2);
                                break;

                        case "FERMENTACION":
                                // Necesita levadura
                                RecipeCreateDTO levaduraRecipe = RecipeCreateDTO.builder()
                                                .productPhaseId(phaseId)
                                                .materialId(levaduraAleId) // Levadura Ale
                                                .quantity(1.5)
                                                .build();
                                recipeService.createRecipe(levaduraRecipe);
                                break;

                        case "DESALCOHOLIZACION":
                                // Necesita adsorbente
                                RecipeCreateDTO adsorbenteRecipe = RecipeCreateDTO.builder()
                                                .productPhaseId(phaseId)
                                                .materialId(adsorbenteId) // Adsorbente
                                                .quantity(8.0)
                                                .build();
                                recipeService.createRecipe(adsorbenteRecipe);
                                break;

                        case "MADURACION":
                        case "GASIFICACION":
                                // Necesita CO2
                                RecipeCreateDTO co2Recipe = RecipeCreateDTO.builder()
                                                .productPhaseId(phaseId)
                                                .materialId(co2Id) // CO2
                                                .quantity(12.0)
                                                .build();
                                recipeService.createRecipe(co2Recipe);
                                break;

                        /*
                         * case "ENVASADO":
                         * // Necesita envases
                         * RecipeCreateDTO envaseRecipe = RecipeCreateDTO.builder()
                         * .productPhaseId(phaseId)
                         * .materialId(botella330Id) // Botella 330ml
                         * .quantity(3000.0)
                         * .build();
                         * recipeService.createRecipe(envaseRecipe);
                         * break;
                         */
                }
        }

        private void loadPackagings() {
                if (packagingRepository.count() == 0) {
                        log.info("Cargando empaques de ejemplo...");

                        PackagingCreateDTO empaque330ml = PackagingCreateDTO.builder()
                                        .name("Botella 330ml (equiv. en LT)")
                                        .packagingMaterialId(botella330Id) // Botella 330ml
                                        .labelingMaterialId(etiquetaBotellaId) // Usando misma botella como labeling
                                        .unitMeasurement(UnitMeasurement.LT)
                                        .quantity(0.33)
                                        .build();
                        packagingService.createPackaging(empaque330ml);

                        PackagingCreateDTO empaque20L = PackagingCreateDTO.builder()
                                        .name("Barril 20L (equiv. en LT)")
                                        .packagingMaterialId(barril20LId) // Barril 20L
                                        .labelingMaterialId(etiquetaBarrilId) // Usando mismo barril como labeling
                                        .unitMeasurement(UnitMeasurement.LT)
                                        .quantity(20.0)
                                        .build();
                        packagingService.createPackaging(empaque20L);

                        log.info("Empaques cargados.");
                }
        }

        private void loadSystemConfiguration() {
                // Asegura que exista una configuración activa (el service crea una por defecto
                // si no hay)
                systemConfigurationService.getSystemConfiguration();

                // Definir lunes a viernes laborales 09:00 - 17:00, fines de semana no laborales
                LocalTime open = LocalTime.of(9, 0);
                LocalTime close = LocalTime.of(17, 0);

                List<WorkingDayUpdateDTO> updates = List.of(
                                WorkingDayUpdateDTO.builder().dayOfWeek(DayOfWeek.MONDAY).isWorkingDay(true)
                                                .openingHour(open).closingHour(close).build(),
                                WorkingDayUpdateDTO.builder().dayOfWeek(DayOfWeek.TUESDAY).isWorkingDay(true)
                                                .openingHour(open).closingHour(close).build(),
                                WorkingDayUpdateDTO.builder().dayOfWeek(DayOfWeek.WEDNESDAY).isWorkingDay(true)
                                                .openingHour(open).closingHour(close).build(),
                                WorkingDayUpdateDTO.builder().dayOfWeek(DayOfWeek.THURSDAY).isWorkingDay(true)
                                                .openingHour(open).closingHour(close).build(),
                                WorkingDayUpdateDTO.builder().dayOfWeek(DayOfWeek.FRIDAY).isWorkingDay(true)
                                                .openingHour(open).closingHour(close).build(),
                                WorkingDayUpdateDTO.builder().dayOfWeek(DayOfWeek.SATURDAY).isWorkingDay(false)
                                                .openingHour(null).closingHour(null).build(),
                                WorkingDayUpdateDTO.builder().dayOfWeek(DayOfWeek.SUNDAY).isWorkingDay(false)
                                                .openingHour(null).closingHour(null).build());

                systemConfigurationService.updateWorkingDays(updates);
        }

        private void loadSectors() {
                if (sectorRepository.count() == 0) {
                        log.info("Cargando sectores de ejemplo...");

                        SectorCreateDTO almacen = SectorCreateDTO.builder()
                                        .name("Almacén Principal")
                                        .supervisorId(supervisorAlmacenId)
                                        .type(SectorType.ALMACEN)
                                        .build();
                        sectorService.createSector(almacen);

                        double highCapacity = 5000.0; // Permite múltiples lotes simultáneos
                        for (Phase phase : Phase.values()) {
                                if (Phase.MADURACION.equals(phase) || Phase.FERMENTACION.equals(phase)) {
                                        // Permitir procesos pasivos con capacidad amplia
                                        createProductionSector(
                                                        "Sector " + capitalize(phase.name().toLowerCase()),
                                                        supervisorProduccionId,
                                                        phase,
                                                        6000.0);
                                } else {
                                        createProductionSector(
                                                        "Sector " + capitalize(phase.name().toLowerCase()),
                                                        supervisorProduccionId,
                                                        phase,
                                                        highCapacity);
                                }
                        }

                        SectorCreateDTO calidad = SectorCreateDTO.builder()
                                        .name("Control de Calidad Central")
                                        .supervisorId(supervisorCalidadId)
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

        private void markAllPhasesAsReady(List<ProductResponseDTO> products) {
                log.info("Marcando todas las fases de productos como listas...");

                for (ProductResponseDTO product : products) {
                        try {
                                List<ProductPhaseResponseDTO> phases = productPhaseService
                                                .getByProduct(product.getId());

                                for (ProductPhaseResponseDTO phase : phases) {
                                        try {
                                                // Solo marcar como listo si no está ya listo
                                                if (!phase.getIsReady()) {
                                                        productPhaseService.toggleReady(phase.getId());
                                                        log.debug("Fase {} del producto {} marcada como lista",
                                                                        phase.getPhase().name(), product.getName());
                                                }
                                        } catch (Exception e) {
                                                log.warn("No se pudo marcar como lista la fase {} del producto {}: {}",
                                                                phase.getPhase().name(), product.getName(),
                                                                e.getMessage());
                                        }
                                }

                                log.info("Producto {} procesado - todas sus fases han sido marcadas como listas",
                                                product.getName());

                        } catch (Exception e) {
                                log.error("Error procesando fases del producto {}: {}", product.getName(),
                                                e.getMessage());
                        }
                }

                log.info("Proceso de marcado de fases completado.");
        }

        private void loadQualityParameters() {
                if (qualityParameterRepository.count() > 0)
                        return;

                log.info("Generando parámetros de calidad por fase...");
                List<QualityParameterCreateDTO> parameters = List.of(
                                // Molienda
                                // value válido: "350 μm" (granulometría media), cualquier valor mayor a 500 μm
                                // sería rechazado
                                QualityParameterCreateDTO.builder()
                                                .phase(Phase.MOLIENDA)
                                                .name("Granulometria")
                                                .description("Molienda homogénea, base para extracción eficiente.")
                                                .isCritical(true)
                                                .build(),
                                // value válido: "4.5 %", por encima de 6 % se considera húmedo en exceso
                                QualityParameterCreateDTO.builder()
                                                .phase(Phase.MOLIENDA)
                                                .name("Humedad Malta")
                                                .description("Control de humedad post molienda para evitar compactación.")
                                                .isCritical(false)
                                                .build(),
                                // Maceración
                                // value válido: "66 °C", menos de 60 °C afecta conversión enzimática
                                QualityParameterCreateDTO.builder()
                                                .phase(Phase.MACERACION)
                                                .name("Temp Macerac")
                                                .description("Temperatura de maceración en rango objetivo.")
                                                .isCritical(true)
                                                .build(),
                                // value válido: "5.4 pH", si supera 5.8 pH se considera fuera de rango
                                QualityParameterCreateDTO.builder()
                                                .phase(Phase.MACERACION)
                                                .name("pH Macerac")
                                                .description("pH del mosto entre 5.2 - 5.6.")
                                                .isCritical(true)
                                                .build(),
                                // Filtración
                                // value válido: "15 NTU", mayor a 50 NTU indicaría turbidez elevada
                                QualityParameterCreateDTO.builder()
                                                .phase(Phase.FILTRACION)
                                                .name("Claridad")
                                                .description("Nivel de turbidez permisible del mosto filtrado.")
                                                .isCritical(false)
                                                .build(),
                                // value válido: "78 °C", menos de 70 °C podría afectar separación
                                QualityParameterCreateDTO.builder()
                                                .phase(Phase.FILTRACION)
                                                .name("Temp Filtrado")
                                                .description("Temperatura de salida tras filtrado.")
                                                .isCritical(false)
                                                .build(),
                                // Cocción
                                // value válido: "12.5 °P", por debajo de 10 °P indica falta de evaporación
                                QualityParameterCreateDTO.builder()
                                                .phase(Phase.COCCION)
                                                .name("Plato Final")
                                                .description("Grados plato finales tras evaporación.")
                                                .isCritical(true)
                                                .build(),
                                // value válido: "10 min", menos de 5 min reduce el aporte aromático
                                QualityParameterCreateDTO.builder()
                                                .phase(Phase.COCCION)
                                                .name("Tiempo Lupulo")
                                                .description("Tiempo exacto de adición de lúpulo aromático.")
                                                .isCritical(false)
                                                .build(),
                                // Fermentación
                                // value válido: "19 °C", más de 24 °C para ales se considera fuera de rango
                                QualityParameterCreateDTO.builder()
                                                .phase(Phase.FERMENTACION)
                                                .name("Temp Ferm")
                                                .description("Temperatura controlada según perfil de levadura.")
                                                .isCritical(true)
                                                .build(),
                                // value válido: "1.010 SG", si queda por encima de 1.020 SG indica fermentación
                                // incompleta
                                QualityParameterCreateDTO.builder()
                                                .phase(Phase.FERMENTACION)
                                                .name("Densidad Ferm")
                                                .description("Densidad específica diaria para seguimiento de atenuación.")
                                                .isCritical(true)
                                                .build(),
                                // Desalcoholización
                                // value válido: "0.4 % ABV", por encima de 0.5 % ABV no cumple sin alcohol
                                QualityParameterCreateDTO.builder()
                                                .phase(Phase.DESALCOHOLIZACION)
                                                .name("Alcohol Final")
                                                .description("Porcentaje de alcohol residual en cerveza sin alcohol.")
                                                .isCritical(true)
                                                .build(),
                                // value válido: "65 °C", más de 75 °C afecta aromas
                                QualityParameterCreateDTO.builder()
                                                .phase(Phase.DESALCOHOLIZACION)
                                                .name("Temp Columna")
                                                .description("Temperatura de columna de adsorción.")
                                                .isCritical(false)
                                                .build(),
                                // Maduración
                                // value válido: "0.08 ppm", superar 0.15 ppm genera sabores mantecosos
                                QualityParameterCreateDTO.builder()
                                                .phase(Phase.MADURACION)
                                                .name("Diacetilo")
                                                .description("Nivel de diacetilo por debajo del umbral sensorial.")
                                                .isCritical(true)
                                                .build(),
                                // value válido: "5 EBC", mayor a 20 EBC sugiere sedimentos en suspensión
                                QualityParameterCreateDTO.builder()
                                                .phase(Phase.MADURACION)
                                                .name("Turbidez")
                                                .description("Control visual de sedimentos previos a gasificación.")
                                                .isCritical(false)
                                                .build(),
                                // Gasificación
                                // value válido: "2.4 vol CO2", menos de 1.8 vol produce carbonatación
                                // insuficiente
                                QualityParameterCreateDTO.builder()
                                                .phase(Phase.GASIFICACION)
                                                .name("CO2 Volumen")
                                                .description("Volumen final de CO2 disuelto.")
                                                .isCritical(true)
                                                .build(),
                                // value válido: "18 psi", pasar de 25 psi puede comprometer válvulas
                                QualityParameterCreateDTO.builder()
                                                .phase(Phase.GASIFICACION)
                                                .name("Presion Tank")
                                                .description("Presión alcanzada en tanques de gasificación.")
                                                .isCritical(false)
                                                .build(),
                                // Envasado
                                // value válido: "Hermético", cualquier anotación diferente implica fallo de
                                // sellado
                                QualityParameterCreateDTO.builder()
                                                .phase(Phase.ENVASADO)
                                                .name("Sellado")
                                                .description("Integridad del cierre en botellas o barriles.")
                                                .isCritical(true)
                                                .build(),
                                // value válido: "OK", valores como "Desalineada" o "Sin lote" marcan rechazo
                                // visual
                                QualityParameterCreateDTO.builder()
                                                .phase(Phase.ENVASADO)
                                                .name("Etiquetado")
                                                .description("Revisión visual de etiquetado y codificación.")
                                                .isCritical(false)
                                                .build());

                parameters.forEach(qualityParameterService::createQualityParameter);
        }

        private String capitalize(String value) {
                if (value == null || value.isBlank()) {
                        return value;
                }
                return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
        }

}