package com.enigcode.frozen_backend.notifications.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Servicio para manejar conexiones Server-Sent Events (SSE)
 * Permite enviar notificaciones en tiempo real a usuarios conectados
 * IMPORTANTE: NO realiza queries a la BD para evitar connection leaks
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SseNotificationService {

    private static final Long SSE_TIMEOUT = 30 * 60 * 1000L; // 30 minutos
    private static final int MAX_CONNECTIONS_PER_USER = 2;
    private static final long HEARTBEAT_INTERVAL_MS = 30_000L; // 30s
    private static final long CACHE_CLEANUP_INTERVAL_MS = 10 * 60 * 1000L; // 10 minutos
    private static final long DEAD_CONNECTION_CLEANUP_INTERVAL_MS = 2 * 60 * 1000L; // 2 minutos

    // Map para almacenar conexiones SSE por usuario
    private final Map<Long, Set<SseEmitter>> userConnections = new ConcurrentHashMap<>();
    // Cache en memoria username -> userId para evitar queries desde SSE
    private final Map<String, Long> usernameToUserIdCache = new ConcurrentHashMap<>();

    /**
     * Crea una nueva conexión SSE para un usuario por username (SIN consulta DB)
     * Usa el cache en memoria para evitar connection leaks
     */
    public SseEmitter createConnectionByUsername(String username) {
        // Buscar userId en cache (sin consultas DB)
        Long userId = usernameToUserIdCache.get(username);
        if (userId == null) {
            log.error("Usuario {} no encontrado en cache. Debe hacer login primero.", username);
            throw new RuntimeException("Usuario no encontrado en cache: " + username);
        }
        return createConnection(userId);
    }

    /**
     * Registra un usuario en el cache cuando hace login (llamar desde UserService)
     */
    public void registerUserInCache(String username, Long userId) {
        usernameToUserIdCache.put(username, userId);
        log.debug("Usuario {} registrado en cache SSE con ID {}", username, userId);
    }

    /**
     * Remueve un usuario del cache cuando hace logout (llamar desde logout)
     */
    public void removeUserFromCache(String username) {
        Long removedUserId = usernameToUserIdCache.remove(username);
        if (removedUserId != null) {
            log.debug("Usuario {} removido del cache SSE", username);
        }
    }

    /**
     * Remueve el usuario del cache solo si no tiene conexiones SSE activas
     */
    public void removeUserFromCacheIfNoActiveConnections(String username) {
        Long userId = usernameToUserIdCache.get(username);
        if (userId == null) {
            return;
        }
        Set<SseEmitter> connections = userConnections.get(userId);
        if (connections == null || connections.isEmpty()) {
            usernameToUserIdCache.remove(username);
            log.debug("Usuario {} removido del cache SSE (sin conexiones activas)", username);
        } else {
            log.debug("Usuario {} no removido del cache SSE ({} conexiones activas)", username, connections.size());
        }
    }

    /**
     * Crea una nueva conexión SSE para un usuario
     */
    public SseEmitter createConnection(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // Agregar el emitter al conjunto de conexiones del usuario con límite
        Set<SseEmitter> set = userConnections.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>());
        while (set.size() >= MAX_CONNECTIONS_PER_USER) {
            SseEmitter old = set.iterator().hasNext() ? set.iterator().next() : null;
            if (old == null)
                break;
            set.remove(old);
            try {
                old.complete();
            } catch (Exception ignored) {
            }
        }
        set.add(emitter);

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
            // Suprimir logs de desconexiones normales del cliente
            if (ex instanceof IOException) {
                handleSseConnectionError(userId, (IOException) ex, "callback de error");
            } else if (ex instanceof org.springframework.web.context.request.async.AsyncRequestNotUsableException) {
                // Desconexión normal del cliente - no logear
                log.debug("Cliente desconectado normalmente para usuario {}", userId);
            } else {
                log.debug("Error en conexión SSE para usuario {}: {}", userId, ex.getClass().getSimpleName());
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
     * Envía heartbeats periódicos para mantener viva la conexión y detectar
     * clientes caídos.
     * Si el envío falla, se remueve la conexión.
     */
    @Scheduled(fixedRate = HEARTBEAT_INTERVAL_MS)
    public void sendHeartbeats() {
        if (userConnections.isEmpty())
            return;
        userConnections.forEach((userId, connections) -> {
            if (connections == null || connections.isEmpty())
                return;
            // Copia para evitar ConcurrentModification
            Set<SseEmitter> snapshot = new CopyOnWriteArraySet<>(connections);
            snapshot.forEach(emitter -> {
                try {
                    emitter.send(SseEmitter.event().name("heartbeat").comment("ping"));
                } catch (IOException e) {
                    handleSseConnectionError(userId, e, "heartbeat");
                    removeConnection(userId, emitter);
                }
            });
        });
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
                // Limpiar cache si no hay conexiones activas
                cleanupCacheForUser(userId);
            }
        }
    }

    /**
     * Limpia las entradas del cache para un userId específico si no tiene conexiones activas
     */
    private void cleanupCacheForUser(Long userId) {
        usernameToUserIdCache.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(userId)) {
                log.debug("Limpiando entrada de cache para usuario: {} (userId: {})", entry.getKey(), userId);
                return true;
            }
            return false;
        });
    }

    /**
     * Cierra y limpia todas las conexiones activas para un usuario especificado por
     * ID.
     */
    public void closeAllConnectionsForUser(Long userId) {
        Set<SseEmitter> connections = userConnections.remove(userId);
        if (connections != null) {
            connections.forEach(emitter -> {
                try {
                    emitter.complete();
                } catch (Exception ignored) {
                }
            });
            log.debug("Conexiones SSE cerradas para usuario: {}", userId);
        }
    }

    /**
     * Cierra y limpia todas las conexiones activas para un usuario especificado por
     * username.
     */
    public void closeAllConnectionsForUsername(String username) {
        Long userId = usernameToUserIdCache.get(username);
        if (userId != null) {
            closeAllConnectionsForUser(userId);
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
     * Obtiene el número total de conexiones SSE activas
     */
    public int getTotalConnections() {
        return userConnections.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    /**
     * Obtiene el número de usuarios únicos conectados
     */
    public int getConnectedUsersCount() {
        return userConnections.size();
    }

    /**
     * Obtiene información detallada de conexiones por usuario
     */
    public Map<Long, Integer> getConnectionsPerUser() {
        Map<Long, Integer> result = new HashMap<>();
        userConnections.forEach((userId, connections) -> result.put(userId, connections.size()));
        return result;
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

    /**
     * Limpieza periódica del cache de usuarios que no tienen conexiones activas.
     * Previene el crecimiento indefinido del cache en memoria.
     */
    @Scheduled(fixedRate = CACHE_CLEANUP_INTERVAL_MS)
    public void cleanupStaleCacheEntries() {
        if (usernameToUserIdCache.isEmpty()) {
            return;
        }

        int initialSize = usernameToUserIdCache.size();
        usernameToUserIdCache.entrySet().removeIf(entry -> {
            Long userId = entry.getValue();
            Set<SseEmitter> connections = userConnections.get(userId);
            if (connections == null || connections.isEmpty()) {
                log.debug("Limpiando entrada de cache stale para usuario: {} (userId: {})", 
                        entry.getKey(), userId);
                return true;
            }
            return false;
        });

        int removed = initialSize - usernameToUserIdCache.size();
        if (removed > 0) {
            log.info("Limpieza de cache SSE: {} entradas removidas, {} restantes", 
                    removed, usernameToUserIdCache.size());
        }
    }

    /**
     * Limpieza periódica de conexiones muertas que no fueron removidas correctamente
     * por los callbacks. Esto previene memory leaks en caso de errores silenciosos.
     * Limpia sets vacíos y usuarios sin conexiones activas.
     */
    @Scheduled(fixedRate = DEAD_CONNECTION_CLEANUP_INTERVAL_MS)
    public void cleanupDeadConnections() {
        if (userConnections.isEmpty()) {
            return;
        }

        var userIdsToRemove = new java.util.ArrayList<Long>();

        // Identificar usuarios sin conexiones o con sets vacíos
        userConnections.forEach((userId, connections) -> {
            if (connections == null || connections.isEmpty()) {
                userIdsToRemove.add(userId);
            }
        });

        // Remover usuarios sin conexiones y limpiar su cache
        int usersRemoved = 0;
        for (Long userId : userIdsToRemove) {
            userConnections.remove(userId);
            cleanupCacheForUser(userId);
            usersRemoved++;
        }

        if (usersRemoved > 0) {
            log.info("Limpieza de conexiones SSE muertas: {} usuarios sin conexiones removidos", usersRemoved);
        }
    }
}