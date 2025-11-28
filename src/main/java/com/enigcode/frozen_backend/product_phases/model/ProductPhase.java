package com.enigcode.frozen_backend.product_phases.model;

import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.products.model.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Entity
@Table(name = "product_phases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPhase {

    private static final Map<Phase, List<MaterialType>> REQUIRED_MATERIALS;
    static {
        Map<Phase, List<MaterialType>> map = new HashMap<>();

        // Carga de datos
        map.put(Phase.MOLIENDA, List.of(MaterialType.MALTA));
        map.put(Phase.MACERACION, List.of(MaterialType.AGUA));
        map.put(Phase.FILTRACION, List.of());
        map.put(Phase.COCCION, List.of(MaterialType.AGUA, MaterialType.LUPULO));
        map.put(Phase.FERMENTACION, List.of(MaterialType.LEVADURA));
        map.put(Phase.MADURACION, List.of());
        map.put(Phase.GASIFICACION, List.of());
        map.put(Phase.ENVASADO, List.of());

        REQUIRED_MATERIALS = map;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_phases_gen")
    @SequenceGenerator(name = "product_phases_gen", sequenceName = "product_phases_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_product")
    @NotNull
    private Product product;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Phase phase;

    @Column(name = "phase_order")
    private Integer phaseOrder;

    @DecimalMin(value = "0.0")
    private Double input;

    @DecimalMin(value = "0.0")
    private Double output;

    @Enumerated(EnumType.STRING)
    @Column(name = "output_unit")
    private UnitMeasurement outputUnit;

    @Column(name = "estimated_hours")
    private Double estimatedHours;

    @Column(name = "is_ready")
    @NotNull
    private Boolean isReady;

    @Column(name = "creation_date")
    @NotNull
    private OffsetDateTime creationDate;

    public List<MaterialType> getRequiredMaterials() {
        return REQUIRED_MATERIALS.getOrDefault(this.getPhase(), List.of());
    }

    public boolean isComplete() {
        return this.getInput() != null
                && this.getOutput() != null
                && this.getOutputUnit() != null
                && this.getEstimatedHours() != null;
    }

    /**
     * Obtiene la siguiente fase del producto en el orden correcto
     * 
     * @return Optional con la siguiente ProductPhase, o empty si no hay siguiente fase
     */
    public Optional<ProductPhase> getNextPhase() {
        if (this.product == null || this.product.getPhases() == null) {
            return Optional.empty();
        }
        
        List<ProductPhase> phases = this.product.getPhases().stream()
                .sorted(Comparator.comparing(ProductPhase::getPhaseOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        
        int currentIndex = phases.indexOf(this);
        if (currentIndex >= 0 && currentIndex < phases.size() - 1) {
            return Optional.of(phases.get(currentIndex + 1));
        }
        return Optional.empty();
    }
    
    /**
     * Obtiene la fase anterior del producto en el orden correcto
     * 
     * @return Optional con la ProductPhase anterior, o empty si no hay fase anterior
     */
    public Optional<ProductPhase> getPreviousPhase() {
        if (this.product == null || this.product.getPhases() == null) {
            return Optional.empty();
        }
        
        List<ProductPhase> phases = this.product.getPhases().stream()
                .sorted(Comparator.comparing(ProductPhase::getPhaseOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        
        int currentIndex = phases.indexOf(this);
        if (currentIndex > 0) {
            return Optional.of(phases.get(currentIndex - 1));
        }
        return Optional.empty();
    }
    
    /**
     * Valida que el output de esta fase coincida con el input de la siguiente fase.
     * Lanza BadRequestException si no coinciden.
     * 
     * @throws jakarta.persistence.PersistenceException si hay problemas de acceso a datos
     */
    public void validateOutputMatchesNextPhaseInput() {
        if (this.output == null || this.outputUnit == null) {
            return; // No validar si no está completo
        }
        
        Optional<ProductPhase> nextPhaseOpt = getNextPhase();
        if (nextPhaseOpt.isPresent()) {
            ProductPhase nextPhase = nextPhaseOpt.get();
            if (nextPhase.getInput() != null && !this.output.equals(nextPhase.getInput())) {
                throw new com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException(
                    String.format("El output de la fase %s (%.2f %s) no coincide con el input de la siguiente fase %s (%.2f %s). " +
                            "El output debe ser igual al input de la siguiente fase.",
                        this.phase, this.output, this.outputUnit,
                        nextPhase.getPhase(), nextPhase.getInput(), 
                        nextPhase.getOutputUnit() != null ? nextPhase.getOutputUnit() : "N/A"));
            }
            // Validar también que las unidades coincidan
            if (nextPhase.getOutputUnit() != null && !this.outputUnit.equals(nextPhase.getOutputUnit())) {
                throw new com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException(
                    String.format("La unidad de medida del output de la fase %s (%s) no coincide con la unidad del input de la siguiente fase %s (%s).",
                        this.phase, this.outputUnit,
                        nextPhase.getPhase(), nextPhase.getOutputUnit()));
            }
        }
    }

    // LÓGICA DE INICIALIZACIÓN:
    @PrePersist
    @PreUpdate
    public void setPhaseOrder() {
        if (this.phase != null) this.phaseOrder = this.phase.getOrder();
    }
}
