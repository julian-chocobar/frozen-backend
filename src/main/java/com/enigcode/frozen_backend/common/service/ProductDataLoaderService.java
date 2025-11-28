package com.enigcode.frozen_backend.common.service;

import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseResponseDTO;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseUpdateDTO;
import com.enigcode.frozen_backend.product_phases.service.ProductPhaseService;
import com.enigcode.frozen_backend.products.DTO.ProductCreateDTO;
import com.enigcode.frozen_backend.products.DTO.ProductResponseDTO;
import com.enigcode.frozen_backend.products.repository.ProductRepository;
import com.enigcode.frozen_backend.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductDataLoaderService {

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final ProductPhaseService productPhaseService;

    public List<ProductResponseDTO> loadProducts() {
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
                    .standardQuantity(1000.0)
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

    public void configureProductPhasesData(List<ProductResponseDTO> products) {
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

        try {
            productPhaseService.updateProductPhase(phase.getId(), updateData);
            log.debug("Fase {} del producto {} configurada con datos técnicos", phaseName, productName);
        } catch (Exception e) {
            log.error("Error configurando fase {} del producto {}: {}", phaseName, productName, e.getMessage());
        }
    }

    private void configurePaleAlePhase(String phaseName, ProductPhaseUpdateDTO updateData) {
        switch (phaseName) {
            case "MOLIENDA":
                updateData.setEstimatedHours(2.0);
                updateData.setInput(0.0);
                updateData.setOutput(238.0);
                updateData.setOutputUnit(UnitMeasurement.KG);
                break;
            case "MACERACION":
                updateData.setEstimatedHours(5.0);
                updateData.setInput(238.0);
                updateData.setOutput(1150.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
            case "FILTRACION":
                updateData.setEstimatedHours(2.0);
                updateData.setInput(1150.0);
                updateData.setOutput(1100.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
            case "COCCION":
                updateData.setEstimatedHours(2.5);
                updateData.setInput(1100.0);
                updateData.setOutput(1050.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
            case "FERMENTACION":
                updateData.setEstimatedHours(168.0);
                updateData.setInput(1050.0);
                updateData.setOutput(1030.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
            case "MADURACION":
                updateData.setEstimatedHours(240.0);
                updateData.setInput(1030.0);
                updateData.setOutput(1020.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
            case "GASIFICACION":
                updateData.setEstimatedHours(3.0);
                updateData.setInput(1020.0);
                updateData.setOutput(1010.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
            case "ENVASADO":
                updateData.setEstimatedHours(8.0);
                updateData.setInput(1010.0);
                updateData.setOutput(1000.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
        }
    }

    private void configureStoutPhase(String phaseName, ProductPhaseUpdateDTO updateData) {
        switch (phaseName) {
            case "MOLIENDA":
                updateData.setEstimatedHours(2.5);
                updateData.setInput(0.0);
                updateData.setOutput(245.0);
                updateData.setOutputUnit(UnitMeasurement.KG);
                break;
            case "MACERACION":
                updateData.setEstimatedHours(6.0);
                updateData.setInput(245.0);
                updateData.setOutput(1200.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
            case "FILTRACION":
                updateData.setEstimatedHours(2.5);
                updateData.setInput(1200.0);
                updateData.setOutput(1150.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
            case "COCCION":
                updateData.setEstimatedHours(3.0);
                updateData.setInput(1150.0);
                updateData.setOutput(1100.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
            case "FERMENTACION":
                updateData.setEstimatedHours(192.0);
                updateData.setInput(1100.0);
                updateData.setOutput(1050.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
            case "MADURACION":
                updateData.setEstimatedHours(336.0);
                updateData.setInput(1050.0);
                updateData.setOutput(1030.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
            case "GASIFICACION":
                updateData.setEstimatedHours(3.0);
                updateData.setInput(1030.0);
                updateData.setOutput(1020.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
            case "ENVASADO":
                updateData.setEstimatedHours(10.0);
                updateData.setInput(1020.0);
                updateData.setOutput(1000.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
        }
    }

    private void configurePaleSinAlcoholPhase(String phaseName, ProductPhaseUpdateDTO updateData) {
        switch (phaseName) {
            case "MOLIENDA":
                updateData.setEstimatedHours(2.0);
                updateData.setInput(0.0);
                updateData.setOutput(223.0);
                updateData.setOutputUnit(UnitMeasurement.KG);
                break;
            case "MACERACION":
                updateData.setEstimatedHours(5.0);
                updateData.setInput(223.0);
                updateData.setOutput(1150.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
            case "FILTRACION":
                updateData.setEstimatedHours(2.0);
                updateData.setInput(1150.0);
                updateData.setOutput(1100.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
            case "COCCION":
                updateData.setEstimatedHours(2.5);
                updateData.setInput(1100.0);
                updateData.setOutput(1050.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
            case "FERMENTACION":
                updateData.setEstimatedHours(144.0);
                updateData.setInput(1050.0);
                updateData.setOutput(1035.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
            case "DESALCOHOLIZACION":
                updateData.setEstimatedHours(48.0);
                updateData.setInput(1035.0);
                updateData.setOutput(1025.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
            case "MADURACION":
                updateData.setEstimatedHours(120.0);
                updateData.setInput(1025.0);
                updateData.setOutput(1015.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
            case "GASIFICACION":
                updateData.setEstimatedHours(4.0);
                updateData.setInput(1015.0);
                updateData.setOutput(1010.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
            case "ENVASADO":
                updateData.setEstimatedHours(8.0);
                updateData.setInput(1010.0);
                updateData.setOutput(1000.0);
                updateData.setOutputUnit(UnitMeasurement.LT);
                break;
        }
    }

    public void markAllPhasesAsReady(List<ProductResponseDTO> products) {
        log.info("Marcando todas las fases de productos como listas...");

        for (ProductResponseDTO product : products) {
            try {
                List<ProductPhaseResponseDTO> phases = productPhaseService.getByProduct(product.getId());

                for (ProductPhaseResponseDTO phase : phases) {
                    try {
                        if (!phase.getIsReady()) {
                            productPhaseService.toggleReady(phase.getId());
                            log.debug("Fase {} del producto {} marcada como lista",
                                    phase.getPhase().name(), product.getName());
                        }
                    } catch (Exception e) {
                        log.warn("No se pudo marcar como lista la fase {} del producto {}: {}",
                                phase.getPhase().name(), product.getName(), e.getMessage());
                    }
                }

                log.info("Producto {} procesado - todas sus fases han sido marcadas como listas",
                        product.getName());

            } catch (Exception e) {
                log.error("Error procesando fases del producto {}: {}", product.getName(), e.getMessage());
            }
        }

        log.info("Proceso de marcado de fases completado.");
    }
}

