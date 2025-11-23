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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationMapper notificationMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SseNotificationService sseNotificationService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification savedNotification;
    private NotificationResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        savedNotification = Notification.builder()
                .id(10L)
                .userId(7L)
                .type(NotificationType.PRODUCTION_ORDER_PENDING)
                .message("Test")
                .relatedEntityId(123L)
                .isRead(false)
                .createdAt(OffsetDateTime.now())
                .build();

        responseDTO = new NotificationResponseDTO();
        responseDTO.setId(10L);
        Mockito.lenient().when(userRepository.existsById(anyLong())).thenReturn(true);
    }

    @Test
    void createNotification_success_sendsSseAndReturnsDto() {
        when(userRepository.existsById(7L)).thenReturn(true);
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        when(notificationMapper.toResponseDTO(savedNotification)).thenReturn(responseDTO);

        NotificationResponseDTO result = notificationService.createNotification(7L, NotificationType.LOW_STOCK_ALERT,
                "Low stock", 50L);

        assertThat(result).isNotNull();
        verify(notificationRepository).save(any(Notification.class));
        verify(sseNotificationService).sendNotificationToUser(eq(7L), any());
        verify(sseNotificationService).sendStatsUpdate(eq(7L), any());
    }

    @Test
    void createNotification_userNotFound_throws() {
        when(userRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class,
                () -> notificationService.createNotification(99L, NotificationType.LOW_STOCK_ALERT, "m", 1L));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAsRead_happyPath_marksAndSendsStats() {
        Notification n = Notification.builder().id(5L).userId(7L).isRead(false).build();
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(Notification.class))).thenReturn(n);
        when(notificationMapper.toResponseDTO(n)).thenReturn(responseDTO);

        NotificationResponseDTO dto = notificationService.markAsRead(5L, 7L);

        assertThat(dto).isNotNull();
        verify(notificationRepository).save(any(Notification.class));
        verify(sseNotificationService).sendStatsUpdate(eq(7L), any());
    }

    @Test
    void markAsRead_wrongUser_throws() {
        Notification n = Notification.builder().id(5L).userId(8L).isRead(false).build();
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(n));

        assertThrows(BadRequestException.class, () -> notificationService.markAsRead(5L, 7L));
    }

    @Test
    void markAllAsRead_savesAll() {
        Notification a = Notification.builder().id(1L).userId(7L).isRead(false).build();
        Notification b = Notification.builder().id(2L).userId(7L).isRead(false).build();
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(eq(7L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(a, b)));

        notificationService.markAllAsRead(7L);

        verify(notificationRepository).saveAll(any());
    }

    @Test
    void getUserNotificationStats_returnsCounts() {
        when(notificationRepository.countByUserIdAndIsReadFalse(7L)).thenReturn(2L);
        when(notificationRepository.countByUserId(7L)).thenReturn(5L);

        NotificationStatsDTO stats = notificationService.getUserNotificationStats(7L);

        assertThat(stats.getUnreadCount()).isEqualTo(2L);
        assertThat(stats.getTotalCount()).isEqualTo(5L);
    }

    @Test
    void createProductionOrderNotification_createsForManagers_whenNotExisting() {
        Long orderId = 88L;
        when(userRepository.findUserIdsByRole(Role.GERENTE_DE_PLANTA)).thenReturn(List.of(11L, 12L));
        when(notificationRepository.findByUserIdAndTypeAndRelatedEntityId(anyLong(), any(), eq(orderId)))
                .thenReturn(List.of());
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        when(notificationMapper.toResponseDTO(any())).thenReturn(responseDTO);

        notificationService.createProductionOrderNotification(orderId, "ProductoX");

        // Expect createNotification invoked twice -> repository.save twice
        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
    }

    @Test
    void getUserNotifications_returnsPage() {
        Notification n = Notification.builder().id(2L).userId(7L).isRead(false).build();
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(7L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(n)));
        when(notificationMapper.toResponseDTO(any(Notification.class))).thenReturn(responseDTO);

        Page<NotificationResponseDTO> page = notificationService.getUserNotifications(7L, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(eq(7L), any(Pageable.class));
    }

    @Test
    void getUserUnreadNotifications_returnsPage() {
        Notification n = Notification.builder().id(3L).userId(7L).isRead(false).build();
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(eq(7L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(n)));
        when(notificationMapper.toResponseDTO(any(Notification.class))).thenReturn(responseDTO);

        Page<NotificationResponseDTO> page = notificationService.getUserUnreadNotifications(7L, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        verify(notificationRepository).findByUserIdAndIsReadFalseOrderByCreatedAtDesc(eq(7L), any(Pageable.class));
    }

    @Test
    void createLowStockNotification_noSupervisors_noSave() {
        when(userRepository.findUserIdsByRole(any())).thenReturn(List.of());

        notificationService.createLowStockNotification(55L, "MaterialX", 5.0, 10.0);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createLowStockNotification_createsForSupervisors_whenNotRecent() {
        when(userRepository.findUserIdsByRole(any())).thenReturn(List.of(11L, 12L));
        when(notificationRepository.findRecentLowStockNotifications(anyLong(), anyLong(), any())).thenReturn(List.of());
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        when(notificationMapper.toResponseDTO(any())).thenReturn(responseDTO);

        notificationService.createLowStockNotification(55L, "MaterialX", 5.0, 10.0);

        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
    }

    @Test
    void createPendingMovementNotification_noOperators_noSave() {
        when(userRepository.findUserIdsByRole(any())).thenReturn(List.of());

        notificationService.createPendingMovementNotification(99L, "Mat", "EGRESO");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createPendingMovementNotification_createsForOperators_whenNotExisting() {
        when(userRepository.findUserIdsByRole(any())).thenReturn(List.of(21L));
        when(notificationRepository.findByUserIdAndTypeAndRelatedEntityId(anyLong(), any(), anyLong()))
                .thenReturn(List.of());
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        when(notificationMapper.toResponseDTO(any())).thenReturn(responseDTO);

        notificationService.createPendingMovementNotification(99L, "Mat", "EGRESO");

        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
    }

    @Test
    void cleanupOldNotifications_deletesWhenOldExist() {
        Notification old = Notification.builder().id(50L).userId(7L).isRead(false).createdAt(OffsetDateTime.now().minusDays(40)).build();
        when(notificationRepository.findOldNotifications(any())).thenReturn(List.of(old));

        notificationService.cleanupOldNotifications();

        verify(notificationRepository).deleteAll(any());
    }
}
