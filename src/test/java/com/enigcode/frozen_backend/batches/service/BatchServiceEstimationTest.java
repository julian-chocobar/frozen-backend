package com.enigcode.frozen_backend.batches.service;

import com.enigcode.frozen_backend.common.Utils.DateUtil;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración para verificar que DateUtil calcula correctamente
 * las fechas de finalización de producción considerando días laborables
 * y fases activas/pasivas
 */
class BatchServiceEstimationTest {

    private Map<DayOfWeek, WorkingDay> workingDays;
    private Product product;

    @BeforeEach
    void setUp() {
        // Configurar días laborables: Lunes a Viernes 8:00-17:00 (9 horas diarias)
        workingDays = new HashMap<>();
        
        for (DayOfWeek day : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
                                      DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            WorkingDay wd = new WorkingDay();
            wd.setDayOfWeek(day);
            wd.setIsWorkingDay(true);
            wd.setOpeningHour(LocalTime.of(8, 0));
            wd.setClosingHour(LocalTime.of(17, 0));
            workingDays.put(day, wd);
        }
        
        for (DayOfWeek day : List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
            WorkingDay wd = new WorkingDay();
            wd.setDayOfWeek(day);
            wd.setIsWorkingDay(false);
            wd.setOpeningHour(LocalTime.of(0, 0));
            wd.setClosingHour(LocalTime.of(0, 0));
            workingDays.put(day, wd);
        }

        // Configurar producto
        product = new Product();
        product.setId(1L);
        product.setName("Cerveza Artesanal");
    }

    @Test
    void estimateEndDate_withActivePhases() {
        // Given: Producto con fases activas cortas
        List<ProductPhase> phases = new ArrayList<>();
        phases.add(createProductPhase(Phase.MOLIENDA, 2.0));
        phases.add(createProductPhase(Phase.ENVASADO, 3.0));
        product.setPhases(phases);

        OffsetDateTime plannedDate = OffsetDateTime.of(2025, 11, 3, 10, 0, 0, 0, ZoneOffset.UTC); // Lunes 10:00

        // When: Se calcula la fecha estimada usando DateUtil
        OffsetDateTime estimatedEndDate = DateUtil.estimateEndDate(product, plannedDate, workingDays);

        // Then: Debe terminar el mismo día
        // Lunes 10:00 (se ajusta a 08:00) + 2h MOLIENDA + 3h ENVASADO = 13:00
        assertThat(estimatedEndDate).isNotNull();
        assertThat(estimatedEndDate.getDayOfMonth()).isEqualTo(3); // Mismo día
        assertThat(estimatedEndDate.getHour()).isEqualTo(13);
    }

    @Test
    void estimateEndDate_withPassivePhases() {
        // Given: Producto con fases pasivas (fermentación)
        List<ProductPhase> phases = new ArrayList<>();
        phases.add(createProductPhase(Phase.MOLIENDA, 2.0));
        phases.add(createProductPhase(Phase.FERMENTACION, 48.0)); // 2 días pasivos
        phases.add(createProductPhase(Phase.ENVASADO, 2.0));
        product.setPhases(phases);

        OffsetDateTime plannedDate = OffsetDateTime.of(2025, 11, 3, 8, 0, 0, 0, ZoneOffset.UTC); // Lunes 8:00

        // When
        OffsetDateTime estimatedEndDate = DateUtil.estimateEndDate(product, plannedDate, workingDays);

        // Then: 
        // Lunes 8-10: MOLIENDA (2h activa) → termina 10:00
        // Lunes 10:00 + 48h → Miércoles 10:00 (pasiva, 24/7)
        // Miércoles 10-12: ENVASADO (2h activa) → termina 12:00
        assertThat(estimatedEndDate).isNotNull();
        assertThat(estimatedEndDate.getDayOfMonth()).isEqualTo(5); // Miércoles
        assertThat(estimatedEndDate.getHour()).isEqualTo(12);
    }

