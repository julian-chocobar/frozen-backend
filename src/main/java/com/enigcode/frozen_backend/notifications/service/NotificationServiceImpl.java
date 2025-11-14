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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;
    private final SseNotificationService sseNotificationService;

    @Value("${app.notification.cleanup.days:30}")
    private int notificationCleanupDays;

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

        log.info("💾 Guardando notificación en BD para usuario {}", userId);
        Notification savedNotification = notificationRepository.save(notification);
        log.info("✅ Notificación guardada con ID: {}", savedNotification.getId());

        NotificationResponseDTO responseDTO = notificationMapper.toResponseDTO(savedNotification);

        // Enviar notificación en tiempo real vía SSE
        log.info("📡 Enviando notificación SSE para usuario {}", userId);
        sseNotificationService.sendNotificationToUser(userId, responseDTO);

        // Enviar actualización de estadísticas
        log.info("📊 Enviando actualización de estadísticas SSE para usuario {}", userId);
        NotificationStatsDTO stats = getUserNotificationStats(userId);
        sseNotificationService.sendStatsUpdate(userId, stats);

        log.info("🎉 Notificación completamente procesada para usuario {}: {}", userId, message);

        return responseDTO;
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

            // Enviar actualización de estadísticas vía SSE
            NotificationStatsDTO stats = getUserNotificationStats(userId);
            sseNotificationService.sendStatsUpdate(userId, stats);

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
        log.info("🔔 Iniciando creación de notificación para orden de producción {} - producto: {}", orderId,
                productName);

        // Obtener todos los usuarios con rol GERENTE_DE_PLANTA
        List<Long> plantManagerIds = userRepository.findUserIdsByRole(Role.GERENTE_DE_PLANTA);
        log.info("🔍 Usuarios con rol GERENTE_DE_PLANTA encontrados: {}", plantManagerIds);

        if (plantManagerIds.isEmpty()) {
            log.warn("❌ No se encontraron usuarios con rol GERENTE_DE_PLANTA para notificar sobre orden {}", orderId);
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
                log.info("📨 Creando notificación para usuario {} - orden {}", userId, orderId);
                createNotification(userId, NotificationType.PRODUCTION_ORDER_PENDING, message, orderId);
            } else {
                log.info("⚠️ Notificación ya existe para usuario {} - orden {}", userId, orderId);
            }
        }

        log.info("✅ Proceso completado: notificaciones para orden {} enviadas a {} gerentes de planta",
                orderId, plantManagerIds.size());
    }

    @Override
    public void createPendingMovementNotification(Long movementId, String materialName, String movementType) {
        // Obtener todos los usuarios con rol OPERARIO_DE_ALMACEN
        List<Long> warehouseOperatorIds = userRepository.findUserIdsByRole(Role.OPERARIO_DE_ALMACEN);

        if (warehouseOperatorIds.isEmpty()) {
            log.warn("No se encontraron usuarios con rol OPERARIO_DE_ALMACEN para notificar sobre movimiento {}",
                    movementId);
            return;
        }

        String message = String.format("Nuevo movimiento de %s pendiente para material: %s",
                movementType.toLowerCase(), materialName);

        // Crear notificación para cada operario de almacén
        for (Long userId : warehouseOperatorIds) {
            // Verificar si ya existe una notificación para este movimiento y usuario
            List<Notification> existingNotifications = notificationRepository
                    .findByUserIdAndTypeAndRelatedEntityId(userId, NotificationType.PENDING_MOVEMENT, movementId);

            if (existingNotifications.isEmpty()) {
                createNotification(userId, NotificationType.PENDING_MOVEMENT, message, movementId);
            }
        }

        log.info("Notificaciones creadas para movimiento pendiente {} para {} operarios de almacén",
                movementId, warehouseOperatorIds.size());
    }

    @Override
    public void createLowStockNotification(Long materialId, String materialName, Double currentStock,
            Double threshold) {
        // Obtener todos los usuarios con rol SUPERVISOR_DE_ALMACEN
        List<Long> warehouseSupervisorIds = userRepository.findUserIdsByRole(Role.SUPERVISOR_DE_ALMACEN);

        if (warehouseSupervisorIds.isEmpty()) {
            log.warn(
                    "No se encontraron usuarios con rol SUPERVISOR_DE_ALMACEN para notificar sobre stock bajo del material {}",
                    materialId);
            return;
        }

        String message = String.format("ALERTA: Material '%s' tiene stock bajo (%.2f unidades). Umbral mínimo: %.2f",
                materialName, currentStock, threshold);

        // Crear notificación para cada supervisor de almacén
        for (Long userId : warehouseSupervisorIds) {
            // Verificar si ya existe una notificación de stock bajo para este material y
            // usuario en las últimas 24 horas
            List<Notification> recentNotifications = notificationRepository
                    .findRecentLowStockNotifications(userId, materialId, OffsetDateTime.now().minusDays(1));

            if (recentNotifications.isEmpty()) {
                createNotification(userId, NotificationType.LOW_STOCK_ALERT, message, materialId);
            } else {
                log.debug("Ya existe una notificación reciente de stock bajo para material {} y usuario {}", materialId,
                        userId);
            }
        }

        log.info("Notificaciones creadas para stock bajo del material {} para {} supervisores de almacén",
                materialId, warehouseSupervisorIds.size());
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?") // Ejecutar diariamente a las 2:00 AM
    public void cleanupOldNotifications() {
        OffsetDateTime cutoffDate = OffsetDateTime.now().minusDays(notificationCleanupDays);

        List<Notification> oldNotifications = notificationRepository.findOldNotifications(cutoffDate);

        if (!oldNotifications.isEmpty()) {
            notificationRepository.deleteAll(oldNotifications);
            log.info("Limpieza automática: {} notificaciones antiguas eliminadas (anteriores a {})",
                    oldNotifications.size(), cutoffDate);
        } else {
            log.debug("Limpieza automática: No hay notificaciones antiguas para eliminar");
        }
    }

    @Override
    public void createBatchStartedNotification(Long batchId, String batchCode, Long sectorId) {
        log.info("🔔 Creando notificación de lote iniciado para lote {} en sector {}", batchCode, sectorId);

        // Obtener el supervisor del sector específico
        List<Long> supervisorIds = userRepository.findSupervisorIdsBySectorId(sectorId);

        if (supervisorIds.isEmpty()) {
            log.warn("❌ No se encontró supervisor para el sector {} del lote {}", sectorId, batchCode);
            return;
        }

        String message = String.format("El lote %s ha iniciado producción en su sector", batchCode);

        // Crear notificación para cada supervisor del sector
        for (Long supervisorId : supervisorIds) {
            createNotification(supervisorId, NotificationType.BATCH_STARTED, message, batchId);
        }

        log.info("✅ Notificaciones de lote iniciado enviadas a {} supervisores del sector {}",
                supervisorIds.size(), sectorId);
    }

    @Override
    public void createNextPhaseReadyNotification(Long batchId, String batchCode, Long sectorId, String phaseName) {
        log.info("🔔 Creando notificación de próxima fase para lote {} - fase {} en sector {}",
                batchCode, phaseName, sectorId);

        // Obtener el supervisor del sector específico de la próxima fase
        List<Long> supervisorIds = userRepository.findSupervisorIdsBySectorId(sectorId);

        if (supervisorIds.isEmpty()) {
            log.warn("❌ No se encontró supervisor para el sector {} de la fase {}", sectorId, phaseName);
            return;
        }

        String message = String.format("El lote %s está listo para iniciar la fase %s en su sector",
                batchCode, phaseName);

        // Crear notificación para cada supervisor del sector
        for (Long supervisorId : supervisorIds) {
            createNotification(supervisorId, NotificationType.NEXT_PHASE_READY, message, batchId);
        }

        log.info("✅ Notificaciones de próxima fase enviadas a {} supervisores del sector {}",
                supervisorIds.size(), sectorId);
    }

    @Override
    public void createPhaseUnderReviewNotification(Long productionPhaseId, String batchCode, String phaseName) {
        log.info("🔔 Creando notificación de fase bajo revisión para lote {} - fase {}", batchCode, phaseName);

        // Obtener todos los operarios de calidad
        List<Long> qualityOperatorIds = userRepository.findUserIdsByRole(Role.OPERARIO_DE_CALIDAD);

        if (qualityOperatorIds.isEmpty()) {
            log.warn("❌ No se encontraron operarios de calidad para notificar sobre la fase {} del lote {}",
                    phaseName, batchCode);
            return;
        }

        String message = String.format("La fase %s del lote %s está esperando revisión de calidad",
                phaseName, batchCode);

        // Crear notificación para cada operario de calidad
        for (Long operatorId : qualityOperatorIds) {
            createNotification(operatorId, NotificationType.PHASE_UNDER_REVIEW, message, productionPhaseId);
        }

        log.info("✅ Notificaciones de fase bajo revisión enviadas a {} operarios de calidad",
                qualityOperatorIds.size());
    }

    @Override
    public void createPhaseAdjustmentRequiredNotification(Long productionPhaseId, String batchCode, String phaseName,
            Long sectorId) {
        log.info("🔔 Creando notificación de ajuste requerido para lote {} - fase {} en sector {}",
                batchCode, phaseName, sectorId);

        // Obtener el supervisor del sector específico
        List<Long> supervisorIds = userRepository.findSupervisorIdsBySectorId(sectorId);

        if (supervisorIds.isEmpty()) {
            log.warn("❌ No se encontró supervisor para el sector {} de la fase {} que requiere ajuste",
                    sectorId, phaseName);
            return;
        }

        String message = String.format(
                "La fase %s del lote %s requiere ajustes. Por favor, realice las correcciones necesarias",
                phaseName, batchCode);

        // Crear notificación para cada supervisor del sector
        for (Long supervisorId : supervisorIds) {
            createNotification(supervisorId, NotificationType.PHASE_ADJUSTMENT_REQUIRED, message, productionPhaseId);
        }

        log.info("✅ Notificaciones de ajuste requerido enviadas a {} supervisores del sector {}",
                supervisorIds.size(), sectorId);
    }

    @Override
    public void createPhaseRejectedBatchCancelledNotification(Long batchId, String batchCode, String phaseName,
            Long sectorId) {
        log.info("🔔 Creando notificación de lote cancelado por fase rechazada - lote {} - fase {} en sector {}",
                batchCode, phaseName, sectorId);

        // Obtener el supervisor del sector específico
        List<Long> supervisorIds = userRepository.findSupervisorIdsBySectorId(sectorId);

        if (supervisorIds.isEmpty()) {
            log.warn("❌ No se encontró supervisor para el sector {} de la fase {} rechazada",
                    sectorId, phaseName);
            return;
        }

        String message = String.format("CRÍTICO: La fase %s del lote %s ha sido rechazada. El lote ha sido cancelado",
                phaseName, batchCode);

        // Crear notificación para cada supervisor del sector
        for (Long supervisorId : supervisorIds) {
            createNotification(supervisorId, NotificationType.PHASE_REJECTED_BATCH_CANCELLED, message, batchId);
        }

        log.info("✅ Notificaciones de lote cancelado enviadas a {} supervisores del sector {}",
                supervisorIds.size(), sectorId);
    }

    @Override
    public void createQualityParameterEnteredNotification(Long productionPhaseId, String batchCode, String phaseName,
            String parameterName) {
        log.info("🔔 Creando notificación de parámetro de calidad ingresado para lote {} - fase {} - parámetro {}",
                batchCode, phaseName, parameterName);

        // Obtener todos los supervisores de calidad
        List<Long> qualitySupervisorIds = userRepository.findUserIdsByRole(Role.SUPERVISOR_DE_CALIDAD);

        if (qualitySupervisorIds.isEmpty()) {
            log.warn(
                    "❌ No se encontraron supervisores de calidad para notificar sobre el parámetro {} de la fase {} del lote {}",
                    parameterName, phaseName, batchCode);
            return;
        }

        String message = String.format("Nuevo parámetro de calidad '%s' ingresado para la fase %s del lote %s",
                parameterName, phaseName, batchCode);

        // Crear notificación para cada supervisor de calidad
        for (Long supervisorId : qualitySupervisorIds) {
            createNotification(supervisorId, NotificationType.QUALITY_PARAMETER_ENTERED, message, productionPhaseId);
        }

        log.info("✅ Notificaciones de parámetro de calidad enviadas a {} supervisores de calidad",
                qualitySupervisorIds.size());
    }
}