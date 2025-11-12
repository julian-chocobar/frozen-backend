package com.enigcode.frozen_backend.production_phases_qualities.model;

import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import com.enigcode.frozen_backend.quality_parameters.model.QualityParameter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "production_phases_qualities", uniqueConstraints = {
        @UniqueConstraint(name = "uq_production_phase_quality_parameter_version", columnNames = { "id_production_phase",
                "id_quality_parameter", "version" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionPhaseQuality {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "production_phases_qualities_gen")
    @SequenceGenerator(name = "production_phases_qualities_gen", sequenceName = "production_phases_qualities_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_quality_parameter")
    @NotNull
    private QualityParameter qualityParameter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_production_phase")
    @NotNull
    private ProductionPhase productionPhase;

    @Size(max = 55)
    @NotNull
    private String value;

    @NotNull
    private Boolean isApproved;

    private OffsetDateTime realizationDate;

    /**
     * Versión del parámetro de calidad.
     * Cada vez que se requiere ajuste de la fase, se crea una nueva versión.
     * La versión 1 es la medición inicial, versión 2 después del primer ajuste,
     * etc.
     */
    @NotNull
    @Builder.Default
    private Integer version = 1;

    /**
     * Indica si este parámetro está activo o es histórico.
     * Solo los parámetros activos se consideran en las evaluaciones.
     * Los históricos se mantienen para trazabilidad.
     */
    @NotNull
    @Builder.Default
    private Boolean isActive = true;

    @PrePersist
    public void setRealizationDate() {
        if (realizationDate == null)
            realizationDate = OffsetDateTime.now();
    }

    /**
     * Marca este parámetro como histórico (no activo)
     */
    public void markAsHistorical() {
        this.isActive = false;
    }

    /**
     * Marca este parámetro como activo
     */
    public void markAsActive() {
        this.isActive = true;
    }
}
