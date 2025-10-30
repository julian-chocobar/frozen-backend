package com.enigcode.frozen_backend.notifications.model;

/**
 * Tipos de notificaciones del sistema
 */
public enum NotificationType {
    PRODUCTION_ORDER_PENDING, // Nueva orden de producción pendiente de aprobación
    PRODUCTION_ORDER_APPROVED, // Orden de producción aprobada
    PRODUCTION_ORDER_REJECTED, // Orden de producción rechazada
    SYSTEM_REMINDER, // Recordatorio del sistema
    PENDING_MOVEMENT, // Movimiento de material pendiente de completar
    LOW_STOCK_ALERT // Alerta de stock bajo
}