package com.enigcode.frozen_backend.materials.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @ColumnDefault("0.0")
    @Builder.Default
    private Double reservedStock = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_measurement")
    @NotNull
    private UnitMeasurement unitMeasurement;

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

    // Campos de ubicación en el almacén
    @Enumerated(EnumType.STRING)
    @Column(name = "warehouse_zone")
    private WarehouseZone warehouseZone;

    @Column(name = "warehouse_section", length = 10)
    private String warehouseSection;

    @Column(name = "warehouse_level")
    @Builder.Default
    private Integer warehouseLevel = 1;

    public void toggleActive() {
        this.isActive = !this.isActive;
    }

    public void reduceStock(Double stock) {
        this.stock = BigDecimal.valueOf(this.stock - stock)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public void increaseStock(Double stock) {
        this.stock = BigDecimal.valueOf(this.stock + stock)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public void reserveStock(Double stock) {
        this.reservedStock = BigDecimal.valueOf(this.reservedStock + stock)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
        reduceStock(stock);
    }

    public void returnStock(Double stock) {
        reduceReservedStock(stock);
        increaseStock(stock);
    }

    public void reduceReservedStock(Double stock) {
        this.reservedStock = BigDecimal.valueOf(this.reservedStock - stock)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}