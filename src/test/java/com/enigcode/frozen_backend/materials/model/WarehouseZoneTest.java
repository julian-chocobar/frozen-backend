package com.enigcode.frozen_backend.materials.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WarehouseZoneTest {

    @Test
    void isValidSection_and_getDefaultZone() {
        assertTrue(WarehouseZone.MALTA.isValidSection("A1"));
        assertFalse(WarehouseZone.MALTA.isValidSection("ZZ"));

        assertEquals(WarehouseZone.MALTA, WarehouseZone.getDefaultZoneForMaterialType(MaterialType.MALTA));
        assertEquals(WarehouseZone.ENVASE, WarehouseZone.getDefaultZoneForMaterialType(MaterialType.ENVASE));
    }

    @Test
    void isValidLevel_checksBounds() {
        assertTrue(WarehouseZone.isValidLevel(1));
        assertTrue(WarehouseZone.isValidLevel(3));
        assertFalse(WarehouseZone.isValidLevel(0));
        assertFalse(WarehouseZone.isValidLevel(4));
    }
}
