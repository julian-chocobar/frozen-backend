package com.enigcode.frozen_backend.notifications.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de monitoreo para conexiones SSE
 * Proporciona métricas de salud y uso de recursos
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SseMonitoringService {

    private final SseNotificationService sseNotificationService;

    /**
     * Verifica el estado de salud de SSE
     */
    public boolean isHealthy() {
        try {
            Map<String, Object> sseMetrics = getSseMetrics();
            int totalConnections = (Integer) sseMetrics.get("totalConnections");

            // Verificar si está dentro de límites razonables para PyME (20 empleados)
            return totalConnections <= 25;

        } catch (Exception e) {
            log.error("Error verificando salud de SSE: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene métricas detalladas de SSE
     */
    public Map<String, Object> getSseMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // Métricas básicas
        int totalConnections = sseNotificationService.getTotalConnections();
        int connectedUsers = sseNotificationService.getConnectedUsersCount();

        metrics.put("totalConnections", totalConnections);
        metrics.put("connectedUsers", connectedUsers);
        metrics.put("avgConnectionsPerUser", connectedUsers > 0 ? (double) totalConnections / connectedUsers : 0);

        // Estimaciones de recursos
        double estimatedMemoryUsageMB = Math.round((totalConnections * 50 * 1024) / (1024.0 * 1024.0) * 100) / 100.0; // 50KB
                                                                                                                      // por
                                                                                                                      // conexión
        metrics.put("estimatedMemoryUsageMB", estimatedMemoryUsageMB);

        // Status según escenarios de PyME
        String loadStatus;
        if (totalConnections <= 6) {
            loadStatus = "NORMAL"; // 30% empleados conectados
        } else if (totalConnections <= 10) {
            loadStatus = "HIGH"; // 50% empleados conectados
        } else if (totalConnections <= 20) {
            loadStatus = "PEAK"; // 100% empleados conectados
        } else {
            loadStatus = "OVERLOAD"; // Más de lo esperado
        }

        metrics.put("loadStatus", loadStatus);

        // Recomendaciones de pool DB basadas en conexiones actuales
        int recommendedPoolSize = calculateRecommendedPoolSize(totalConnections);
        metrics.put("recommendedHikariPoolSize", recommendedPoolSize);

        return metrics;
    }

    /**
     * Calcula el tamaño recomendado del pool basado en conexiones SSE actuales
     */
    private int calculateRecommendedPoolSize(int sseConnections) {
        // Fórmula: (SSE × 1.2) + (API × 2) + Buffer
        int sseComponent = (int) Math.ceil(sseConnections * 1.2);
        int apiComponent = 5 * 2; // 5 peticiones API concurrentes típicas
        int buffer = 10;

        return sseComponent + apiComponent + buffer;
    }

    /**
     * Log de estado cada cierto tiempo (para debugging)
     */
    public void logCurrentStatus() {
        Map<String, Object> metrics = getSseMetrics();
        log.info("📊 SSE Status: {} conexiones, {} usuarios, {} MB memoria estimada, status: {}",
                metrics.get("totalConnections"),
                metrics.get("connectedUsers"),
                metrics.get("estimatedMemoryUsageMB"),
                metrics.get("loadStatus"));
    }
}