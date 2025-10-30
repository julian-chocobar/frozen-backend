package com.enigcode.frozen_backend.movements.model;

/**
 * Estado de los movimientos de materiales
 */
public enum MovementStatus {
    PENDIENTE, // El movimiento ha sido creado pero no ejecutado
    COMPLETADO // El movimiento ha sido ejecutado por un operario
}