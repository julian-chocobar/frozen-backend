package com.enigcode.frozen_backend.notifications.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Servicio para manejar conexiones Server-Sent Events (SSE)
 * Permite enviar notificaciones en tiempo real a usuarios conectados
 */
@Service
@Slf4j
public class SseNotificationService {

    private static final Long SSE_TIMEOUT = 30 * 60 * 1000L; // 30 minutos

    // Map para almacenar conexiones SSE por usuario
    private final Map<Long, Set<SseEmitter>> userConnections = new ConcurrentHashMap<>();

    /**
     * Crea una nueva conexión SSE para un usuario
     */
    public SseEmitter createConnection(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // Agregar el emitter al conjunto de conexiones del usuario
        userConnections.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(emitter);

        // Configurar callbacks para limpiar cuando la conexión se cierre o expire
        emitter.onCompletion(() -> {
            log.debug("Conexión SSE completada para usuario: {}", userId);
            removeConnection(userId, emitter);
        });

        emitter.onTimeout(() -> {
            log.debug("Conexión SSE expirada para usuario: {}", userId);
            removeConnection(userId, emitter);
        });

        emitter.onError((ex) -> {
            if (ex instanceof IOException) {
                handleSseConnectionError(userId, (IOException) ex, "callback de error");
            } else {
                log.warn("Error no-IOException en conexión SSE para usuario {}: {}", userId, ex.getMessage());
            }
            removeConnection(userId, emitter);
        });

        log.info("Nueva conexión SSE creada para usuario: {}", userId);

        // Enviar evento inicial de conexión con delay para evitar problemas de timing
        try {
            // Enviar un heartbeat inicial
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("{\"status\":\"connected\",\"userId\":" + userId + "}"));
        } catch (IOException e) {
            handleSseConnectionError(userId, e, "enviando evento inicial");
            removeConnection(userId, emitter);
        }
        return emitter;
    }

    /**
     * Envía datos iniciales a un usuario (notificaciones + estadísticas)
     */
    public void sendInitialData(Long userId, Object notifications, Object stats) {
        Set<SseEmitter> connections = userConnections.get(userId);
        if (connections != null && !connections.isEmpty()) {
            connections.forEach(emitter -> {
                try {
                    // Enviar notificaciones existentes
                    if (notifications != null) {
                        emitter.send(SseEmitter.event()
                                .name("initial-notifications")
                                .data(notifications));
                    }

                    // Enviar estadísticas
                    if (stats != null) {
                        emitter.send(SseEmitter.event()
                                .name("stats-update")
                                .data(stats));
                    }
                } catch (IOException e) {
                    handleSseConnectionError(userId, e, "enviando datos iniciales");
                    removeConnection(userId, emitter);
                }
            });
        }
    }

    /**
     * Envía una notificación a un usuario específico
     */
    public void sendNotificationToUser(Long userId, Object notification) {
        Set<SseEmitter> connections = userConnections.get(userId);
        if (connections != null && !connections.isEmpty()) {
            log.debug("Enviando notificación SSE a usuario: {} ({} conexiones activas)",
                    userId, connections.size());

            // Crear una copia para evitar modificaciones concurrentes
            Set<SseEmitter> connectionsToSend = new CopyOnWriteArraySet<>(connections);

            connectionsToSend.forEach(emitter -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("notification")
                            .data(notification));
                } catch (IOException e) {
                    handleSseConnectionError(userId, e, "enviando notificación");
                    removeConnection(userId, emitter);
                }
            });
        } else {
            log.debug("No hay conexiones SSE activas para usuario: {}", userId);
        }
    }

    /**
     * Envía una notificación a múltiples usuarios basado en sus roles
     */
    public void sendNotificationToUsers(Set<Long> userIds, Object notification) {
        userIds.forEach(userId -> sendNotificationToUser(userId, notification));
    }

    /**
     * Envía un evento de actualización de estadísticas a un usuario
     */
    public void sendStatsUpdate(Long userId, Object stats) {
        Set<SseEmitter> connections = userConnections.get(userId);
        if (connections != null && !connections.isEmpty()) {
            connections.forEach(emitter -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("stats-update")
                            .data(stats));
                } catch (IOException e) {
                    handleSseConnectionError(userId, e, "enviando actualización de estadísticas");
                    removeConnection(userId, emitter);
                }
            });
        }
    }

    /**
     * Remueve una conexión específica de un usuario
     */
    private void removeConnection(Long userId, SseEmitter emitter) {
        Set<SseEmitter> connections = userConnections.get(userId);
        if (connections != null) {
            connections.remove(emitter);
            if (connections.isEmpty()) {
                userConnections.remove(userId);
                log.debug("Todas las conexiones SSE removidas para usuario: {}", userId);
            }
        }
    }

    /**
     * Obtiene el número de conexiones activas para un usuario
     */
    public int getActiveConnectionsCount(Long userId) {
        Set<SseEmitter> connections = userConnections.get(userId);
        return connections != null ? connections.size() : 0;
    }

    /**
     * Obtiene el número total de conexiones activas
     */
    public int getTotalActiveConnections() {
        return userConnections.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    /**
     * Maneja errores de conexión SSE de manera más específica
     */
    private void handleSseConnectionError(Long userId, IOException e, String operation) {
        String errorMessage = e.getMessage();

        // Errores comunes de SSE que no requieren logging completo
        if (isCommonSseError(errorMessage)) {
            log.debug("Cliente desconectado durante {}: usuario {} - {}", operation, userId, errorMessage);
        } else {
            log.warn("Error SSE {} para usuario {}: {}", operation, userId, errorMessage);
        }
    }

    /**
     * Determina si es un error común de SSE (desconexión del cliente)
     */
    private boolean isCommonSseError(String errorMessage) {
        if (errorMessage == null)
            return false;

        return errorMessage.contains("Se ha anulado una conexión establecida") ||
                errorMessage.contains("Connection reset") ||
                errorMessage.contains("Broken pipe") ||
                errorMessage.contains("Connection aborted") ||
                errorMessage.contains("Cliente desconectado") ||
                errorMessage.contains("Stream closed");
    }
}