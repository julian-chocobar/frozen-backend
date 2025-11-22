package com.enigcode.frozen_backend.sectors.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SectorModelTest {

    @Test
    void prePersist_setsActualProductionToZeroWhenNull() {
        Sector s = new Sector();
        s.setActualProduction(null);

        // emulate JPA @PrePersist call
        s.setActualProduction();

        assertThat(s.getActualProduction()).isEqualTo(0.0);
    }

    @Test
    void increaseAndDecreaseActualProduction_behaveCorrectly() {
        Sector s = new Sector();
        s.setActualProduction(5.0);

        s.increaseActualProduction(2.0);
        assertThat(s.getActualProduction()).isEqualTo(7.0);

        s.decreaseActualProduction(3.0);
        assertThat(s.getActualProduction()).isEqualTo(4.0);
    }

    @Test
    void decreaseActualProduction_neverGoesBelowZero() {
        Sector s = new Sector();
        s.setActualProduction(1.0);

        s.decreaseActualProduction(5.0);
        assertThat(s.getActualProduction()).isGreaterThanOrEqualTo(0.0);
    }
}
