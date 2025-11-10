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
    private List<ZoneInfoDTO> availableZones;
    private SuggestedLocationDTO suggestedLocation;
    private Map<String, Long> materialsByZone;
    private Long totalMaterials;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ZoneInfoDTO {
        private String name;
        private String displayName;
        private Integer totalSections;
        private Integer occupiedSections;
        private List<String> availableSections;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SuggestedLocationDTO {
        private String zone;
        private String section;
        private Integer level;
    }
}