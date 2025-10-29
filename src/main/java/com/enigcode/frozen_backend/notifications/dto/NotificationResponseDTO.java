package com.enigcode.frozen_backend.notifications.dto;

import com.enigcode.frozen_backend.notifications.model.NotificationType;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * DTO de respuesta para notificaciones
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDTO {

    private Long id;
    private Long userId;
    private NotificationType type;
    private String message;
    private Long relatedEntityId;
    private Boolean isRead;
    private OffsetDateTime createdAt;
    private OffsetDateTime readAt;
}