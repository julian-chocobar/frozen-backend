package com.enigcode.frozen_backend.common.service;

import com.enigcode.frozen_backend.materials.DTO.MaterialCreateDTO;
import com.enigcode.frozen_backend.materials.DTO.MaterialResponseDTO;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.enigcode.frozen_backend.materials.service.MaterialService;
import com.enigcode.frozen_backend.packagings.DTO.PackagingCreateDTO;
import com.enigcode.frozen_backend.packagings.repository.PackagingRepository;
import com.enigcode.frozen_backend.packagings.service.PackagingService;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseResponseDTO;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseUpdateDTO;
import com.enigcode.frozen_backend.product_phases.service.ProductPhaseService;
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
import com.enigcode.frozen_backend.users.DTO.UserCreateDTO;
import com.enigcode.frozen_backend.users.DTO.UserResponseDTO;
import com.enigcode.frozen_backend.users.repository.UserRepository;
import com.enigcode.frozen_backend.users.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

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

        private final UserService userService;
        private final MaterialService materialService;
        private final ProductService productService;
        private final ProductPhaseService productPhaseService;
        private final RecipeService recipeService;
        private final PackagingService packagingService;
        private final SectorService sectorService;

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

                                // Paso 6: Marcar productos como ready después de tener recipes
                                log.info("Marcando productos como ready...");
                                markProductsAsReady(products);
                                log.info("Productos marcados como ready exitosamente.");

                                // Paso 7: Crear packagings
                                log.info("Iniciando carga de packagings...");
                                loadPackagings();
                                log.info("Packagings cargados exitosamente.");

                                /*
                                 * // Paso 7: Crear sectores
                                 * log.info("Iniciando carga de sectores...");
                                 * loadSectors();
                                 * log.info("Sectores cargados exitosamente.");
                                 */
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
                                        .password("Admin123")
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
                                        .password("GerenteGen123")
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
                                        .password("GerentePlanta123")
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
                                        .password("SupProd123")
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
                                        .password("SupCal123")
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
                                        .password("SupAlmacen123")
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
                                        .password("OperarioProd123")
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
                                        .password("OperarioCal123")
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
                                        .password("OperarioAlmacen123")
                                        .name("Pedro Operario")
                                        .email("operarioalmacen@frozen.com")
                                        .phoneNumber("1234567892")
                                        .roles(Set.of("OPERARIO_DE_ALMACEN"))
                                        .build();
                        UserResponseDTO operarioAlmacenResponse = userService.createUser(operarioAlmacen);
                        operarioAlmacenId = operarioAlmacenResponse.getId();

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
                                        .build();
                        MaterialResponseDTO maltaChocolateResponse = materialService.createMaterial(maltaChocolate);
                        maltaChocolateId = maltaChocolateResponse.getId();

                        // Lúpulos - Stock suficiente para múltiples órdenes
                        MaterialCreateDTO lupuloCitra = MaterialCreateDTO.builder()
                                        .name("Lúpulo Citra")
                                        .type(MaterialType.LUPULO)
                                        .supplier("HopsCo")
                                        .value(15.00)
                                        .stock(2000.0) // Stock suficiente para muchas órdenes
                                        .unitMeasurement(UnitMeasurement.KG)
                                        .threshold(1.0)
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
                                        .build();
                        MaterialResponseDTO lupuloSimcoeResponse = materialService.createMaterial(lupuloSimcoe);
                        lupuloSimcoeId = lupuloSimcoeResponse.getId();

                        // Levaduras - Stock suficiente para múltiples fermentaciones
                        MaterialCreateDTO levaduraAle = MaterialCreateDTO.builder()
                                        .name("Levadura Ale")
                                        .type(MaterialType.LEVADURA)
                                        .supplier("Fermentos AR")
                                        .value(8.00)
                                        .stock(2000.0) // Stock amplio para múltiples fermentaciones
                                        .unitMeasurement(UnitMeasurement.KG)
                                        .threshold(0.2)
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
                                        .build();
                        MaterialResponseDTO levaduraLagerResponse = materialService.createMaterial(levaduraLager);
                        levaduraLagerId = levaduraLagerResponse.getId();

                        // Agua - Stock muy grande, recurso principal
                        MaterialCreateDTO agua = MaterialCreateDTO.builder()
                                        .name("Agua potable")
                                        .type(MaterialType.AGUA)
                                        .supplier("Acueducto Local")
                                        .value(0.50)
                                        .stock(1000000.0) // Stock masivo de agua
                                        .unitMeasurement(UnitMeasurement.LT)
                                        .threshold(500.0)
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
                                        .build();
                        MaterialResponseDTO barril20LResponse = materialService.createMaterial(barril20L);
                        barril20LId = barril20LResponse.getId();

                        // Etiquetado
                        MaterialCreateDTO etiquetaBotella = MaterialCreateDTO.builder()
                                        .name("Etiqueta para Botella 330ml")
                                        .type(MaterialType.ETIQUETADO)
                                        .supplier("Etiquetas SRL")
                                        .value(0.05)
                                        .stock(500000.0) // Stock masivo de etiquetas
                                        .unitMeasurement(UnitMeasurement.UNIDAD)
                                        .threshold(200.0)
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

        private void markProductsAsReady(List<ProductResponseDTO> products) {
                log.info("Marcando productos como ready después de tener recipes...");

                for (ProductResponseDTO product : products) {
                        try {
                                productService.toggleReady(product.getId());
                                log.info("Producto {} marcado como ready", product.getName());
                        } catch (Exception e) {
                                log.warn("No se pudo marcar el producto {} como ready: {}", product.getName(),
                                                e.getMessage());
                        }
                }

                log.info("Productos marcados como ready.");
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

        @SuppressWarnings("unused")
        private void loadSectors() {
                if (sectorRepository.count() == 0) {
                        log.info("Cargando sectores de ejemplo...");

                        SectorCreateDTO almacen = SectorCreateDTO.builder()
                                        .name("Almacén Principal")
                                        .supervisorId(supervisorAlmacenId)
                                        .type(SectorType.ALMACEN)
                                        .productionCapacity(1000.0)
                                        .isTimeActive(true)
                                        .build();
                        sectorService.createSector(almacen);

                        SectorCreateDTO produccion = SectorCreateDTO.builder()
                                        .name("Línea de Producción")
                                        .supervisorId(supervisorProduccionId)
                                        .type(SectorType.PRODUCCION)
                                        .productionCapacity(500.0)
                                        .isTimeActive(true)
                                        .build();
                        sectorService.createSector(produccion);

                        SectorCreateDTO calidad = SectorCreateDTO.builder()
                                        .name("Control de Calidad")
                                        .supervisorId(supervisorCalidadId)
                                        .type(SectorType.CALIDAD)
                                        .productionCapacity(200.0)
                                        .isTimeActive(true)
                                        .build();
                        sectorService.createSector(calidad);

                        log.info("Sectores cargados.");
                }
        }

}