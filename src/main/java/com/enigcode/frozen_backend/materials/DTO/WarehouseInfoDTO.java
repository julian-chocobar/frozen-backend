package com.enigcode.frozen_backend.materials.DTO;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseInfoDTO {
    private List<String> availableZones;
    private Map<String, List<String>> sectionsByZone;
    private SuggestedLocationDTO suggestedLocation;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SuggestedLocationDTO {
        private String zone;
        private String section;
        private Double x;
        private Double y;
        private Integer level;
    }
}