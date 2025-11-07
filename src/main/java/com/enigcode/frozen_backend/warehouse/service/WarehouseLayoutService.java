package com.enigcode.frozen_backend.warehouse.service;

import com.enigcode.frozen_backend.materials.DTO.MaterialWarehouseLocationDTO;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.WarehouseCoordinates;
import com.enigcode.frozen_backend.warehouse.config.WarehouseConfig;
import com.enigcode.frozen_backend.warehouse.config.ZoneConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseLayoutService {

    @Value("${warehouse.layout.svg-path:classpath:static/warehouse/warehouse-layout.svg}")
    private String svgPath;

    @Value("${warehouse.layout.config-path:classpath:static/warehouse/warehouse-config.json}")
    private String configPath;

    private WarehouseConfig warehouseConfig;

    @PostConstruct
    public void loadWarehouseConfig() {
        try {
            Resource configResource = new ClassPathResource("static/warehouse/warehouse-config.json");
            ObjectMapper mapper = new ObjectMapper();
            this.warehouseConfig = mapper.readValue(
                    configResource.getInputStream(),
                    WarehouseConfig.class);
            log.info("Warehouse configuration loaded successfully");
        } catch (IOException e) {
            log.error("Error loading warehouse configuration", e);
            // Crear configuración por defecto si no se puede cargar
            this.warehouseConfig = createDefaultWarehouseConfig();
            log.info("Using default warehouse configuration");
        }
    }

    public String getWarehouseSvg() {
        try {
            Resource svgResource = new ClassPathResource("static/warehouse/warehouse-layout.svg");
            return new String(svgResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error loading warehouse SVG", e);
            // Retornar SVG básico por defecto
            return createDefaultSvg();
        }
    }

    public WarehouseConfig getWarehouseConfig() {
        return this.warehouseConfig;
    }

    public WarehouseCoordinates calculateCoordinatesForSection(String zone, String section) {
        ZoneConfig zoneConfig = warehouseConfig.getZones().get(zone);
        if (zoneConfig == null) {
            log.warn("Zone not found: {}, using default coordinates", zone);
            return new WarehouseCoordinates(50.0, 50.0);
        }

        // Parsear sección (ej: A1, B2, C3)
        if (section == null || section.length() < 2) {
            return new WarehouseCoordinates(
                    zoneConfig.getBounds().getX() + 25.0,
                    zoneConfig.getBounds().getY() + 25.0);
        }

        char row = section.charAt(0);
        int col;
        try {
            col = Integer.parseInt(section.substring(1));
        } catch (NumberFormatException e) {
            col = 1;
        }

        // Calcular coordenadas dentro de la zona
        double x = zoneConfig.getBounds().getX() +
                ((col - 1) * zoneConfig.getSectionSpacing().getX()) +
                (zoneConfig.getSectionSize().getWidth() / 2);

        double y = zoneConfig.getBounds().getY() +
                ((row - 'A') * zoneConfig.getSectionSpacing().getY()) +
                (zoneConfig.getSectionSize().getHeight() / 2);

        return new WarehouseCoordinates(x, y);
    }

    public List<String> getAvailableSectionsForZone(String zone) {
        ZoneConfig zoneConfig = warehouseConfig.getZones().get(zone);
        if (zoneConfig == null) {
            return Collections.emptyList();
        }

        List<String> sections = new ArrayList<>();
        for (int row = 0; row < zoneConfig.getMaxRows(); row++) {
            char rowLetter = (char) ('A' + row);
            for (int col = 1; col <= zoneConfig.getMaxSectionsPerRow(); col++) {
                sections.add(rowLetter + String.valueOf(col));
            }
        }
        return sections;
    }

    public boolean isValidSection(String zone, String section) {
        return getAvailableSectionsForZone(zone).contains(section);
    }

    public String getDefaultZoneForMaterialType(MaterialType type) {
        return switch (type) {
            case MALTA -> "ZONA_MALTA";
            case LUPULO -> "ZONA_LUPULO";
            case AGUA -> "ZONA_AGUA";
            case LEVADURA -> "ZONA_LEVADURA";
            case ENVASE -> "ZONA_ENVASE";
            case ETIQUETADO -> "ZONA_ETIQUETADO";
            case OTROS -> "ZONA_OTROS";
        };
    }

    public String addMaterialsToSvg(String baseSvg, List<MaterialWarehouseLocationDTO> materials) {
        // Aquí se puede agregar la lógica para insertar marcadores de materiales en el
        // SVG
        // Por ahora retornamos el SVG base
        return baseSvg;
    }

    public String appleModeStyles(String baseSvg, String mode) {
        // Aplicar estilos específicos según el modo (modal, fullscreen, etc.)
        return baseSvg;
    }

    private WarehouseConfig createDefaultWarehouseConfig() {
        // Configuración por defecto basada en MaterialType
        ZoneConfig maltaZone = ZoneConfig.builder()
                .bounds(ZoneConfig.Bounds.builder().x(20.0).y(20.0).width(450.0).height(250.0).build())
                .sectionSize(ZoneConfig.SectionSize.builder().width(50.0).height(50.0).build())
                .sectionSpacing(ZoneConfig.SectionSpacing.builder().x(60.0).y(60.0).build())
                .maxSectionsPerRow(7)
                .maxRows(4)
                .priority(1)
                .description("Almacenamiento de malta y cereales")
                .build();

        ZoneConfig lupuloZone = ZoneConfig.builder()
                .bounds(ZoneConfig.Bounds.builder().x(530.0).y(20.0).width(450.0).height(250.0).build())
                .sectionSize(ZoneConfig.SectionSize.builder().width(50.0).height(50.0).build())
                .sectionSpacing(ZoneConfig.SectionSpacing.builder().x(60.0).y(60.0).build())
                .maxSectionsPerRow(7)
                .maxRows(4)
                .priority(2)
                .description("Almacenamiento de lúpulo refrigerado")
                .build();

        ZoneConfig levaduraZone = ZoneConfig.builder()
                .bounds(ZoneConfig.Bounds.builder().x(20.0).y(330.0).width(200.0).height(250.0).build())
                .sectionSize(ZoneConfig.SectionSize.builder().width(40.0).height(40.0).build())
                .sectionSpacing(ZoneConfig.SectionSpacing.builder().x(45.0).y(45.0).build())
                .maxSectionsPerRow(4)
                .maxRows(5)
                .priority(3)
                .description("Levaduras y cultivos - temperatura controlada")
                .build();

        return WarehouseConfig.builder()
                .dimensions(WarehouseConfig.Dimensions.builder()
                        .width(1000.0)
                        .height(600.0)
                        .unit("pixels")
                        .build())
                .zones(java.util.Map.of(
                        "ZONA_MALTA", maltaZone,
                        "ZONA_LUPULO", lupuloZone,
                        "ZONA_LEVADURA", levaduraZone))
                .walkways(List.of(
                        WarehouseConfig.Walkway.builder().x(20.0).y(280.0).width(960.0).height(40.0).build(),
                        WarehouseConfig.Walkway.builder().x(480.0).y(20.0).width(40.0).height(560.0).build()))
                .doors(List.of(
                        WarehouseConfig.Door.builder().x(470.0).y(10.0).width(60.0).height(10.0).type("main_entrance")
                                .build()))
                .build();
    }

    private String createDefaultSvg() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <svg viewBox="0 0 1000 600" xmlns="http://www.w3.org/2000/svg">
                    <rect x="20" y="20" width="450" height="250" fill="#8B4513" stroke="#654321" stroke-width="2"/>
                    <text x="245" y="35" font-family="Arial" font-size="14" font-weight="bold" text-anchor="middle" fill="white">ZONA MALTA</text>
                    <rect x="530" y="20" width="450" height="250" fill="#228B22" stroke="#006400" stroke-width="2"/>
                    <text x="755" y="35" font-family="Arial" font-size="14" font-weight="bold" text-anchor="middle" fill="white">ZONA LUPULO</text>
                    <rect x="20" y="330" width="200" height="250" fill="#FFD700" stroke="#FFA500" stroke-width="2"/>
                    <text x="120" y="345" font-family="Arial" font-size="14" font-weight="bold" text-anchor="middle" fill="black">ZONA LEVADURA</text>
                </svg>
                """;
    }

    // Métodos para gestión dinámica de zonas y secciones

    public ZoneConfig updateZoneConfig(String zoneName,
            com.enigcode.frozen_backend.warehouse.DTO.ZoneConfigUpdateDTO updateDTO) {
        ZoneConfig zoneConfig = warehouseConfig.getZones().get(zoneName);
        if (zoneConfig == null) {
            throw new IllegalArgumentException("Zona no encontrada: " + zoneName);
        }

        // Actualizar configuración
        if (updateDTO.getMaxSectionsPerRow() != null) {
            zoneConfig.setMaxSectionsPerRow(updateDTO.getMaxSectionsPerRow());
        }
        if (updateDTO.getMaxRows() != null) {
            zoneConfig.setMaxRows(updateDTO.getMaxRows());
        }
        if (updateDTO.getSectionWidth() != null) {
            zoneConfig.getSectionSize().setWidth(updateDTO.getSectionWidth());
        }
        if (updateDTO.getSectionHeight() != null) {
            zoneConfig.getSectionSize().setHeight(updateDTO.getSectionHeight());
        }
        if (updateDTO.getSpacingX() != null) {
            zoneConfig.getSectionSpacing().setX(updateDTO.getSpacingX());
        }
        if (updateDTO.getSpacingY() != null) {
            zoneConfig.getSectionSpacing().setY(updateDTO.getSpacingY());
        }
        if (updateDTO.getDescription() != null) {
            zoneConfig.setDescription(updateDTO.getDescription());
        }

        // Guardar configuración actualizada
        saveWarehouseConfig();

        log.info("Configuración de zona {} actualizada: {}x{} secciones",
                zoneName, zoneConfig.getMaxSectionsPerRow(), zoneConfig.getMaxRows());

        return zoneConfig;
    }

    public String regenerateSvg() {
        // Recargar configuración
        loadWarehouseConfig();
        // Generar SVG dinámicamente
        return generateDynamicSvg();
    }

    public com.enigcode.frozen_backend.warehouse.DTO.ZoneSectionsDTO getZoneSections(String zoneName) {
        List<String> sections = getAvailableSectionsForZone(zoneName);
        ZoneConfig zoneConfig = warehouseConfig.getZones().get(zoneName);

        return com.enigcode.frozen_backend.warehouse.DTO.ZoneSectionsDTO.builder()
                .zone(zoneName)
                .sections(sections)
                .totalSections(sections.size())
                .layout(zoneConfig != null ? zoneConfig.getMaxSectionsPerRow() + "x" + zoneConfig.getMaxRows()
                        : "unknown")
                .build();
    }

    public List<com.enigcode.frozen_backend.warehouse.DTO.ZoneSectionsDTO> getAllZonesSections() {
        return warehouseConfig.getZones().keySet().stream()
                .map(this::getZoneSections)
                .collect(java.util.stream.Collectors.toList());
    }

    private void saveWarehouseConfig() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(
                    new java.io.File("src/main/resources/static/warehouse/warehouse-config.json"),
                    warehouseConfig);
            log.info("Configuración del almacén guardada exitosamente");
        } catch (IOException e) {
            log.error("Error guardando configuración del almacén", e);
        }
    }

    private String generateDynamicSvg() {
        StringBuilder svg = new StringBuilder();
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        svg.append(
                "<svg viewBox=\"0 0 1000 600\" preserveAspectRatio=\"xMidYMid meet\" xmlns=\"http://www.w3.org/2000/svg\" class=\"warehouse-svg\">\n");

        // Agregar estilos
        svg.append(getSvgStyles());

        // Contenedor principal
        svg.append("<g id=\"warehouse-container\">\n");

        // Paredes y elementos fijos
        svg.append(getFixedElements());

        // Generar zonas dinámicamente
        warehouseConfig.getZones().forEach((zoneName, zoneConfig) -> {
            svg.append(generateZoneSvg(zoneName, zoneConfig));
        });

        // Layers para materiales y UI
        svg.append("<g id=\"materials-layer\" class=\"materials-overlay\"></g>\n");
        svg.append("<g id=\"ui-layer\" class=\"ui-overlay\"></g>\n");

        svg.append("</g>\n");
        svg.append("</svg>");
        return svg.toString();
    }

    private String getSvgStyles() {
        return """
                <defs>
                    <style>
                        .zone-malta { fill: #8B4513; stroke: #654321; stroke-width: 2; }
                        .zone-lupulo { fill: #228B22; stroke: #006400; stroke-width: 2; }
                        .zone-agua { fill: #4169E1; stroke: #000080; stroke-width: 2; }
                        .zone-levadura { fill: #FFD700; stroke: #FFA500; stroke-width: 2; }
                        .zone-envase { fill: #D3D3D3; stroke: #808080; stroke-width: 2; }
                        .zone-etiquetado { fill: #DDA0DD; stroke: #9370DB; stroke-width: 2; }
                        .zone-otros { fill: #F0E68C; stroke: #DAA520; stroke-width: 2; }
                        .walkway { fill: #E6E6FA; stroke: #9370DB; stroke-width: 1; }
                        .wall { fill: none; stroke: #2F4F4F; stroke-width: 4; }
                        .door { fill: #8B4513; stroke: #654321; stroke-width: 2; }
                        .section-grid { fill: none; stroke: #666; stroke-width: 0.5; stroke-dasharray: 2,2; }
                        .text-zone { font-family: Arial; font-size: 14px; font-weight: bold; }
                        .text-section { font-family: Arial; font-size: 10px; fill: #333; }
                        .zone:hover { opacity: 0.8; cursor: pointer; }
                        .section:hover { stroke-width: 3; cursor: pointer; }
                    </style>
                </defs>
                """;
    }

    private String getFixedElements() {
        return """
                <!-- Paredes exteriores -->
                <rect x="10" y="10" width="980" height="580" class="wall"/>

                <!-- Entrada principal -->
                <rect x="470" y="10" width="60" height="10" class="door"/>

                <!-- Pasillos principales -->
                <rect x="20" y="280" width="960" height="40" class="walkway"/>
                <rect x="480" y="20" width="40" height="560" class="walkway"/>
                """;
    }

    private String generateZoneSvg(String zoneName, ZoneConfig config) {
        StringBuilder zoneSvg = new StringBuilder();

        String zoneClass = "zone-" + zoneName.toLowerCase().replace("zona_", "");
        String zoneId = zoneName.toLowerCase().replace("_", "-");

        zoneSvg.append(String.format(
                "<g id=\"%s\" class=\"zone\" data-zone=\"%s\">\n", zoneId, zoneName));

        // Fondo de la zona
        zoneSvg.append(String.format(
                "<rect x=\"%.1f\" y=\"%.1f\" width=\"%.1f\" height=\"%.1f\" class=\"%s\"/>\n",
                config.getBounds().getX(), config.getBounds().getY(),
                config.getBounds().getWidth(), config.getBounds().getHeight(),
                zoneClass));

        // Título de la zona
        double centerX = config.getBounds().getX() + config.getBounds().getWidth() / 2;
        double titleY = config.getBounds().getY() + 20;
        zoneSvg.append(String.format(
                "<text x=\"%.1f\" y=\"%.1f\" class=\"text-zone\" text-anchor=\"middle\" fill=\"white\">%s</text>\n",
                centerX, titleY, zoneName.replace("_", " ")));

        // Container de secciones
        zoneSvg.append(String.format(
                "<g class=\"sections-container\" data-zone=\"%s\">\n", zoneName));

        // Generar todas las secciones dinámicamente
        for (int row = 0; row < config.getMaxRows(); row++) {
            char rowLetter = (char) ('A' + row);
            for (int col = 1; col <= config.getMaxSectionsPerRow(); col++) {
                String section = rowLetter + String.valueOf(col);
                zoneSvg.append(generateSectionSvg(zoneName, section, config, row, col));
            }
        }

        zoneSvg.append("</g>\n"); // Cierra sections-container
        zoneSvg.append("</g>\n"); // Cierra zona

        return zoneSvg.toString();
    }

    private String generateSectionSvg(String zoneName, String section, ZoneConfig config, int row, int col) {
        double x = config.getBounds().getX() + (col - 1) * config.getSectionSpacing().getX() + 10;
        double y = config.getBounds().getY() + row * config.getSectionSpacing().getY() + 35;

        double centerX = x + config.getSectionSize().getWidth() / 2;
        double centerY = y + config.getSectionSize().getHeight() / 2;

        return String.format("""
                <g class="section" id="section-%s-%s" data-zone="%s" data-section="%s" data-x="%.1f" data-y="%.1f">
                    <rect x="%.1f" y="%.1f" width="%.1f" height="%.1f" class="section-background section-grid"/>
                    <text x="%.1f" y="%.1f" class="text-section section-label" text-anchor="middle">%s</text>
                    <circle cx="%.1f" cy="%.1f" r="2" class="anchor-point" opacity="0"/>
                </g>
                """,
                zoneName, section, zoneName, section, centerX, centerY,
                x, y, config.getSectionSize().getWidth(), config.getSectionSize().getHeight(),
                centerX, centerY + 3, section,
                centerX, centerY);
    }
}