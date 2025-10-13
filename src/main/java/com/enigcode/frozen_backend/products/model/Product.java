package com.enigcode.frozen_backend.products.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.enigcode.frozen_backend.materials.model.MeasurementUnit;
import com.enigcode.frozen_backend.packagings.model.Packaging;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "packaging_id_standard")
    @NotNull
    private Packaging packaging;

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = false,
            fetch = FetchType.LAZY
    )
    @OrderBy("id ASC")
    private List<ProductPhase> phases = new ArrayList<>();

    @Column(name = "is_active")
    @NotNull
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_ready")
    @NotNull
    @Builder.Default
    private Boolean isReady= false;

    @Column(name = "is_alcoholic")
    @NotNull
    @Builder.Default
    private Boolean isAlcoholic = true;

    @Column(name = "creation_date")
    @NotNull
    private OffsetDateTime creationDate;

    public void toggleActive(){
        this.isActive = !this.isActive;
    }

    public List<Phase> getApplicablePhases() {
        List<Phase> phases = new ArrayList<>(List.of(
                Phase.MOLIENDA,
                Phase.MACERACION,
                Phase.FILTRACION,
                Phase.COCCION,
                Phase.FERMENTACION,
                Phase.MADURACION,
                Phase.GASIFICACION,
                Phase.ENVASADO
        ));

        if (!this.isAlcoholic) {
            phases.add(7, Phase.DESALCOHOLIZACION);
        }
        return phases;
    }
}
