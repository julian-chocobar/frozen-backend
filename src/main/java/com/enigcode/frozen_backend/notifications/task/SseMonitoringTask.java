package com.enigcode.frozen_backend.notifications.task;

import com.enigcode.frozen_backend.notifications.service.SseMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tareas programadas para monitoreo de SSE
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SseMonitoringTask {

    private final SseMonitoringService sseMonitoringService;

    /**
     * Log del estado de SSE cada 15 minutos
     */
    @Scheduled(fixedRate = 15 * 60 * 1000) // 15 minutos
    public void logSseStatus() {
        if (sseMonitoringService.getSseMetrics().get("totalConnections").equals(0)) {
            // No loggear si no hay conexiones activas
            return;
        }

        sseMonitoringService.logCurrentStatus();
    }

    /**
     * Alerta si hay demasiadas conexiones (cada 5 minutos)
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5 minutos
    public void checkSseOverload() {
        if (!sseMonitoringService.isHealthy()) {
            log.warn("⚠️  ALERTA: Más de 25 conexiones SSE activas - revisar carga del sistema");
        }
    }
}