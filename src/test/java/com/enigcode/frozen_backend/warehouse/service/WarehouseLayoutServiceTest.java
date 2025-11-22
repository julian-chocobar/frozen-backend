package com.enigcode.frozen_backend.warehouse.service;

import com.enigcode.frozen_backend.materials.model.WarehouseZone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WarehouseLayoutServiceTest {

    @InjectMocks
    private WarehouseLayoutService service;

    @Test
    @DisplayName("isValidLocation returns false for null inputs")
    void isValidLocation_nulls() {
        assertFalse(service.isValidLocation(null, null, null));
        assertFalse(service.isValidLocation(WarehouseZone.MALTA, null, 1));
        assertFalse(service.isValidLocation(null, "A1", 1));
    }

    @Test
    @DisplayName("isValidLocation rejects invalid section or level")
    void isValidLocation_invalidSectionOrLevel() {
        assertFalse(service.isValidLocation(WarehouseZone.MALTA, "Z9", 1));
        assertFalse(service.isValidLocation(WarehouseZone.MALTA, "A1", 99));
    }

    @Test
    @DisplayName("isValidLocation accepts a known valid combination")
    void isValidLocation_valid() {
        assertTrue(service.isValidLocation(WarehouseZone.MALTA, "A1", 2));
    }

    @Test
    @DisplayName("calculateCoordinates returns expected offsets for levels")
    void calculateCoordinates_levelsOffset() {
        Double[] coordsLevel1 = service.calculateCoordinates(WarehouseZone.MALTA, "A1", 1);
        Double[] coordsLevel2 = service.calculateCoordinates(WarehouseZone.MALTA, "A1", 2);
        Double[] coordsLevel3 = service.calculateCoordinates(WarehouseZone.MALTA, "A1", 3);

        assertNotNull(coordsLevel1);
        assertNotNull(coordsLevel2);
        assertNotNull(coordsLevel3);

        assertEquals(coordsLevel2[0], coordsLevel1[0]);
        assertEquals(coordsLevel3[0], coordsLevel1[0]);

        double diff21 = coordsLevel2[1] - coordsLevel1[1];
        double diff32 = coordsLevel3[1] - coordsLevel2[1];

        assertEquals(3.0, diff32, 0.0001);
        assertEquals(3.0, Math.abs(diff21), 0.0001);
    }

    @Test
    @DisplayName("calculateCoordinates returns nulls for unknown section")
    void calculateCoordinates_unknownSection() {
        Double[] coords = service.calculateCoordinates(WarehouseZone.MALTA, "Z9", 2);
        assertNull(coords[0]);
        assertNull(coords[1]);
    }
}
