package com.enigcode.frozen_backend.notifications.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.notifications.dto.NotificationResponseDTO;
import com.enigcode.frozen_backend.notifications.dto.NotificationStatsDTO;
import com.enigcode.frozen_backend.notifications.mapper.NotificationMapper;
import com.enigcode.frozen_backend.notifications.model.Notification;
import com.enigcode.frozen_backend.notifications.model.NotificationType;
import com.enigcode.frozen_backend.notifications.repository.NotificationRepository;
import com.enigcode.frozen_backend.users.model.Role;
import com.enigcode.frozen_backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del servicio de notificaciones
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;

    @Override
    public NotificationResponseDTO createNotification(Long userId, NotificationType type, String message,
            Long relatedEntityId) {
        // Verificar que el usuario existe
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + userId);
        }

        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .message(message)
                .relatedEntityId(relatedEntityId)
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        log.info("Notificación creada para usuario {}: {}", userId, message);

        return notificationMapper.toResponseDTO(savedNotification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getUserNotifications(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(notificationMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getUserUnreadNotifications(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId,
                pageable);
        return notifications.map(notificationMapper::toResponseDTO);
    }

    @Override
    public NotificationResponseDTO markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Notificación no encontrada con ID: " + notificationId));

        // Verificar que la notificación pertenece al usuario
        if (!notification.getUserId().equals(userId)) {
            throw new BadRequestException("No tienes permisos para marcar esta notificación como leída");
        }

        if (!notification.getIsRead()) {
            notification.markAsRead();
            notification = notificationRepository.save(notification);
            log.info("Notificación {} marcada como leída por usuario {}", notificationId, userId);
        }

        return notificationMapper.toResponseDTO(notification);
    }

    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, Pageable.unpaged())
                .getContent();

        unreadNotifications.forEach(Notification::markAsRead);
        notificationRepository.saveAll(unreadNotifications);

        log.info("Todas las notificaciones marcadas como leídas para usuario {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationStatsDTO getUserNotificationStats(Long userId) {
        Long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);
        Long totalCount = notificationRepository.countByUserId(userId);

        return NotificationStatsDTO.builder()
                .unreadCount(unreadCount)
                .totalCount(totalCount)
                .build();
    }

    @Override
    public void createProductionOrderNotification(Long orderId, String productName) {
        // Obtener todos los usuarios con rol GERENTE_DE_PLANTA
        List<Long> plantManagerIds = userRepository.findUserIdsByRole(Role.GERENTE_DE_PLANTA);

        if (plantManagerIds.isEmpty()) {
            log.warn("No se encontraron usuarios con rol GERENTE_DE_PLANTA para notificar sobre orden {}", orderId);
            return;
        }

        String message = String.format("Nueva orden de producción pendiente de aprobación para producto: %s",
                productName);

        // Crear notificación para cada gerente de planta
        for (Long userId : plantManagerIds) {
            // Verificar si ya existe una notificación para esta orden y usuario
            List<Notification> existingNotifications = notificationRepository
                    .findByUserIdAndTypeAndRelatedEntityId(userId, NotificationType.PRODUCTION_ORDER_PENDING, orderId);

            if (existingNotifications.isEmpty()) {
                createNotification(userId, NotificationType.PRODUCTION_ORDER_PENDING, message, orderId);
            }
        }

        log.info("Notificaciones creadas para orden de producción {} para {} gerentes de planta", orderId,
                plantManagerIds.size());
    }
}