package com.enigcode.frozen_backend.materials.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;

@Entity
@Table(name = "materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "materials_gen")
    @SequenceGenerator(name = "materials_gen", sequenceName = "materials_seq", allocationSize = 1)
    private Long id;

    @Column(unique = true)
    private String code;

    @NotNull
    private String name;

    @NotNull
    private MaterialType type;

    private String supplier;

    private Double value;

    @NotNull
    @ColumnDefault("0.0")
    private Double stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_measurement")
    @NotNull
    private MeasurementUnit unitMeasurement;

    @NotNull
    private Double threshold;

    @Column(name = "is_active")
    @NotNull
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_update_date")
    private OffsetDateTime lastUpdateDate;

    @Column(name = "creation_date")
    @NotNull
    private OffsetDateTime creationDate;

    public void toggleActive(){
        this.isActive = !this.isActive;
    }

    public void reduceStock(Double stock) {
        this.stock -= stock;
    }

    public void increaseStock(Double stock) {
        this.stock += stock;
    }
}
