package com.enigcode.frozen_backend.warehouse.config;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneConfig {
    private Bounds bounds;
    private SectionSize sectionSize;
    private SectionSpacing sectionSpacing;
    private Integer maxSectionsPerRow;
    private Integer maxRows;
    private Integer priority;
    private String description;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Bounds {
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
    public static class SectionSize {
        private Double width;
        private Double height;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SectionSpacing {
        private Double x;
        private Double y;
    }
}