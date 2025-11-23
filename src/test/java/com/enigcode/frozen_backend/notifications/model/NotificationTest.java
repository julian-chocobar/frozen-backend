package com.enigcode.frozen_backend.notifications.model;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    @Test
    void markAsRead_setsFlagAndReadAt() {
        Notification n = Notification.builder()
            .id(1L)
            .userId(2L)
            .type(NotificationType.LOW_STOCK_ALERT)
            .message("m")
            .isRead(false)
            // ensure createdAt is slightly in the past so readAt will be after
            .createdAt(OffsetDateTime.now().minusSeconds(1))
            .build();

        assertThat(n.getIsRead()).isFalse();
        assertThat(n.getReadAt()).isNull();

        n.markAsRead();

        assertThat(n.getIsRead()).isTrue();
        assertThat(n.getReadAt()).isNotNull();
        assertThat(n.getReadAt()).isAfter(n.getCreatedAt());
    }
}
