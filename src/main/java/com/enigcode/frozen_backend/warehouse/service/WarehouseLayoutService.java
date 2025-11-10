package com.enigcode.frozen_backend.warehouse.service;

import com.enigcode.frozen_backend.materials.model.WarehouseZone;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseLayoutService {

    /**
     * Obtiene el contenido del SVG estático del almacén
     */
    public String getWarehouseSvg() {
        try {
            Resource svgResource = new ClassPathResource("static/warehouse/warehouse-layout.svg");
            return new String(svgResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error loading warehouse SVG", e);
            throw new RuntimeException("No se pudo cargar el mapa del almacén", e);
        }
    }

    /**
     * Valida si una zona y sección son válidas
     */
    public boolean isValidLocation(WarehouseZone zone, String section, Integer level) {
        if (zone == null || section == null || level == null) {
            return false;
        }

        return zone.isValidSection(section) && WarehouseZone.isValidLevel(level);
    }

    /**
     * Obtiene las secciones disponibles para una zona específica
     */
    public List<String> getAvailableSectionsForZone(WarehouseZone zone) {
        return zone != null ? zone.getAvailableSections() : List.of();
    }

    /**
     * Calcula las coordenadas X/Y para el frontend basado en zona, sección y nivel
     * usando las coordenadas exactas del SVG actualizado
     */
    public Double[] calculateCoordinates(WarehouseZone zone, String section, Integer level) {
        if (zone == null || section == null || level == null) {
            return new Double[] { null, null };
        }

        // Obtener coordenadas exactas de la sección desde el mapeo del SVG
        Double[] baseCoords = getSectionCoordinates(zone, section);
        if (baseCoords[0] == null || baseCoords[1] == null) {
            return new Double[] { null, null };
        }

        // Ajuste por nivel (desplazamiento vertical pequeño)
        double levelOffset = (level - 2) * 3.0; // Nivel 1: -3, Nivel 2: 0, Nivel 3: +3

        return new Double[] { baseCoords[0], baseCoords[1] + levelOffset };
    }

    /**
     * Genera el texto display para el nivel
     */
    public String getLevelDisplay(Integer level) {
        return level != null ? "Nivel " + level : null;
    }

    /**
     * Obtiene las coordenadas exactas de una sección específica basadas en el SVG
     */
    private Double[] getSectionCoordinates(WarehouseZone zone, String section) {
        String key = zone.name() + "-" + section;

        return switch (key) {
            // ZONA MALTA
            case "MALTA-A1" -> new Double[] { 65.0, 85.0 };
            case "MALTA-A2" -> new Double[] { 145.0, 85.0 };
            case "MALTA-A3" -> new Double[] { 225.0, 85.0 };
            case "MALTA-A4" -> new Double[] { 305.0, 85.0 };
            case "MALTA-A5" -> new Double[] { 385.0, 85.0 };
            case "MALTA-B1" -> new Double[] { 65.0, 145.0 };
            case "MALTA-B2" -> new Double[] { 145.0, 145.0 };
            case "MALTA-B3" -> new Double[] { 225.0, 145.0 };
            case "MALTA-B4" -> new Double[] { 305.0, 145.0 };
            case "MALTA-B5" -> new Double[] { 385.0, 145.0 };
            case "MALTA-C1" -> new Double[] { 65.0, 205.0 };
            case "MALTA-C2" -> new Double[] { 145.0, 205.0 };
            case "MALTA-C3" -> new Double[] { 225.0, 205.0 };
            case "MALTA-C4" -> new Double[] { 305.0, 205.0 };
            case "MALTA-C5" -> new Double[] { 385.0, 205.0 };

            // ZONA LUPULO
            case "LUPULO-A1" -> new Double[] { 565.0, 85.0 };
            case "LUPULO-A2" -> new Double[] { 645.0, 85.0 };
            case "LUPULO-A3" -> new Double[] { 725.0, 85.0 };
            case "LUPULO-A4" -> new Double[] { 805.0, 85.0 };
            case "LUPULO-A5" -> new Double[] { 885.0, 85.0 };
            case "LUPULO-B1" -> new Double[] { 565.0, 145.0 };
            case "LUPULO-B2" -> new Double[] { 645.0, 145.0 };
            case "LUPULO-B3" -> new Double[] { 725.0, 145.0 };
            case "LUPULO-B4" -> new Double[] { 805.0, 145.0 };
            case "LUPULO-B5" -> new Double[] { 885.0, 145.0 };
            case "LUPULO-C1" -> new Double[] { 565.0, 205.0 };
            case "LUPULO-C2" -> new Double[] { 645.0, 205.0 };
            case "LUPULO-C3" -> new Double[] { 725.0, 205.0 };
            case "LUPULO-C4" -> new Double[] { 805.0, 205.0 };
            case "LUPULO-C5" -> new Double[] { 885.0, 205.0 };

            // ZONA LEVADURA
            case "LEVADURA-A1" -> new Double[] { 75.0, 395.0 };
            case "LEVADURA-A2" -> new Double[] { 165.0, 395.0 };
            case "LEVADURA-B1" -> new Double[] { 75.0, 465.0 };
            case "LEVADURA-B2" -> new Double[] { 165.0, 465.0 };
            case "LEVADURA-C1" -> new Double[] { 75.0, 535.0 };
            case "LEVADURA-C2" -> new Double[] { 165.0, 535.0 };

            // ZONA AGUA
            case "AGUA-A1" -> new Double[] { 305.0, 395.0 };
            case "AGUA-A2" -> new Double[] { 395.0, 395.0 };
            case "AGUA-B1" -> new Double[] { 305.0, 465.0 };
            case "AGUA-B2" -> new Double[] { 395.0, 465.0 };
            case "AGUA-C1" -> new Double[] { 305.0, 535.0 };
            case "AGUA-C2" -> new Double[] { 395.0, 535.0 };

            // ZONA ENVASE
            case "ENVASE-A1" -> new Double[] { 815.0, 395.0 };
            case "ENVASE-A2" -> new Double[] { 905.0, 395.0 };
            case "ENVASE-B1" -> new Double[] { 815.0, 465.0 };
            case "ENVASE-B2" -> new Double[] { 905.0, 465.0 };
            case "ENVASE-C1" -> new Double[] { 815.0, 535.0 };
            case "ENVASE-C2" -> new Double[] { 905.0, 535.0 };

            // ZONA ETIQUETADO
            case "ETIQUETADO-A1" -> new Double[] { 585.0, 380.0 };
            case "ETIQUETADO-A2" -> new Double[] { 675.0, 380.0 };
            case "ETIQUETADO-B1" -> new Double[] { 585.0, 430.0 };
            case "ETIQUETADO-B2" -> new Double[] { 675.0, 430.0 };

            // ZONA OTROS
            case "OTROS-A1" -> new Double[] { 585.0, 510.0 };
            case "OTROS-A2" -> new Double[] { 675.0, 510.0 };
            case "OTROS-B1" -> new Double[] { 585.0, 560.0 };
            case "OTROS-B2" -> new Double[] { 675.0, 560.0 };

            default -> new Double[] { null, null };
        };
    }

}
