package com.enigcode.frozen_backend.common.service;

import com.enigcode.frozen_backend.production_orders.Repository.ProductionOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductionOrderDataLoaderService {

    private final ProductionOrderRepository productionOrderRepository;
    private final DataSource dataSource;

    public void loadSampleProductionOrdersAndBatchesFromSQL() {
        if (productionOrderRepository.count() > 0) {
            log.info("Ya existen órdenes de producción, omitiendo carga de datos de ejemplo.");
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            log.info("Ejecutando scripts SQL para cargar órdenes y lotes de ejemplo...");

            String[] scriptFiles = {
                    "sample-production-order-1-5.sql",
                    "sample-production-order-1-15.sql",
                    "sample-production-order-2-8.sql",
                    "sample-production-order-3-15.sql",
                    "sample-production-order-4-20.sql",
                    "sample-production-order-5-18.sql",
                    "sample-production-order-5-25.sql",
                    "sample-production-order-7-15.sql",
                    "sample-production-order-8-10.sql",
                    "sample-production-order-9-20.sql",
                    "sample-production-order-10-5.sql",
                    "sample-production-order-11-2.sql",
                    "sample-production-order-11-10.sql"
            };

            for (String scriptFile : scriptFiles) {
                ClassPathResource resource = new ClassPathResource(scriptFile);
                String sqlScript = new String(resource.getInputStream().readAllBytes(),
                        java.nio.charset.StandardCharsets.UTF_8);

                try (java.sql.Statement statement = connection.createStatement()) {
                    statement.execute(sqlScript);
                    log.info("Script {} ejecutado exitosamente.", scriptFile);
                }
            }

            log.info("Todos los scripts SQL ejecutados exitosamente.");
        } catch (Exception e) {
            log.error("Error ejecutando scripts SQL para cargar órdenes y lotes: {}",
                    e.getMessage(), e);
            throw new RuntimeException("Error cargando órdenes y lotes desde SQL: " + e.getMessage(), e);
        }
    }
}

