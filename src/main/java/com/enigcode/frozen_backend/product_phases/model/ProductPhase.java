package com.enigcode.frozen_backend.product_phases.model;

import com.enigcode.frozen_backend.materials.model.MeasurementUnit;
import com.enigcode.frozen_backend.products.model.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "product_phases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPhase {

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

    @NotNull
    private Double output;

    @Enumerated(EnumType.STRING)
    @Column(name = "output_unit")
    @NotNull
    private MeasurementUnit outputUnit;

    @Column(name = "estimated_hours")
    private Double estimatedHours;

    @Column(name = "creation_date")
    @NotNull
    private OffsetDateTime creationDate;
}
