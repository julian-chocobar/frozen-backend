package com.enigcode.frozen_backend.common.mapper;

import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DoubleRoundingUtil {

    @Named("round2")
    public static Double round2(Double value) {
        if (value == null) return null;
        return BigDecimal
                .valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
