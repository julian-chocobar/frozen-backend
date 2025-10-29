package com.enigcode.frozen_backend.notifications.mapper;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.notifications.dto.NotificationResponseDTO;
import com.enigcode.frozen_backend.notifications.model.Notification;
import org.mapstruct.*;

/**
 * Mapper para convertir entre entidades Notification y sus DTOs
 */
@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface NotificationMapper {

    /**
     * Convierte una entidad Notification a NotificationResponseDTO
     */
    NotificationResponseDTO toResponseDTO(Notification notification);
}