    @Test
    void estimateEndDate_crossingWeekend() {
        // Given: Producto cuyas fases activas cruzan fin de semana
        List<ProductPhase> phases = new ArrayList<>();
        phases.add(createProductPhase(Phase.MOLIENDA, 15.0)); // Requiere 2 días laborables
        product.setPhases(phases);

        OffsetDateTime plannedDate = OffsetDateTime.of(2025, 10, 31, 8, 0, 0, 0, ZoneOffset.UTC); // Viernes 8:00

        // When
        OffsetDateTime estimatedEndDate = DateUtil.estimateEndDate(product, plannedDate, workingDays);

        // Then: Viernes 9h + Lunes 6h = 15h (salta el fin de semana)
        assertThat(estimatedEndDate).isNotNull();
        assertThat(estimatedEndDate.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(estimatedEndDate.getHour()).isEqualTo(14); // Lunes 8:00 + 6h = 14:00
    }

    @Test
    void estimateEndDate_realBeerProduction() {
        // Given: Escenario realista de producción de cerveza
        List<ProductPhase> phases = new ArrayList<>();
        phases.add(createProductPhase(Phase.MOLIENDA, 2.0));
        phases.add(createProductPhase(Phase.MACERACION, 3.0));
        phases.add(createProductPhase(Phase.FILTRACION, 1.5));
        phases.add(createProductPhase(Phase.COCCION, 2.5));
        phases.add(createProductPhase(Phase.FERMENTACION, 168.0)); // 7 días
        phases.add(createProductPhase(Phase.MADURACION, 336.0));   // 14 días
        phases.add(createProductPhase(Phase.ENVASADO, 4.0));
        product.setPhases(phases);

        OffsetDateTime plannedDate = OffsetDateTime.of(2025, 11, 3, 8, 0, 0, 0, ZoneOffset.UTC); // Lunes 3 nov

        // When
        OffsetDateTime estimatedEndDate = DateUtil.estimateEndDate(product, plannedDate, workingDays);

        // Then: 
        // Día 1 (Lunes 3): 9h activas (sobran horas) → termina ~17:00
        // Lun 17:00 + 168h (7 días) → Lun 10 nov 17:00
        // Lun 17:00 + 336h (14 días) → Lun 24 nov 17:00
        // Lun 24 nov no hay horas, siguiente día laborable: Mar 25 nov 8-12: ENVASADO (4h)
        assertThat(estimatedEndDate).isNotNull();
        assertThat(estimatedEndDate.getMonthValue()).isEqualTo(11);
        assertThat(estimatedEndDate.getDayOfMonth()).isGreaterThan(20); // Más de 3 semanas después
    }

    @Test
    void estimateEndDate_startingWeekend() {
        // Given: Orden planificada para el fin de semana
        List<ProductPhase> phases = new ArrayList<>();
        phases.add(createProductPhase(Phase.MOLIENDA, 2.0));
        product.setPhases(phases);

        OffsetDateTime plannedDate = OffsetDateTime.of(2025, 11, 1, 10, 0, 0, 0, ZoneOffset.UTC); // Sábado 1 nov

        // When
        OffsetDateTime estimatedEndDate = DateUtil.estimateEndDate(product, plannedDate, workingDays);

        // Then: Debe moverse al lunes y completarse
        assertThat(estimatedEndDate).isNotNull();
        assertThat(estimatedEndDate.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(estimatedEndDate.getDayOfMonth()).isEqualTo(3);
        assertThat(estimatedEndDate.getHour()).isEqualTo(10); // 8:00 + 2h = 10:00
    }

    @Test
    void estimateEndDate_withDecimalHours() {
        // Given: Fases con horas decimales
        List<ProductPhase> phases = new ArrayList<>();
        phases.add(createProductPhase(Phase.MOLIENDA, 1.5));  // 1h 30min
        phases.add(createProductPhase(Phase.ENVASADO, 2.25)); // 2h 15min
        product.setPhases(phases);

        OffsetDateTime plannedDate = OffsetDateTime.of(2025, 11, 3, 10, 0, 0, 0, ZoneOffset.UTC); // Lunes 10:00

        // When
        OffsetDateTime estimatedEndDate = DateUtil.estimateEndDate(product, plannedDate, workingDays);

        // Then: 8:00 + 1.5h + 2.25h = 11:45
        assertThat(estimatedEndDate).isNotNull();
        assertThat(estimatedEndDate.getDayOfMonth()).isEqualTo(3);
        assertThat(estimatedEndDate.getHour()).isEqualTo(11);
        assertThat(estimatedEndDate.getMinute()).isEqualTo(45);
    }

    @Test
    void estimateEndDate_multipleWeeks() {
        // Given: Producción que toma varias semanas
        List<ProductPhase> phases = new ArrayList<>();
        phases.add(createProductPhase(Phase.MOLIENDA, 50.0)); // ~6 días laborables
        product.setPhases(phases);

        OffsetDateTime plannedDate = OffsetDateTime.of(2025, 11, 3, 8, 0, 0, 0, ZoneOffset.UTC); // Lunes 3 nov

        // When
        OffsetDateTime estimatedEndDate = DateUtil.estimateEndDate(product, plannedDate, workingDays);

        // Then: 50 horas / 9 horas por día = 5.55 días
        // Lun 3: 9h → 41h restantes
        // Mar 4: 9h → 32h restantes
        // Mie 5: 9h → 23h restantes
        // Jue 6: 9h → 14h restantes
        // Vie 7: 9h → 5h restantes
        // Lun 10: 5h → termina 13:00
        assertThat(estimatedEndDate).isNotNull();
        assertThat(estimatedEndDate.getDayOfMonth()).isEqualTo(10); // Lunes siguiente
        assertThat(estimatedEndDate.getHour()).isEqualTo(13);
    }

    // Helper method
    private ProductPhase createProductPhase(Phase phase, Double estimatedHours) {
        ProductPhase productPhase = new ProductPhase();
        productPhase.setPhase(phase);
        productPhase.setEstimatedHours(estimatedHours);
        return productPhase;
    }
}
