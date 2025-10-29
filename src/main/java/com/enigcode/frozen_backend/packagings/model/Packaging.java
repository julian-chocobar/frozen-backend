package com.enigcode.frozen_backend.packagings.model;

import java.time.OffsetDateTime;

import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "packagings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Packaging {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "packagings_gen")
    @SequenceGenerator(name = "packagings_gen", sequenceName = "packagings_seq", allocationSize = 1)
    private Long id;

    @NotNull
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_packaging_material")
    @NotNull
    private Material packagingMaterial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_labeling_material")
    @NotNull
    private Material labelingMaterial;

    @NotNull
    @DecimalMin(value = "0.0")
    private Double quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_measurement")
    @NotNull
    private UnitMeasurement unitMeasurement;

    @Column(name = "is_active")
    @NotNull
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "creation_date")
    @NotNull
    private OffsetDateTime creationDate;

    public void toggleActive() {
        this.isActive = !this.isActive;
    }

}
