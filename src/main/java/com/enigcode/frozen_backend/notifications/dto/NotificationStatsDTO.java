package com.enigcode.frozen_backend.notifications.dto;

import lombok.*;

/**
 * DTO para estadísticas de notificaciones del usuario
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationStatsDTO {

    /**
     * Número de notificaciones no leídas
     */
    private Long unreadCount;

    /**
     * Número total de notificaciones
     */
    private Long totalCount;
}