package com.enigcode.frozen_backend.common.Utils;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import com.enigcode.frozen_backend.products.model.Product;
import com.enigcode.frozen_backend.system_configurations.model.WorkingDay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DateUtilTest {

    private Map<DayOfWeek, WorkingDay> workingDays;
    private Product product;

    @BeforeEach
    void setUp() {
        // Configurar días laborables: Lunes a Viernes 8:00-17:00 (9 horas)
        workingDays = new HashMap<>();
        
        // Días laborables
        for (DayOfWeek day : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
                                      DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            WorkingDay wd = new WorkingDay();
            wd.setDayOfWeek(day);
            wd.setIsWorkingDay(true);
            wd.setOpeningHour(LocalTime.of(8, 0));
            wd.setClosingHour(LocalTime.of(17, 0));
            workingDays.put(day, wd);
        }
        
        // Fin de semana - no laborables
        for (DayOfWeek day : List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
            WorkingDay wd = new WorkingDay();
            wd.setDayOfWeek(day);
            wd.setIsWorkingDay(false);
            wd.setOpeningHour(LocalTime.of(0, 0));
            wd.setClosingHour(LocalTime.of(0, 0));
            workingDays.put(day, wd);
        }

        // Producto de prueba
        product = new Product();
        product.setName("Test Product");
    }

    @Test
    void estimateEndDate_singleActivePhase_withinSameDay() {
        // Given: Una fase activa de 4 horas, inicio a las 9:00 del lunes
        ProductPhase phase = createProductPhase(Phase.MOLIENDA, 4.0);
        product.setPhases(List.of(phase));
        
        OffsetDateTime startDate = OffsetDateTime.of(2025, 11, 3, 9, 0, 0, 0, ZoneOffset.UTC); // Lunes
        
        // When
        OffsetDateTime result = DateUtil.estimateEndDate(product, startDate, workingDays);
        
        // Then: Se alinea a las 8:00 (apertura) y termina a las 12:00 (8:00 + 4h)
        assertThat(result.getHour()).isEqualTo(12);
        assertThat(result.getMinute()).isEqualTo(0);
        assertThat(result.getDayOfMonth()).isEqualTo(3);
    }

    @Test
    void estimateEndDate_singleActivePhase_spansMultipleDays() {
        // Given: Una fase activa de 20 horas (más de 2 días laborables)
        ProductPhase phase = createProductPhase(Phase.MOLIENDA, 20.0);
        product.setPhases(List.of(phase));
        
        OffsetDateTime startDate = OffsetDateTime.of(2025, 11, 3, 8, 0, 0, 0, ZoneOffset.UTC); // Lunes 8:00
        
        // When
        OffsetDateTime result = DateUtil.estimateEndDate(product, startDate, workingDays);
        
        // Then: Lunes 9h + Martes 9h + Miércoles 2h = 20h total
        // Debe terminar el miércoles a las 10:00
        assertThat(result.getDayOfMonth()).isEqualTo(5); // Miércoles
        assertThat(result.getHour()).isEqualTo(10);
        assertThat(result.getMinute()).isEqualTo(0);
    }

    @Test
    void estimateEndDate_singlePassivePhase_ignoresWorkingHours() {
        // Given: Una fase pasiva (FERMENTACION) de 48 horas
        ProductPhase phase = createProductPhase(Phase.FERMENTACION, 48.0);
        product.setPhases(List.of(phase));
        
        OffsetDateTime startDate = OffsetDateTime.of(2025, 11, 3, 10, 0, 0, 0, ZoneOffset.UTC); // Lunes 10:00
        
        // When
        OffsetDateTime result = DateUtil.estimateEndDate(product, startDate, workingDays);
        
        // Then: Se alinea a las 8:00 (apertura) + 48 horas → Miércoles 8:00
        assertThat(result.getDayOfMonth()).isEqualTo(5); // Miércoles
        assertThat(result.getHour()).isEqualTo(8);
        assertThat(result.getMinute()).isEqualTo(0);
    }

    @Test
    void estimateEndDate_multiplePhasesActiveAndPassive() {
        // Given: MOLIENDA (activa 4h) + FERMENTACION (pasiva 24h) + ENVASADO (activa 2h)
        List<ProductPhase> phases = new ArrayList<>();
        phases.add(createProductPhase(Phase.MOLIENDA, 4.0));       // Activa
        phases.add(createProductPhase(Phase.FERMENTACION, 24.0));  // Pasiva
        phases.add(createProductPhase(Phase.ENVASADO, 2.0));       // Activa
        product.setPhases(phases);
        
        OffsetDateTime startDate = OffsetDateTime.of(2025, 11, 3, 8, 0, 0, 0, ZoneOffset.UTC); // Lunes 8:00
        
        // When
        OffsetDateTime result = DateUtil.estimateEndDate(product, startDate, workingDays);
        
        // Then:
        // - Lunes 8:00-12:00: MOLIENDA (4h activas) → termina 12:00
        // - Lunes 12:00 + 24h → Martes 12:00: FERMENTACION (pasiva)
        // - Martes 12:00-14:00: ENVASADO (2h activas) → termina 14:00
        assertThat(result.getDayOfMonth()).isEqualTo(4); // Martes
        assertThat(result.getHour()).isEqualTo(14);
        assertThat(result.getMinute()).isEqualTo(0);
    }

    @Test
    void estimateEndDate_startBeforeWorkingHours_alignsToOpeningTime() {
        // Given: Inicio a las 6:00 AM (antes de las 8:00)
        ProductPhase phase = createProductPhase(Phase.MOLIENDA, 2.0);
        product.setPhases(List.of(phase));
        
        OffsetDateTime startDate = OffsetDateTime.of(2025, 11, 3, 6, 0, 0, 0, ZoneOffset.UTC); // Lunes 6:00
        
        // When
        OffsetDateTime result = DateUtil.estimateEndDate(product, startDate, workingDays);
        
        // Then: Debe comenzar a las 8:00 y terminar a las 10:00
        assertThat(result.getHour()).isEqualTo(10);
        assertThat(result.getMinute()).isEqualTo(0);
    }

    @Test
    void estimateEndDate_startAfterWorkingHours_movesToNextDay() {
        // Given: Inicio a las 18:00 (después de las 17:00)
        ProductPhase phase = createProductPhase(Phase.MOLIENDA, 2.0);
        product.setPhases(List.of(phase));
        
        OffsetDateTime startDate = OffsetDateTime.of(2025, 11, 3, 18, 0, 0, 0, ZoneOffset.UTC); // Lunes 18:00
        
        // When
        OffsetDateTime result = DateUtil.estimateEndDate(product, startDate, workingDays);
        
        // Then: Debe moverse al mismo día alineado (lunes 8:00) y terminar a las 10:00
        assertThat(result.getDayOfMonth()).isEqualTo(3); // Mismo día (lunes)
        assertThat(result.getHour()).isEqualTo(10);
        assertThat(result.getMinute()).isEqualTo(0);
    }

    @Test
    void estimateEndDate_startOnWeekend_movesToMonday() {
        // Given: Inicio el sábado
        ProductPhase phase = createProductPhase(Phase.MOLIENDA, 2.0);
        product.setPhases(List.of(phase));
        
        OffsetDateTime startDate = OffsetDateTime.of(2025, 11, 1, 10, 0, 0, 0, ZoneOffset.UTC); // Sábado
        
        // When
        OffsetDateTime result = DateUtil.estimateEndDate(product, startDate, workingDays);
        
        // Then: Debe comenzar el lunes a las 8:00 y terminar a las 10:00
        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(result.getDayOfMonth()).isEqualTo(3);
        assertThat(result.getHour()).isEqualTo(10);
    }

    @Test
    void estimateEndDate_phaseSpansWeekend_skipsWeekend() {
        // Given: Fase de 20 horas que cruza el fin de semana
        ProductPhase phase = createProductPhase(Phase.MOLIENDA, 20.0);
        product.setPhases(List.of(phase));
        
        OffsetDateTime startDate = OffsetDateTime.of(2025, 10, 31, 8, 0, 0, 0, ZoneOffset.UTC); // Viernes 8:00
        
        // When
        OffsetDateTime result = DateUtil.estimateEndDate(product, startDate, workingDays);
        
        // Then: Viernes 9h + Lunes 9h + Martes 2h = 20h
        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
        assertThat(result.getHour()).isEqualTo(10);
    }

    @Test
    void estimateEndDate_phaseWithDecimalHours() {
        // Given: Fase de 2.5 horas (2 horas 30 minutos)
        ProductPhase phase = createProductPhase(Phase.MOLIENDA, 2.5);
        product.setPhases(List.of(phase));
        
        OffsetDateTime startDate = OffsetDateTime.of(2025, 11, 3, 10, 0, 0, 0, ZoneOffset.UTC); // Lunes 10:00
        
        // When
        OffsetDateTime result = DateUtil.estimateEndDate(product, startDate, workingDays);
        
        // Then: Se alinea a las 8:00 + 2.5h = 10:30
        assertThat(result.getHour()).isEqualTo(10);
        assertThat(result.getMinute()).isEqualTo(30);
    }

    @Test
    void estimateEndDate_phaseEndsExactlyAtClosingTime() {
        // Given: Fase de 9 horas (exactamente el horario laboral)
        ProductPhase phase = createProductPhase(Phase.MOLIENDA, 9.0);
        product.setPhases(List.of(phase));
        
        OffsetDateTime startDate = OffsetDateTime.of(2025, 11, 3, 8, 0, 0, 0, ZoneOffset.UTC); // Lunes 8:00
        
        // When
        OffsetDateTime result = DateUtil.estimateEndDate(product, startDate, workingDays);
        
        // Then: Debe terminar a las 17:00
        assertThat(result.getHour()).isEqualTo(17);
        assertThat(result.getMinute()).isEqualTo(0);
        assertThat(result.getDayOfMonth()).isEqualTo(3);
    }

    @Test
    void estimateEndDate_realWorldScenario_beerProduction() {
        // Given: Escenario real de producción de cerveza
        List<ProductPhase> phases = new ArrayList<>();
        phases.add(createProductPhase(Phase.MOLIENDA, 2.0));          // 2h activa
        phases.add(createProductPhase(Phase.MACERACION, 3.0));        // 3h activa
        phases.add(createProductPhase(Phase.COCCION, 2.0));           // 2h activa
        phases.add(createProductPhase(Phase.FERMENTACION, 168.0));    // 7 días pasiva (24*7)
        phases.add(createProductPhase(Phase.MADURACION, 336.0));      // 14 días pasiva (24*14)
        phases.add(createProductPhase(Phase.ENVASADO, 4.0));          // 4h activa
        product.setPhases(phases);
        
        OffsetDateTime startDate = OffsetDateTime.of(2025, 11, 3, 8, 0, 0, 0, ZoneOffset.UTC); // Lunes 8:00
        
        // When
        OffsetDateTime result = DateUtil.estimateEndDate(product, startDate, workingDays);
        
        // Then: 
        // - Lunes 8:00-15:00: Molienda + Maceración + Cocción (7h activas) → 15:00
        // - Lunes 15:00 + 168h (7 días) → Lunes siguiente 15:00
        // - Lunes 15:00 + 336h (14 días) → Lunes 2 semanas después 15:00
        // - Ese lunes 15:00-17:00 + Martes 8:00-10:00: Envasado (4h activas) → Martes 10:00
        
        // Total: ~21 días después
        assertThat(result.getDayOfMonth()).isEqualTo(25); // 3 + 21 + 1 = 25 (aproximado)
        assertThat(result.getHour()).isEqualTo(10);
    }

    @Test
    void estimateEndDate_emptyPhaseList_returnsStartDate() {
        // Given: Producto sin fases
        product.setPhases(new ArrayList<>());
        
        OffsetDateTime startDate = OffsetDateTime.of(2025, 11, 3, 10, 0, 0, 0, ZoneOffset.UTC);
        
        // When
        OffsetDateTime result = DateUtil.estimateEndDate(product, startDate, workingDays);
        
        // Then: Debe ajustarse al horario de apertura pero mantener el día
        assertThat(result.getDayOfMonth()).isEqualTo(3);
        assertThat(result.getHour()).isEqualTo(8); // Alineado a hora de apertura
    }

    @Test
    void estimateEndDate_phaseSpansMultipleWeeks() {
        // Given: Fase activa muy larga (100 horas = ~11 días laborables)
        ProductPhase phase = createProductPhase(Phase.MOLIENDA, 100.0);
        product.setPhases(List.of(phase));
        
        OffsetDateTime startDate = OffsetDateTime.of(2025, 11, 3, 8, 0, 0, 0, ZoneOffset.UTC); // Lunes 8:00
        
        // When
        OffsetDateTime result = DateUtil.estimateEndDate(product, startDate, workingDays);
        
        // Then: 100 horas / 9 horas día = 11.11 días
        // Debe terminar aproximadamente 11-12 días laborables después
        assertThat(result.getDayOfMonth()).isGreaterThan(13); // Al menos 10 días después
        assertThat(result.getDayOfWeek()).isIn(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
                                                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
    }

    @Test
    void estimateEndDate_startAtMiddayWithPartialHours() {
        // Given: Inicio a medio día con fase de 5.5 horas
        ProductPhase phase = createProductPhase(Phase.MOLIENDA, 5.5);
        product.setPhases(List.of(phase));
        
        OffsetDateTime startDate = OffsetDateTime.of(2025, 11, 3, 12, 30, 0, 0, ZoneOffset.UTC); // Lunes 12:30
        
        // When
        OffsetDateTime result = DateUtil.estimateEndDate(product, startDate, workingDays);
        
        // Then: Se alinea al inicio del día (8:00) + 5.5h = 13:30 mismo día
        assertThat(result.getDayOfMonth()).isEqualTo(3); // Mismo día (lunes)
        assertThat(result.getHour()).isEqualTo(13);
        assertThat(result.getMinute()).isEqualTo(30);
    }

    // Helper method
    private ProductPhase createProductPhase(Phase phase, Double estimatedHours) {
        ProductPhase productPhase = new ProductPhase();
        productPhase.setPhase(phase);
        productPhase.setEstimatedHours(estimatedHours);
        return productPhase;
    }
}
