package com.enigcode.frozen_backend.warehouse.service;

import com.enigcode.frozen_backend.materials.model.WarehouseZone;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;

@Service
public class WarehouseCoordinateMapper {

    // Mapeo de coordenadas base por zona (basado en el SVG actual)
    private static final Map<WarehouseZone, ZoneCoordinates> ZONE_COORDINATES = new HashMap<>() {
        {
            put(WarehouseZone.MALTA, new ZoneCoordinates(20, 20, 70, 70, 5, 3)); // x, y, spacingX, spacingY, cols, rows
            put(WarehouseZone.LUPULO, new ZoneCoordinates(530, 20, 70, 70, 5, 3));
            put(WarehouseZone.LEVADURA, new ZoneCoordinates(20, 330, 107, 80, 2, 3));
            put(WarehouseZone.AGUA, new ZoneCoordinates(245, 330, 107, 80, 2, 3));
            put(WarehouseZone.ENVASE, new ZoneCoordinates(755, 330, 107, 80, 2, 3));
            put(WarehouseZone.ETIQUETADO, new ZoneCoordinates(530, 330, 107, 55, 2, 2));
            put(WarehouseZone.OTROS, new ZoneCoordinates(530, 460, 107, 55, 2, 2));
        }
    };

    /**
     * Convierte ubicación lógica (zona + sección + nivel) a coordenadas SVG
     */
    public CoordinateResult calculateCoordinates(WarehouseZone zone, String section, Integer level) {
        if (zone == null || section == null) {
            return new CoordinateResult(0.0, 0.0, false, "Zona o sección inválida");
        }

        ZoneCoordinates zoneCoords = ZONE_COORDINATES.get(zone);
        if (zoneCoords == null) {
            return new CoordinateResult(0.0, 0.0, false, "Zona no configurada");
        }

        // Parsear sección (ej: "A1" -> row=0, col=0)
        SectionPosition position = parseSectionPosition(section);
        if (!position.isValid()) {
            return new CoordinateResult(0.0, 0.0, false, "Formato de sección inválido");
        }

        // Validar que la sección existe en esta zona
        if (!isValidSectionForZone(zone, position)) {
            return new CoordinateResult(0.0, 0.0, false, "Sección no válida para esta zona");
        }

        // Calcular coordenadas
        double baseX = zoneCoords.baseX + (position.col * zoneCoords.spacingX) + (zoneCoords.spacingX / 2);
        double baseY = zoneCoords.baseY + (position.row * zoneCoords.spacingY) + (zoneCoords.spacingY / 2);

        // Ajuste sutil por nivel (para diferenciar visualmente)
        double offsetX = (level != null && level > 1) ? (level - 1) * 3 : 0;
        double offsetY = (level != null && level > 1) ? (level - 1) * 3 : 0;

        return new CoordinateResult(
                baseX + offsetX,
                baseY + offsetY,
                true,
                "Coordenadas calculadas correctamente");
    }

    /**
     * Parsea una sección como "A1" -> row=0, col=0
     */
    private SectionPosition parseSectionPosition(String section) {
        if (section == null || section.length() != 2) {
            return new SectionPosition(-1, -1);
        }

        char rowChar = section.charAt(0);
        char colChar = section.charAt(1);

        if (rowChar < 'A' || rowChar > 'C' || colChar < '1' || colChar > '5') {
            return new SectionPosition(-1, -1);
        }

        int row = rowChar - 'A'; // A=0, B=1, C=2
        int col = colChar - '1'; // 1=0, 2=1, 3=2, 4=3, 5=4

        return new SectionPosition(row, col);
    }

    /**
     * Valida que una sección existe en la zona especificada
     */
    private boolean isValidSectionForZone(WarehouseZone zone, SectionPosition position) {
        ZoneCoordinates zoneCoords = ZONE_COORDINATES.get(zone);
        return position.row >= 0 && position.row < zoneCoords.maxRows &&
                position.col >= 0 && position.col < zoneCoords.maxCols;
    }

    // Clases helper
    private static class ZoneCoordinates {
        final double baseX, baseY, spacingX, spacingY;
        final int maxCols, maxRows;

        ZoneCoordinates(double baseX, double baseY, double spacingX, double spacingY, int maxCols, int maxRows) {
            this.baseX = baseX;
            this.baseY = baseY;
            this.spacingX = spacingX;
            this.spacingY = spacingY;
            this.maxCols = maxCols;
            this.maxRows = maxRows;
        }
    }

    private static class SectionPosition {
        final int row, col;

        SectionPosition(int row, int col) {
            this.row = row;
            this.col = col;
        }

        boolean isValid() {
            return row >= 0 && col >= 0;
        }
    }

    public static class CoordinateResult {
        public final double x, y;
        public final boolean isValid;
        public final String message;

        public CoordinateResult(double x, double y, boolean isValid, String message) {
            this.x = x;
            this.y = y;
            this.isValid = isValid;
            this.message = message;
        }
    }
}