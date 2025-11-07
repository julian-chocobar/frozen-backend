package com.enigcode.frozen_backend.warehouse.DTO;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneSectionsDTO {
    private String zone;
    private List<String> sections;
    private Integer totalSections;
    private String layout; // e.g., "5x3" for 5 sections per row, 3 rows
}