package com.enigcode.frozen_backend.batches.DTO;

import com.enigcode.frozen_backend.batches.model.BatchStatus;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.production_orders.Model.OrderStatus;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhaseStatus;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchTraceabilityDTO {

    // Información básica del batch
    private Long batchId;
    private String batchCode;
    private BatchStatus batchStatus;
    private Integer quantity;
    private OffsetDateTime creationDate;
    private OffsetDateTime plannedDate;
    private OffsetDateTime startDate;
    private OffsetDateTime estimatedCompletedDate;
    private OffsetDateTime completedDate;

    // Información del producto
    private String productName;
    private UnitMeasurement productUnitMeasurement;
    private Double standardQuantity;

    // Información del packaging
    private String packagingName;
    private Double packagingQuantity;
    private UnitMeasurement packagingUnitMeasurement;

    // Información de la orden de producción
    private Long productionOrderId;
    private OrderStatus orderStatus;
    private OffsetDateTime orderCreationDate;
    private OffsetDateTime orderValidationDate;

    // Usuarios involucrados
    private String assignedUserName;
    private String createdByUserName;
    private String approvedByUserName;

    // Fases de producción con todos sus detalles
    private List<ProductionPhaseTraceabilityDTO> phases;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductionPhaseTraceabilityDTO {
        private Long phaseId;
        private Phase phase;
        private ProductionPhaseStatus status;
        private Double input;
        private Double standardInput;
        private Double output;
        private Double standardOutput;
        private UnitMeasurement outputUnit;
        private OffsetDateTime startDate;
        private OffsetDateTime endDate;

        // Sector responsable
        private String sectorName;

        // Materiales utilizados en esta fase
        private List<ProductionMaterialTraceabilityDTO> materials;

        // Parámetros de calidad de esta fase
        private List<QualityParameterTraceabilityDTO> qualityParameters;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductionMaterialTraceabilityDTO {
        private String materialName;
        private String materialType;
        private Double quantity;
        private UnitMeasurement unitMeasurement;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QualityParameterTraceabilityDTO {
        private String parameterName;
        private String value;
        private String unit;
        private Boolean isApproved;
        private Boolean isCritical;
        private OffsetDateTime realizationDate;
    }
}