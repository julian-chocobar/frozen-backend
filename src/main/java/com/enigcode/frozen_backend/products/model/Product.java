package com.enigcode.frozen_backend.products.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_gen")
    @SequenceGenerator(name = "products_gen", sequenceName = "products_seq", allocationSize = 1)
    private Long id;

    @NotNull
    private String name;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("phaseOrder ASC")
    private List<ProductPhase> phases;

    @NotNull
    @Column(name = "standard_quantity")
    @DecimalMin(value = "0.0")
    private Double standardQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_measurement")
    @NotNull
    private UnitMeasurement unitMeasurement;

    @Column(name = "is_active")
    @NotNull
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_ready")
    @NotNull
    @Builder.Default
    private Boolean isReady = false;

    @Column(name = "is_alcoholic")
    @NotNull
    @Builder.Default
    private Boolean isAlcoholic = true;

    @Column(name = "creation_date")
    @NotNull
    private OffsetDateTime creationDate;

    public void toggleActive() {
        this.isActive = !this.isActive;
    }

    public List<Phase> getApplicablePhases() {
        List<Phase> phases = new ArrayList<>(List.of(
                Phase.MOLIENDA,      // 1
                Phase.MACERACION,    // 2
                Phase.FILTRACION,    // 3
                Phase.COCCION,       // 4
                Phase.FERMENTACION,  // 5
                Phase.MADURACION,    // 6
                Phase.GASIFICACION   // 7
        ));

        if (!this.isAlcoholic) {
            // Agregar DESALCOHOLIZACION antes de ENVASADO
            phases.add(Phase.DESALCOHOLIZACION); // 8
        }
        
        phases.add(Phase.ENVASADO); // 8 (alcohólico) o 9 (no alcohólico)
        
        return phases;
    }

    public void markAsReady() {
        this.isReady = Boolean.TRUE;
    }

    public void markAsNotReady() {
        this.isReady = Boolean.FALSE;
    }

    public List<ProductPhase> getProductPhasesWithOutEnvasado() {
        List<ProductPhase> filteredPhases = new ArrayList<>();
        for (ProductPhase pp : this.phases) {
            if (pp.getPhase() != Phase.ENVASADO) {
                filteredPhases.add(pp);
            }
        }
        return filteredPhases;
    }
}
