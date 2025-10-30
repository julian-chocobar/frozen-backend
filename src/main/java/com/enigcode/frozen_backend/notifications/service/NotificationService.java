package com.enigcode.frozen_backend.notifications.service;

import com.enigcode.frozen_backend.notifications.dto.NotificationResponseDTO;
import com.enigcode.frozen_backend.notifications.dto.NotificationStatsDTO;
import com.enigcode.frozen_backend.notifications.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Servicio para manejar notificaciones del sistema
 */
public interface NotificationService {

    /**
     * Crea una nueva notificación para un usuario (uso interno)
     */
    NotificationResponseDTO createNotification(Long userId, NotificationType type, String message,
            Long relatedEntityId);

    /**
     * Obtiene todas las notificaciones de un usuario con paginación
     */
    Page<NotificationResponseDTO> getUserNotifications(Long userId, Pageable pageable);

    /**
     * Obtiene solo las notificaciones no leídas de un usuario
     */
    Page<NotificationResponseDTO> getUserUnreadNotifications(Long userId, Pageable pageable);

    /**
     * Marca una notificación como leída
     */
    NotificationResponseDTO markAsRead(Long notificationId, Long userId);

    /**
     * Marca todas las notificaciones de un usuario como leídas
     */
    void markAllAsRead(Long userId);

    /**
     * Obtiene estadísticas de notificaciones del usuario
     */
    NotificationStatsDTO getUserNotificationStats(Long userId);

    /**
     * Crea notificación automática para órdenes de producción pendientes
     * Este método será llamado desde ProductionOrderService
     */
    void createProductionOrderNotification(Long orderId, String productName);

    /**
     * Crea notificación automática para movimientos pendientes
     * Este método será llamado desde MovementService cuando se crea un movimiento
     */
    void createPendingMovementNotification(Long movementId, String materialName, String movementType);

    /**
     * Crea notificación automática para alerta de stock bajo
     * Este método será llamado desde MovementService cuando un egreso deje stock
     * bajo el umbral
     */
    void createLowStockNotification(Long materialId, String materialName, Double currentStock, Double threshold);

    /**
     * Limpia las notificaciones automáticamente según la configuración
     */
    void cleanupOldNotifications();
}