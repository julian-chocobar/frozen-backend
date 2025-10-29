package com.enigcode.frozen_backend.notifications.controller;

import com.enigcode.frozen_backend.notifications.dto.NotificationResponseDTO;
import com.enigcode.frozen_backend.notifications.dto.NotificationStatsDTO;
import com.enigcode.frozen_backend.notifications.dto.PollingWindowDTO;
import com.enigcode.frozen_backend.notifications.service.NotificationService;
import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para el manejo de notificaciones
 * Optimizado para polling en ventanas específicas (09:00-10:00 y 17:00-18:00)
 * Incluye cache, rate limiting y logging para cargas puntuales predecibles
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @Operation(summary = "Obtener notificaciones del usuario", description = "Obtiene todas las notificaciones del usuario actual con paginación. Optimizado para ventanas de polling específicas.")
    @GetMapping
    @Cacheable(value = "notifications", key = "#root.target.getUserCacheKey(#unreadOnly, #pageable)", condition = "!#unreadOnly", unless = "#result.body.get('totalItems') == 0")
    public ResponseEntity<Map<String, Object>> getUserNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest request) {

        User currentUser = userService.getCurrentUser();

        // Log para monitorear las ventanas de polling
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
    @Cacheable(value = "notificationStats", key = "#root.target.getStatsKey()", unless = "#result.body.unreadCount == 0")
    public ResponseEntity<NotificationStatsDTO> getNotificationStats(HttpServletRequest request) {
        User currentUser = userService.getCurrentUser();

        log.debug("Solicitud de estadísticas - Usuario: {}, IP: {}",
                currentUser.getUsername(), getClientIpAddress(request));

        NotificationStatsDTO stats = notificationService.getUserNotificationStats(currentUser.getId());
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    /**
     * Métodos auxiliares para optimización y logging
     */

    /**
     * Genera clave de cache para notificaciones por usuario
     */
    public String getUserCacheKey(boolean unreadOnly, Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        return String.format("user_%d_unread_%b_page_%d_size_%d",
                currentUser.getId(), unreadOnly, pageable.getPageNumber(), pageable.getPageSize());
    }

    /**
     * Genera clave de cache para estadísticas por usuario
     */
    public String getStatsKey() {
        User currentUser = userService.getCurrentUser();
        return String.format("stats_user_%d", currentUser.getId());
    }

    /**
     * Endpoint para obtener configuración de ventanas de polling
     */
    @Operation(summary = "Obtener ventanas de polling", description = "Devuelve la configuración de ventanas de polling con estado actual")
    @GetMapping("/polling-windows")
    @Cacheable(value = "pollingWindows")
    public ResponseEntity<List<PollingWindowDTO>> getPollingWindows(HttpServletRequest request) {

        String clientIp = getClientIpAddress(request);
        log.debug("Solicitud de ventanas de polling desde IP: {}", clientIp);

        List<PollingWindowDTO> windows = PollingWindowDTO.getDefaultWindows();

        // Verificar qué ventanas están activas actualmente
        LocalTime currentTime = LocalTime.now();
        windows.forEach(window -> {
            boolean isActive = !currentTime.isBefore(window.getStartTime()) &&
                    !currentTime.isAfter(window.getEndTime());
            window.setIsActive(isActive);

            if (isActive) {
                log.info("Ventana de polling activa: {} ({}–{})",
                        window.getName(), window.getStartTime(), window.getEndTime());
            }
        });

        return ResponseEntity.ok(windows);
    }

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
}