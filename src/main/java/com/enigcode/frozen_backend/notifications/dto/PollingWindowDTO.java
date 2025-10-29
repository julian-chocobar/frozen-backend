package com.enigcode.frozen_backend.notifications.dto;

import lombok.*;

import java.time.LocalTime;
import java.util.List;

/**
 * DTO para configurar ventanas de polling
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PollingWindowDTO {

    /**
     * Nombre de la ventana (ej: "morning", "afternoon")
     */
    private String name;

    /**
     * Hora de inicio de la ventana (formato HH:mm)
     */
    private LocalTime startTime;

    /**
     * Hora de fin de la ventana (formato HH:mm)
     */
    private LocalTime endTime;

    /**
     * Intervalo de polling en minutos durante la ventana activa
     */
    private Integer pollingIntervalMinutes;

    /**
     * Si la ventana está activa actualmente
     */
    private Boolean isActive;

    /**
     * DTO estático para las ventanas configuradas
     */
    public static List<PollingWindowDTO> getDefaultWindows() {
        return List.of(
                PollingWindowDTO.builder()
                        .name("morning")
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(10, 0))
                        .pollingIntervalMinutes(10)
                        .isActive(false)
                        .build(),
                PollingWindowDTO.builder()
                        .name("afternoon")
                        .startTime(LocalTime.of(17, 0))
                        .endTime(LocalTime.of(18, 0))
                        .pollingIntervalMinutes(10)
                        .isActive(false)
                        .build());
    }
}