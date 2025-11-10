package com.enigcode.frozen_backend.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class FutureOrPresentDateValidator implements ConstraintValidator<FutureOrPresentDate, OffsetDateTime> {

    private Clock clock = Clock.systemDefaultZone();

    @Override
    public boolean isValid(OffsetDateTime value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        ZoneId systemZone = clock.getZone();
        LocalDate today = LocalDate.now(clock);
        LocalDate valueDate = value.atZoneSameInstant(systemZone).toLocalDate();

        return !valueDate.isBefore(today);
    }
}
