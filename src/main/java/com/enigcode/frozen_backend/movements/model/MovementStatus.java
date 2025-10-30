package com.enigcode.frozen_backend.movements.model;

/**
 * Estado de los movimientos de materiales
 */
public enum MovementStatus {
    PENDIENTE, // El movimiento ha sido creado pero no ejecutado
    EN_PROCESO, // El movimiento está en proceso de ejecución
    COMPLETADO // El movimiento ha sido ejecutado por un operario
}