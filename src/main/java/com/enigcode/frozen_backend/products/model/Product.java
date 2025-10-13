package com.enigcode.frozen_backend.products.model;

import java.time.OffsetDateTime;

import com.enigcode.frozen_backend.materials.model.MeasurementUnit;
import com.enigcode.frozen_backend.packagings.model.Packaging;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_measurement")
    @NotNull
    private MeasurementUnit measurementUnit;

    @Column(name = "is_active")
    @NotNull
    @Builder.Default
    private Boolean isActive = true;

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

}
