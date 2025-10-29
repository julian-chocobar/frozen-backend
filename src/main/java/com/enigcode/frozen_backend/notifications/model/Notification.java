package com.enigcode.frozen_backend.notifications.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Entidad para manejar notificaciones del sistema
 * Las notificaciones se crean automáticamente para usuarios específicos
 * basándose en eventos del sistema como órdenes de producción pendientes
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_gen")
    @SequenceGenerator(name = "notification_gen", sequenceName = "notification_seq", allocationSize = 1)
    private Long id;

    /**
     * ID del usuario que recibirá la notificación
     */
    @NotNull
    @Column(name = "user_id")
    private Long userId;

    /**
     * Tipo de notificación
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    /**
     * Mensaje de la notificación
     */
    @NotNull
    @Column(length = 500)
    private String message;

    /**
     * ID de la entidad relacionada (ej: ID de la orden de producción)
     * Permite navegar directamente al elemento que generó la notificación
     */
    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    /**
     * Indica si la notificación fue leída por el usuario
     */
    @NotNull
    @Builder.Default
    private Boolean isRead = false;

    /**
     * Fecha y hora de creación de la notificación
     */
    @NotNull
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /**
     * Fecha y hora en que el usuario leyó la notificación
     */
    private OffsetDateTime readAt;

    /**
     * Marca la notificación como leída
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = OffsetDateTime.now();
    }
}