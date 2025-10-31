package com.enigcode.frozen_backend.common.Utils;

import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import com.enigcode.frozen_backend.products.model.Product;
import com.enigcode.frozen_backend.system_configurations.model.WorkingDay;
import com.enigcode.frozen_backend.system_configurations.service.SystemConfigurationService;
import lombok.RequiredArgsConstructor;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class DateUtil {

    /**
     * Funcion que estima la fecha de finalizacion de un producto teniendo en cuenta
     * el trabajo en la pyme
     * 
     * @param product
     * @return
     */
    public static OffsetDateTime estimateEndDate(Product product, OffsetDateTime startDate,
            Map<DayOfWeek, WorkingDay> workingDays) {
        List<ProductPhase> productPhases = product.getPhases();
        DayOfWeek startDay = startDate.getDayOfWeek();

        WorkingDay startWorkingDay = workingDays.get(startDay);

        OffsetDateTime currentDate = startDate.withHour(startWorkingDay.getOpeningHour().getHour())
                .withMinute(startWorkingDay.getOpeningHour().getMinute())
                .withSecond(0).withNano(0);

        for (ProductPhase phase : productPhases) {
            Double phaseHours = phase.getEstimatedHours();
            Boolean isActive = phase.getPhase().getIsTimeActive();
            currentDate = estimateEndPhaseDate(workingDays, currentDate, phaseHours, isActive);
        }

        return currentDate;
    }

    private static OffsetDateTime estimateEndPhaseDate(Map<DayOfWeek, WorkingDay> workingDays, OffsetDateTime startDate,
            Double phaseHours, Boolean isActive) {
        if (isActive) {
            WorkingDay startDay = workingDays.get(startDate.getDayOfWeek());
            startDate = alignStartWithWorkingHours(startDate, startDay, workingDays);
            Double remainingHours = phaseHours;
            while (remainingHours > 0) {
                WorkingDay currentDay = workingDays.get(startDate.getDayOfWeek());
                double hoursOfWork = hoursWorkedUntilClose(startDate, currentDay, remainingHours);
                startDate = addHours(startDate, hoursOfWork);
                remainingHours -= hoursOfWork;
                if (remainingHours > 0) {
                    // 1. Mueve al próximo día hábil
                    startDate = nextWorkingDay(startDate, workingDays);

                    // 2. Establece la hora a la hora de apertura del nuevo día (la alineación
                    // manual)
                    WorkingDay nextWorking = workingDays.get(startDate.getDayOfWeek());
                    startDate = startDate
                            .withHour(nextWorking.getOpeningHour().getHour())
                            .withMinute(nextWorking.getOpeningHour().getMinute())
                            .withSecond(0)
                            .withNano(0);
                }
            }

            return startDate;
        }
        return addHours(startDate, phaseHours);
    }

    private static Double hoursWorkedUntilClose(OffsetDateTime startDate, WorkingDay currentDay, Double phaseHours) {
        // 1. Obtener la hora de cierre del día
        LocalTime closeTime = currentDay.getClosingHour();

        // 2. Crear un OffsetDateTime con la fecha de inicio, pero a la hora de cierre.
        OffsetDateTime closeDateTime = startDate
                .withHour(closeTime.getHour())
                .withMinute(closeTime.getMinute())
                .withSecond(0)
                .withNano(0);

        // 3. Calcular la duración (diferencia) entre la hora de inicio y la hora de
        // cierre.
        // Usamos Duration.between, que devuelve un valor positivo si closeDateTime es
        // posterior a startDate.
        Duration timeUntilClose = Duration.between(startDate, closeDateTime);

        // 4. Convertir la duración en horas con decimales (Double)
        double hoursUntilClose = timeUntilClose.toSeconds() / 3600.0; // 3600 segundos en una hora

        // 5. Determinar las horas reales que se aplicarán en este día/ciclo
        // Solo podemos aplicar el tiempo que queda en el turno O las horas restantes de
        // la fase,
        // el que sea menor.
        return Math.min(hoursUntilClose, phaseHours);
    }

    private static OffsetDateTime addHours(OffsetDateTime dateTime, double hoursToAdd) {
        long hours = (long) hoursToAdd;
        long minutes = (long) ((hoursToAdd - hours) * 60);
        return dateTime.plusHours(hours).plusMinutes(minutes);
    }

    private static OffsetDateTime alignStartWithWorkingHours(OffsetDateTime startDate,
            WorkingDay workingDay,
            Map<DayOfWeek, WorkingDay> workingDays) {
        LocalTime startTime = startDate.toLocalTime();
        LocalTime open = workingDay.getOpeningHour();
        LocalTime close = workingDay.getClosingHour();

        if (startTime.isBefore(open)) {
            return startDate.withHour(open.getHour())
                    .withMinute(open.getMinute())
                    .withSecond(0)
                    .withNano(0);
        } else if (startTime.isAfter(close) || startTime.equals(close)) {
            OffsetDateTime nextDay = nextWorkingDay(startDate, workingDays);
            WorkingDay nextWorking = workingDays.get(nextDay.getDayOfWeek());
            return nextDay.withHour(nextWorking.getOpeningHour().getHour())
                    .withMinute(nextWorking.getOpeningHour().getMinute())
                    .withSecond(0)
                    .withNano(0);

        } else {
            return startDate;
        }
    }

    private static OffsetDateTime nextWorkingDay(OffsetDateTime startDate, Map<DayOfWeek, WorkingDay> workingDays) {
        OffsetDateTime nextDay = startDate.plusDays(1);
        WorkingDay workingDay = workingDays.get(nextDay.getDayOfWeek());
        while (!workingDay.getIsWorkingDay()) {
            nextDay = nextDay.plusDays(1);
            workingDay = workingDays.get(nextDay.getDayOfWeek());
        }
        return nextDay;
    }

}
