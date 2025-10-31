package com.enigcode.frozen_backend.notifications.repository;

import com.enigcode.frozen_backend.notifications.model.Notification;
import com.enigcode.frozen_backend.notifications.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Repositorio para manejar operaciones de base de datos de notificaciones
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

        /**
         * Obtiene todas las notificaciones de un usuario específico, ordenadas por
         * fecha de creación desc
         */
        Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

        /**
         * Obtiene solo las notificaciones no leídas de un usuario específico
         */
        Page<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

        /**
         * Cuenta las notificaciones no leídas de un usuario
         */
        Long countByUserIdAndIsReadFalse(Long userId);

        /**
         * Cuenta todas las notificaciones de un usuario
         */
        Long countByUserId(Long userId);

        /**
         * Encuentra notificaciones por usuario y tipo, útil para evitar duplicados
         */
        List<Notification> findByUserIdAndTypeAndRelatedEntityId(Long userId, NotificationType type,
                        Long relatedEntityId);

        /**
         * Obtiene todas las notificaciones no leídas de usuarios con rol
         * GERENTE_DE_PLANTA
         * Para uso en el servicio de recordatorios
         */
        @Query("""
                        SELECT n FROM Notification n
                        JOIN User u ON n.userId = u.id
                        JOIN u.roles r
                        WHERE r = com.enigcode.frozen_backend.users.model.Role.GERENTE_DE_PLANTA
                        AND n.isRead = false
                        AND n.createdAt <= :maxDate
                        ORDER BY n.createdAt ASC
                        """)
        List<Notification> findUnreadNotificationsForPlantManagers(@Param("maxDate") OffsetDateTime maxDate);

        /**
         * Busca usuarios con rol GERENTE_DE_PLANTA que tienen notificaciones pendientes
         * Para crear recordatorios personalizados
         */
        @Query("""
                        SELECT DISTINCT n.userId FROM Notification n
                        JOIN User u ON n.userId = u.id
                        JOIN u.roles r
                        WHERE r = com.enigcode.frozen_backend.users.model.Role.GERENTE_DE_PLANTA
                        AND n.isRead = false
                        AND n.type = :notificationType
                        """)
        List<Long> findUserIdsWithUnreadNotifications(@Param("notificationType") NotificationType notificationType);

        /**
         * Busca notificaciones recientes de stock bajo para evitar spam
         */
        @Query("""
                        SELECT n FROM Notification n
                        WHERE n.userId = :userId
                        AND n.type = 'LOW_STOCK_ALERT'
                        AND n.relatedEntityId = :materialId
                        AND n.createdAt >= :since
                        """)
        List<Notification> findRecentLowStockNotifications(@Param("userId") Long userId,
                        @Param("materialId") Long materialId,
                        @Param("since") OffsetDateTime since);

        /**
         * Busca notificaciones antiguas para limpieza automática
         */
        @Query("""
                        SELECT n FROM Notification n
                        WHERE n.createdAt < :cutoffDate
                        """)
        List<Notification> findOldNotifications(@Param("cutoffDate") OffsetDateTime cutoffDate);
}