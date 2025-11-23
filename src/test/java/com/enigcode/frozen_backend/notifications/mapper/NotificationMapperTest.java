package com.enigcode.frozen_backend.notifications.mapper;

import com.enigcode.frozen_backend.notifications.dto.NotificationResponseDTO;
import com.enigcode.frozen_backend.notifications.model.Notification;
import com.enigcode.frozen_backend.notifications.model.NotificationType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationMapperTest {

    private final NotificationMapper mapper = Mappers.getMapper(NotificationMapper.class);

    @Test
    void toResponseDTO_mapsFieldsCorrectly() {
        Notification n = Notification.builder()
                .id(123L)
                .userId(42L)
                .type(NotificationType.PRODUCTION_ORDER_PENDING)
                .message("Orden pendiente")
                .relatedEntityId(88L)
                .isRead(false)
                .createdAt(OffsetDateTime.now())
                .build();

        NotificationResponseDTO dto = mapper.toResponseDTO(n);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(123L);
        assertThat(dto.getUserId()).isEqualTo(42L);
        assertThat(dto.getType()).isEqualTo(NotificationType.PRODUCTION_ORDER_PENDING);
        assertThat(dto.getMessage()).isEqualTo("Orden pendiente");
        assertThat(dto.getRelatedEntityId()).isEqualTo(88L);
        assertThat(dto.getIsRead()).isEqualTo(false);
        assertThat(dto.getCreatedAt()).isNotNull();
    }
}
