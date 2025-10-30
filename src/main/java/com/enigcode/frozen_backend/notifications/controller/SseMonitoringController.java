package com.enigcode.frozen_backend.notifications.controller;

import com.enigcode.frozen_backend.notifications.service.SseMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador para métricas y monitoreo de SSE
 * Solo accesible para ADMIN
 */
@RestController
@RequestMapping("/monitoring/sse")
@RequiredArgsConstructor
public class SseMonitoringController {

    private final SseMonitoringService sseMonitoringService;

    /**
     * Obtiene métricas detalladas de SSE
     */
    @GetMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSseMetrics() {
        Map<String, Object> metrics = sseMonitoringService.getSseMetrics();
        return ResponseEntity.ok(metrics);
    }

    /**
     * Verifica el estado de salud de SSE
     */
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSseHealth() {
        boolean healthy = sseMonitoringService.isHealthy();
        Map<String, Object> status = Map.of(
                "healthy", healthy,
                "status", healthy ? "UP" : "DOWN");
        return ResponseEntity.ok(status);
    }
}