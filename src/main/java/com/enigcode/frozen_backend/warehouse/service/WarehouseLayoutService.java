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
     */
    public Double[] calculateCoordinates(WarehouseZone zone, String section, Integer level) {
        if (zone == null || section == null || level == null) {
            return new Double[] { null, null };
        }

        // Coordenadas base por zona (centros aproximados de cada zona en el SVG)
        double baseX = getBaseX(zone);
        double baseY = getBaseY(zone);

        // Ajustes por sección dentro de la zona
        double[] sectionOffset = getSectionOffset(section);

        // Ajuste por nivel (desplazamiento vertical mínimo)
        double levelOffset = (level - 2) * 5.0; // Nivel 1: -5, Nivel 2: 0, Nivel 3: +5

        double finalX = baseX + sectionOffset[0];
        double finalY = baseY + sectionOffset[1] + levelOffset;

        return new Double[] { finalX, finalY };
    }

    /**
     * Genera el texto display para el nivel
     */
    public String getLevelDisplay(Integer level) {
        return level != null ? "Nivel " + level : null;
    }

    private double getBaseX(WarehouseZone zone) {
        return switch (zone) {
            case MALTA -> 100.0;
            case LUPULO -> 300.0;
            case LEVADURA -> 500.0;
            case AGUA -> 700.0;
            case ENVASE -> 200.0;
            case ETIQUETADO -> 400.0;
            case OTROS -> 600.0;
        };
    }

    private double getBaseY(WarehouseZone zone) {
        return switch (zone) {
            case MALTA, LUPULO, LEVADURA, AGUA -> 100.0;
            case ENVASE, ETIQUETADO, OTROS -> 300.0;
        };
    }

    private double[] getSectionOffset(String section) {
        if (section == null || section.length() < 2) {
            return new double[] { 0.0, 0.0 };
        }

        // Extraer letra y número de la sección (ej: "A1" -> A=0, 1=0)
        char letter = section.charAt(0);
        char number = section.charAt(1);

        // Offset horizontal por letra (A=0, B=50, C=100, etc.)
        double letterOffset = (letter - 'A') * 50.0;

        // Offset vertical por número (1=0, 2=30, 3=60, etc.)
        double numberOffset = (Character.getNumericValue(number) - 1) * 30.0;

        return new double[] { letterOffset, numberOffset };
    }

}
