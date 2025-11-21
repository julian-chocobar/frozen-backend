package com.enigcode.frozen_backend.analytics.DTO;

public interface GeneralDashboardProjectionDTO {
    Double getTotalProduced();
    Double getTotalWaste();
    Double getTotalMaterialsUsed();
    Long getBatchesInProgress();
    Long getBatchesCancelled();
    Long getBatchesCompleted();
    Long getOrdersRejected();
}
