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
    LOW_STOCK_ALERT, // Alerta de stock bajo
    BATCH_STARTED, // Lote iniciado en producción
    NEXT_PHASE_READY, // Próxima fase lista para iniciar
    PHASE_UNDER_REVIEW, // Fase enviada a revisión de calidad
    PHASE_ADJUSTMENT_REQUIRED, // Fase requiere ajuste
    PHASE_REJECTED_BATCH_CANCELLED, // Fase rechazada y lote cancelado
    QUALITY_PARAMETER_ENTERED // Parámetro de calidad ingresado
}