package com.enigcode.frozen_backend.production_phases.model;

import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.sectors.model.Sector;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "production_phases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionPhase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "production_phases_gen")
    @SequenceGenerator(name = "production_phases_gen", sequenceName = "production_phases_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sector")
    private Sector sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_batch")
    private Batch batch;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ProductionPhaseStatus status;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Phase phase;

    @Column(name = "phase_order")
    private Integer phaseOrder;

    @DecimalMin(value = "0.0")
    private Double input;

    @DecimalMin(value = "0.0")
    private Double standardInput;

    @DecimalMin(value = "0.0")
    private Double output;

    @DecimalMin(value = "0.0")
    private Double standardOutput;

    @Enumerated(EnumType.STRING)
    @Column(name = "output_unit")
    private UnitMeasurement outputUnit;

    @Column(name = "product_waste")
    private Double productWaste;

    @Column(name = "start_date")
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    // LÓGICA DE INICIALIZACIÓN:
    @PrePersist
    public void setPhaseOrder() {
        if (this.phase != null) this.phaseOrder = this.phase.getOrder();
    }

    @PreUpdate
    public void calculateProductWaste() {
        if (this.phase != null) this.phaseOrder = this.phase.getOrder();

        if (input == null || output == null || standardInput == null || standardOutput == null) {
            return; // no calcular hasta tener todos los valores
        }

        // Evitar divisiones por cero
        if (standardInput == 0) {
            this.productWaste = 0.0;
            return;
        }

        // REGLA ESPECIAL: input < standardInput y output == input → NO hay desperdicio
        if (input < standardInput && Double.compare(output, input) == 0) {
            this.productWaste = 0.0;
            return;
        }

        // Cálculo general
        double expectedOutput = standardOutput * (input / standardInput);
        double wasteCalc = expectedOutput - output;

        // Nunca desperdicio negativo
        if (wasteCalc < 0) {
            wasteCalc = 0;
        }

        this.productWaste = wasteCalc;
    }
}
