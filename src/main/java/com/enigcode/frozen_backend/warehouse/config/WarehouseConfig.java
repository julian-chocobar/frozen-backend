package com.enigcode.frozen_backend.warehouse.config;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseConfig {
    private Dimensions dimensions;
    private Map<String, ZoneConfig> zones;
    private List<Walkway> walkways;
    private List<Door> doors;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Dimensions {
        private Double width;
        private Double height;
        private String unit;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Walkway {
        private Double x;
        private Double y;
        private Double width;
        private Double height;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Door {
        private Double x;
        private Double y;
        private Double width;
        private Double height;
        private String type;
    }
}