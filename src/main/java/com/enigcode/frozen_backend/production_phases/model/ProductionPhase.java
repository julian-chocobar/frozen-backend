package com.enigcode.frozen_backend.production_phases.model;

import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.sectors.model.Sector;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

    @Column(name = "movement_waste")
    private Double movementWaste;

    @Column(name = "start_date")
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    /**
     * Obtiene la siguiente fase del batch en el orden correcto
     * 
     * @return Optional con la siguiente ProductionPhase, o empty si no hay siguiente fase
     */
    public Optional<ProductionPhase> getNextPhase() {
        if (this.batch == null || this.batch.getPhases() == null) {
            return Optional.empty();
        }
        
        List<ProductionPhase> phases = this.batch.getPhases().stream()
                .sorted(Comparator.comparing(ProductionPhase::getPhaseOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        
        int currentIndex = phases.indexOf(this);
        if (currentIndex >= 0 && currentIndex < phases.size() - 1) {
            return Optional.of(phases.get(currentIndex + 1));
        }
        return Optional.empty();
    }
    
    /**
     * Obtiene la fase anterior del batch en el orden correcto
     * 
     * @return Optional con la ProductionPhase anterior, o empty si no hay fase anterior
     */
    public Optional<ProductionPhase> getPreviousPhase() {
        if (this.batch == null || this.batch.getPhases() == null) {
            return Optional.empty();
        }
        
        List<ProductionPhase> phases = this.batch.getPhases().stream()
                .sorted(Comparator.comparing(ProductionPhase::getPhaseOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        
        int currentIndex = phases.indexOf(this);
        if (currentIndex > 0) {
            return Optional.of(phases.get(currentIndex - 1));
        }
        return Optional.empty();
    }
    
    /**
     * Valida que el standardOutput de esta fase coincida con el standardInput de la siguiente fase.
     * Lanza BadRequestException si no coinciden.
     */
    public void validateStandardOutputMatchesNextPhaseStandardInput() {
        if (this.standardOutput == null || this.outputUnit == null) {
            return; // No validar si no está completo
        }
        
        Optional<ProductionPhase> nextPhaseOpt = getNextPhase();
        if (nextPhaseOpt.isPresent()) {
            ProductionPhase nextPhase = nextPhaseOpt.get();
            if (nextPhase.getStandardInput() != null && !this.standardOutput.equals(nextPhase.getStandardInput())) {
                throw new com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException(
                    String.format("El standardOutput de la fase %s (%.2f %s) no coincide con el standardInput de la siguiente fase %s (%.2f %s). " +
                            "El standardOutput debe ser igual al standardInput de la siguiente fase.",
                        this.phase, this.standardOutput, this.outputUnit,
                        nextPhase.getPhase(), nextPhase.getStandardInput(), 
                        nextPhase.getOutputUnit() != null ? nextPhase.getOutputUnit() : "N/A"));
            }
            // Validar también que las unidades coincidan
            if (nextPhase.getOutputUnit() != null && !this.outputUnit.equals(nextPhase.getOutputUnit())) {
                throw new com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException(
                    String.format("La unidad de medida del standardOutput de la fase %s (%s) no coincide con la unidad del standardInput de la siguiente fase %s (%s).",
                        this.phase, this.outputUnit,
                        nextPhase.getPhase(), nextPhase.getOutputUnit()));
            }
        }
    }

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
