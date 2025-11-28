package com.enigcode.frozen_backend.notifications.controller;

import com.enigcode.frozen_backend.notifications.dto.NotificationResponseDTO;
import com.enigcode.frozen_backend.notifications.dto.NotificationStatsDTO;
import com.enigcode.frozen_backend.notifications.service.NotificationService;
import com.enigcode.frozen_backend.notifications.service.SseNotificationService;
import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para el manejo de notificaciones
 * Soporta notificaciones en tiempo real vía Server-Sent Events (SSE)
 * Incluye logging y gestión completa de notificaciones persistidas
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;
    private final SseNotificationService sseNotificationService;

    @Operation(summary = "Obtener notificaciones del usuario", description = "Obtiene todas las notificaciones del usuario actual con paginación.")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest request) {

        User currentUser = userService.getCurrentUser();

        // Log para monitoreo de solicitudes
        log.info("Solicitud de notificaciones - Usuario: {}, UnreadOnly: {}, IP: {}, UserAgent: {}",
                currentUser.getUsername(), unreadOnly,
                getClientIpAddress(request), request.getHeader("User-Agent"));

        Page<NotificationResponseDTO> notifications;
        if (unreadOnly) {
            notifications = notificationService.getUserUnreadNotifications(currentUser.getId(), pageable);
        } else {
            notifications = notificationService.getUserNotifications(currentUser.getId(), pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications.getContent());
        response.put("currentPage", notifications.getNumber());
        response.put("totalItems", notifications.getTotalElements());
        response.put("totalPages", notifications.getTotalPages());
        response.put("size", notifications.getSize());
        response.put("isFirst", notifications.isFirst());
        response.put("isLast", notifications.isLast());
        response.put("hasNext", notifications.hasNext());
        response.put("hasPrevious", notifications.hasPrevious());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Marcar notificación como leída", description = "Marca una notificación específica como leída")
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        NotificationResponseDTO notification = notificationService.markAsRead(id, currentUser.getId());
        return new ResponseEntity<>(notification, HttpStatus.OK);
    }

    @Operation(summary = "Marcar todas las notificaciones como leídas", description = "Marca todas las notificaciones del usuario como leídas")
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        User currentUser = userService.getCurrentUser();
        notificationService.markAllAsRead(currentUser.getId());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Obtener estadísticas de notificaciones", description = "Obtiene el conteo de notificaciones leídas y no leídas del usuario")
    @GetMapping("/stats")
    public ResponseEntity<NotificationStatsDTO> getNotificationStats(HttpServletRequest request) {
        User currentUser = userService.getCurrentUser();

        log.debug("Solicitud de estadísticas - Usuario: {}, IP: {}",
                currentUser.getUsername(), getClientIpAddress(request));

        NotificationStatsDTO stats = notificationService.getUserNotificationStats(currentUser.getId());
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    /**
     * Métodos auxiliares para logging y utilidades
     */

    /**
     * Extrae la IP real del cliente considerando proxies
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Tomar solo la primera IP si hay múltiples
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Obtiene el username del contexto de seguridad SIN consultas a DB
     */
    private String getCurrentUsername() {
        try {
            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !authentication.getName().equals("anonymousUser")) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.warn("Error obteniendo username del SecurityContext: {}", e.getMessage());
        }
        return null;
    }

    @Operation(summary = "Conectar a notificaciones en tiempo real", description = "Establece una conexión Server-Sent Events para recibir notificaciones en tiempo real")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> streamNotifications(HttpServletRequest request) {
        // Obtener username desde SecurityContext SIN CONSULTA DB
        String username = getCurrentUsername();
        if (username == null) {
            log.error("Usuario no autenticado intentando crear conexión SSE");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String clientIp = getClientIpAddress(request);
        log.info("Nueva conexión SSE solicitada por usuario: {} desde IP: {}", username, clientIp);

        SseEmitter emitter;
        try {
            // Intentar crear conexión usando cache primero (más rápido, sin DB)
            emitter = sseNotificationService.createConnectionByUsername(username);
        } catch (RuntimeException e) {
            // Cache miss: el usuario no está en cache pero la sesión sigue activa
            // Obtener userId desde BD y registrar en cache
            log.warn("Cache miss para usuario {} en SSE. Repoblando cache desde BD.", username);
            try {
                User currentUser = userService.getCurrentUser();
                if (currentUser != null && currentUser.getUsername().equals(username)) {
                    // Crear conexión y registrar en cache simultáneamente
                    emitter = sseNotificationService.createConnectionAndRegisterInCache(username, currentUser.getId());
                    log.info("Conexión SSE creada y cache repoblado para usuario: {} (ID: {})", username, currentUser.getId());
                } else {
                    log.error("Usuario {} no encontrado o no coincide con usuario autenticado.", username);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
            } catch (Exception ex) {
                log.error("Error al obtener usuario {} para repoblar cache SSE: {}", username, ex.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache")
                .header("Connection", "keep-alive")
                .header("Content-Type", "text/event-stream; charset=UTF-8")
                .header("X-Accel-Buffering", "no")
                .body(emitter);
    }

    @Operation(summary = "Obtener información de conexiones SSE", description = "Obtiene información sobre las conexiones activas del usuario")
    @GetMapping("/connections")
    public ResponseEntity<Map<String, Object>> getConnectionInfo() {
        // Obtener ID de usuario sin mantener transacción abierta
        Long currentUserId;
        try {
            User currentUser = userService.getCurrentUser();
            currentUserId = currentUser.getId();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> connectionInfo = new HashMap<>();
        Map<Long, Integer> connectionsPerUser = sseNotificationService.getConnectionsPerUser();

        connectionInfo.put("userConnections", connectionsPerUser.getOrDefault(currentUserId, 0));
        connectionInfo.put("totalSystemConnections", sseNotificationService.getTotalConnections());
        connectionInfo.put("totalUsers", sseNotificationService.getConnectedUsersCount());

        return ResponseEntity.ok(connectionInfo);
    }

    @Operation(summary = "Endpoint de prueba SSE", description = "Prueba la conectividad SSE")
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnectivity(HttpServletRequest request) {
        User currentUser = userService.getCurrentUser();

        Map<String, Object> testInfo = new HashMap<>();
        testInfo.put("userId", currentUser.getId());
        testInfo.put("username", currentUser.getUsername());
        testInfo.put("timestamp", System.currentTimeMillis());
        testInfo.put("clientIP", getClientIpAddress(request));
        testInfo.put("sseConnections", sseNotificationService.getActiveConnectionsCount(currentUser.getId()));
        testInfo.put("origin", request.getHeader("Origin"));
        testInfo.put("userAgent", request.getHeader("User-Agent"));
        testInfo.put("cookies", request.getHeader("Cookie") != null ? "Present" : "None");

        log.info("🧪 Test de conectividad SSE para usuario: {} desde IP: {} - Origin: {}",
                currentUser.getUsername(), getClientIpAddress(request), request.getHeader("Origin"));

        return ResponseEntity.ok(testInfo);
    }

    @Operation(summary = "Diagnóstico CORS", description = "Verifica configuración CORS")
    @GetMapping("/cors-test")
    public ResponseEntity<Map<String, Object>> corsTest(HttpServletRequest request) {
        Map<String, Object> corsInfo = new HashMap<>();
        corsInfo.put("origin", request.getHeader("Origin"));
        corsInfo.put("method", request.getMethod());
        corsInfo.put("timestamp", System.currentTimeMillis());

        // Listar headers importantes para CORS
        Map<String, String> headers = new HashMap<>();
        headers.put("Origin", request.getHeader("Origin"));
        headers.put("Access-Control-Request-Method", request.getHeader("Access-Control-Request-Method"));
        headers.put("Access-Control-Request-Headers", request.getHeader("Access-Control-Request-Headers"));
        corsInfo.put("corsHeaders", headers);

        log.info("🌐 CORS Test - Origin: {}, Method: {}",
                request.getHeader("Origin"), request.getMethod());

        return ResponseEntity.ok(corsInfo);
    }
}