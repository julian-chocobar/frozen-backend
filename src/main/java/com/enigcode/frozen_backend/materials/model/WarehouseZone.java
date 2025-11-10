package com.enigcode.frozen_backend.materials.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum WarehouseZone {
    MALTA("MALTA", List.of("A1", "A2", "A3", "A4", "A5", "B1", "B2", "B3", "B4", "B5", "C1", "C2", "C3", "C4", "C5")),
    LUPULO("LÚPULO", List.of("A1", "A2", "A3", "A4", "A5", "B1", "B2", "B3", "B4", "B5", "C1", "C2", "C3", "C4", "C5")),
    LEVADURA("LEVADURA", List.of("A1", "A2", "B1", "B2", "C1", "C2")),
    AGUA("AGUA", List.of("A1", "A2", "B1", "B2", "C1", "C2")),
    ENVASE("ENVASES", List.of("A1", "A2", "B1", "B2", "C1", "C2")),
    ETIQUETADO("ETIQUETADO", List.of("A1", "A2", "B1", "B2")),
    OTROS("OTROS", List.of("A1", "A2", "B1", "B2"));

    private final String displayName;
    private final List<String> availableSections;

    public static WarehouseZone getDefaultZoneForMaterialType(MaterialType materialType) {
        return switch (materialType) {
            case MALTA -> MALTA;
            case LUPULO -> LUPULO;
            case LEVADURA -> LEVADURA;
            case AGUA -> AGUA;
            case ENVASE -> ENVASE;
            case ETIQUETADO -> ETIQUETADO;
            case OTROS -> OTROS;
        };
    }

    public boolean isValidSection(String section) {
        return availableSections.contains(section);
    }

    public static boolean isValidLevel(Integer level) {
        return level != null && level >= 1 && level <= 3;
    }
}