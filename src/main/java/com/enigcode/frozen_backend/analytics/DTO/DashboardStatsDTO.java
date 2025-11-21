package com.enigcode.frozen_backend.analytics.DTO;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardStatsDTO {
    private Double totalProduced;
    private Double totalWaste;
    private Double totalMaterialsUsed;
    private Long batchesInProgress;
    private Long batchesCancelled;
    private Long batchesCompleted;
    private Long ordersRejected;
}

