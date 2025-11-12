package com.enigcode.frozen_backend.batches.model;

import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.production_orders.Model.ProductionOrder;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import com.enigcode.frozen_backend.users.model.User;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Table(name = "batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "batches_gen")
    @SequenceGenerator(name = "batches_gen", sequenceName = "batches_seq", allocationSize = 1)
    private Long id;

    @Column(unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_packaging")
    @NotNull
    private Packaging packaging;

    @OneToOne(mappedBy = "batch")
    private ProductionOrder productionOrder;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("phaseOrder ASC")
    private List<ProductionPhase> phases;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @NotNull
    private BatchStatus status;

    @NotNull
    @Min(value = 0)
    private Integer quantity;

    @Column(name = "creation_date")
    @NotNull
    private OffsetDateTime creationDate;

    @Column(name = "planned_date")
    @NotNull
    private OffsetDateTime plannedDate;

    @Column(name = "start_date")
    private OffsetDateTime startDate;

    @Column(name = "completed_date")
    private OffsetDateTime completedDate;

    @Column(name = "estimated_completed_date")
    private OffsetDateTime estimatedCompletedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    public Map<Phase, ProductionPhase> getPhasesAsMap() {
        if (phases == null || phases.isEmpty()) {
            return Map.of();
        }

        return phases.stream()
                .collect(Collectors.toMap(
                        ProductionPhase::getPhase,
                        phase -> phase));
    }
}
