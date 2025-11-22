package com.enigcode.frozen_backend.warehouse.service;

import com.enigcode.frozen_backend.materials.model.WarehouseZone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WarehouseCoordinateMapperTest {

    private final WarehouseCoordinateMapper mapper = new WarehouseCoordinateMapper();

    @Test
    @DisplayName("calculateCoordinates returns invalid for null inputs")
    void calculateCoordinates_nullInputs() {
        WarehouseCoordinateMapper.CoordinateResult r1 = mapper.calculateCoordinates(null, "A1", 1);
        assertFalse(r1.isValid);

        WarehouseCoordinateMapper.CoordinateResult r2 = mapper.calculateCoordinates(WarehouseZone.MALTA, null, 1);
        assertFalse(r2.isValid);
    }

    @Test
    @DisplayName("calculateCoordinates parses A1 correctly")
    void calculateCoordinates_parseA1() {
        WarehouseCoordinateMapper.CoordinateResult r = mapper.calculateCoordinates(WarehouseZone.MALTA, "A1", 2);
        assertTrue(r.isValid);
        assertTrue(r.x > 0);
        assertTrue(r.y > 0);
        assertEquals("Coordenadas calculadas correctamente", r.message);
    }

    @Test
    @DisplayName("calculateCoordinates rejects invalid section format")
    void calculateCoordinates_invalidFormat() {
        WarehouseCoordinateMapper.CoordinateResult r = mapper.calculateCoordinates(WarehouseZone.MALTA, "AA1", 1);
        assertFalse(r.isValid);
        assertTrue(r.message.contains("Formato de sección inválido") || r.message.length() > 0);
    }

    @Test
    @DisplayName("calculateCoordinates rejects section out of zone range")
    void calculateCoordinates_sectionOutOfRange() {
        // Use a col that is outside maxCols (e.g. C5 is valid but we try C6 via incorrect char '6')
        WarehouseCoordinateMapper.CoordinateResult r = mapper.calculateCoordinates(WarehouseZone.MALTA, "C6", 1);
        assertFalse(r.isValid);
    }
}
