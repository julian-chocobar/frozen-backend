package com.enigcode.frozen_backend.materials.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaterialModelTest {

    @Test
    void reserveAndReturnStock_behaviour() {
        Material m = new Material();
        m.setStock(10.0);
        m.setReservedStock(0.0);

        m.reserveStock(3.0);
        assertEquals(3.0, m.getReservedStock());
        assertEquals(7.0, m.getStock());

        m.returnStock(2.0);
        assertEquals(1.0, m.getReservedStock());
        assertEquals(9.0, m.getStock());
    }

    @Test
    void increaseAndReduceStock_behaviour() {
        Material m = new Material();
        m.setStock(5.0);

        m.increaseStock(4.0);
        assertEquals(9.0, m.getStock());

        m.reduceStock(2.0);
        assertEquals(7.0, m.getStock());
    }

    @Test
    void toggleActive_flipsFlag() {
        Material m = new Material();
        m.setIsActive(true);
        m.toggleActive();
        assertFalse(m.getIsActive());
        m.toggleActive();
        assertTrue(m.getIsActive());
    }
}
