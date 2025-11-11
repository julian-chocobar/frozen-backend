package com.enigcode.frozen_backend.common.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class FutureOrPresentDateValidatorTest {

    private FutureOrPresentDateValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new FutureOrPresentDateValidator();
    }

    @Test
    void isValid_nullValue_returnsTrue() {
        // Given
        OffsetDateTime nullDate = null;

        // When
        boolean result = validator.isValid(nullDate, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isValid_todayDate_returnsTrue() {
        // Given
        OffsetDateTime today = OffsetDateTime.now();

        // When
        boolean result = validator.isValid(today, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isValid_futureDate_returnsTrue() {
        // Given
        OffsetDateTime futureDate = OffsetDateTime.now().plusDays(5);

        // When
        boolean result = validator.isValid(futureDate, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isValid_pastDate_returnsFalse() {
        // Given
        OffsetDateTime pastDate = OffsetDateTime.now().minusDays(1);

        // When
        boolean result = validator.isValid(pastDate, context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isValid_yesterdayDate_returnsFalse() {
        // Given
        OffsetDateTime yesterday = OffsetDateTime.now().minusDays(1);

        // When
        boolean result = validator.isValid(yesterday, context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isValid_tomorrowDate_returnsTrue() {
        // Given
        OffsetDateTime tomorrow = OffsetDateTime.now().plusDays(1);

        // When
        boolean result = validator.isValid(tomorrow, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isValid_startOfToday_returnsTrue() {
        // Given: Inicio del día de hoy (00:00:00)
        OffsetDateTime startOfToday = OffsetDateTime.now()
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        // When
        boolean result = validator.isValid(startOfToday, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isValid_endOfToday_returnsTrue() {
        // Given: Fin del día de hoy (23:59:59)
        OffsetDateTime endOfToday = OffsetDateTime.now()
                .withHour(23)
                .withMinute(59)
                .withSecond(59);

        // When
        boolean result = validator.isValid(endOfToday, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isValid_differentTimezone_handlesCorrectly() {
        // Given: Fecha de hoy en zona horaria diferente (UTC)
        OffsetDateTime todayUTC = OffsetDateTime.now(ZoneOffset.UTC);

        // When
        boolean result = validator.isValid(todayUTC, context);

        // Then
        // Debería ser válida porque el validador convierte a la zona horaria del sistema
        assertThat(result).isTrue();
    }

    @Test
    void isValid_farPastDate_returnsFalse() {
        // Given
        OffsetDateTime farPast = OffsetDateTime.now().minusYears(1);

        // When
        boolean result = validator.isValid(farPast, context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isValid_farFutureDate_returnsTrue() {
        // Given
        OffsetDateTime farFuture = OffsetDateTime.now().plusYears(1);

        // When
        boolean result = validator.isValid(farFuture, context);

        // Then
        assertThat(result).isTrue();
    }
}
