package com.enigcode.frozen_backend.production_materials.model;

import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "production_materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "production_materials_gen")
    @SequenceGenerator(name = "production_materials_gen", sequenceName = "production_materials_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_material")
    @NotNull
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_production_phase")
    @NotNull
    private ProductionPhase productionPhase;

    @NotNull
    @DecimalMin(value = "0.0")
    private Double quantity;

    @Column(name = "creation_date", updatable = false)
    @NotNull
    private OffsetDateTime creationDate;

    // LÓGICA DE INICIALIZACIÓN:
    @PrePersist
    public void setCreationDate() {
        if (this.creationDate == null) this.creationDate = OffsetDateTime.now();
    }
}
