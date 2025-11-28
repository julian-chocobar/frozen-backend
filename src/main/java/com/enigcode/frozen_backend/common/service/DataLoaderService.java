package com.enigcode.frozen_backend.common.service;

import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.enigcode.frozen_backend.packagings.repository.PackagingRepository;
import com.enigcode.frozen_backend.products.DTO.ProductResponseDTO;
import com.enigcode.frozen_backend.products.repository.ProductRepository;
import com.enigcode.frozen_backend.production_orders.Repository.ProductionOrderRepository;
import com.enigcode.frozen_backend.sectors.repository.SectorRepository;
import com.enigcode.frozen_backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio principal que orquesta la carga de datos de ejemplo.
 * Delega la responsabilidad a servicios especializados para mantener el código organizado y mantenible.
 */
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

    // Servicios especializados para cargar datos
    private final UserDataLoaderService userDataLoaderService;
    private final MaterialDataLoaderService materialDataLoaderService;
    private final ProductDataLoaderService productDataLoaderService;
    private final RecipeDataLoaderService recipeDataLoaderService;
    private final PackagingDataLoaderService packagingDataLoaderService;
    private final SystemConfigurationDataLoaderService systemConfigurationDataLoaderService;
    private final SectorDataLoaderService sectorDataLoaderService;
    private final QualityParameterDataLoaderService qualityParameterDataLoaderService;
    private final ProductionOrderDataLoaderService productionOrderDataLoaderService;

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
                userDataLoaderService.loadUsers();
                                log.info("Usuarios cargados exitosamente.");

                // Paso 2: Crear materiales
                                log.info("Iniciando carga de materiales...");
                materialDataLoaderService.loadMaterials();
                                log.info("Materiales cargados exitosamente.");

                                // Paso 3: Crear productos (inicialmente no ready, con fases automáticas)
                                log.info("Iniciando carga de productos...");
                List<ProductResponseDTO> products = productDataLoaderService.loadProducts();
                                log.info("Productos cargados exitosamente: {}", products.size());

                                // Paso 4: Crear recipes para las fases de cada producto
                                log.info("Iniciando carga de recipes...");
                recipeDataLoaderService.loadRecipes(products);
                                log.info("Recipes cargadas exitosamente.");

                                // Paso 5: Configurar datos técnicos de las fases
                                log.info("Configurando datos técnicos de las fases...");
                productDataLoaderService.configureProductPhasesData(products);
                                log.info("Datos técnicos de las fases configurados exitosamente.");

                                

                // Paso 6: Marcar todas las fases como listas
                                log.info("Marcando todas las fases de productos como listas...");
                productDataLoaderService.markAllPhasesAsReady(products);
                                log.info("Todas las fases marcadas como listas y productos marcados como listos exitosamente.");

                                // Paso 7: Crear packagings
                                log.info("Iniciando carga de packagings...");
                packagingDataLoaderService.loadPackagings();
                                log.info("Packagings cargados exitosamente.");

                                // Paso 8: Crear SystemConfiguration con días laborales
                                log.info("Iniciando configuración de días laborales...");
                systemConfigurationDataLoaderService.loadSystemConfiguration();
                                log.info("SystemConfiguration creada/actualizada exitosamente.");

                                // Paso 9: Crear sectores de producción disponibles
                                log.info("Iniciando carga de sectores de producción...");
                sectorDataLoaderService.loadSectors();
                                log.info("Sectores cargados exitosamente.");

                                // Paso 10: Crear parámetros de calidad para las fases
                                log.info("Iniciando carga de parámetros de calidad...");
                qualityParameterDataLoaderService.loadQualityParameters();
                                log.info("Parámetros de calidad cargados exitosamente.");

                                // Paso 11: Crear órdenes de producción y lotes de ejemplo usando script SQL
                                log.info("Iniciando carga de órdenes de producción y lotes de ejemplo...");
                productionOrderDataLoaderService.loadSampleProductionOrdersAndBatchesFromSQL();
                                log.info("Órdenes de producción y lotes cargados exitosamente.");

                                log.info("Datos de ejemplo cargados exitosamente siguiendo reglas de negocio!");

                        } catch (Exception e) {
                                log.error("Error durante la carga de datos de ejemplo: {}", e.getMessage(), e);
                                throw new RuntimeException("Falló la carga de datos de ejemplo: " + e.getMessage(), e);
                        }
                } else {
                        log.info("Los datos ya existen, omitiendo carga de datos de ejemplo.");
                }
    }
}
