package com.enigcode.frozen_backend.common.mapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoubleRoundingUtilTest {

    @Test
    void round2_handlesNullAndRounding() {
        assertNull(DoubleRoundingUtil.round2(null));

        Double a = DoubleRoundingUtil.round2(1.234);
        assertEquals(1.23, a);

        Double b = DoubleRoundingUtil.round2(1.235);
        assertEquals(1.24, b);

        Double c = DoubleRoundingUtil.round2(2.0);
        assertEquals(2.00, c);
    }
}
