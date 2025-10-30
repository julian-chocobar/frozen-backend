package com.enigcode.frozen_backend.notifications.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuración para habilitar tareas programadas
 * Necesario para la limpieza automática de notificaciones
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}