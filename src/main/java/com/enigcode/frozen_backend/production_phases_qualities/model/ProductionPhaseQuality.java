package com.enigcode.frozen_backend.production_phases_qualities.model;

import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import com.enigcode.frozen_backend.quality_parameters.model.QualityParameter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "production_phases_qualities",
        uniqueConstraints = {
        @UniqueConstraint(
                name = "uq_production_phase_quality_parameter",
                columnNames = {"id_production_phase", "id_quality_parameter"})
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

    @PrePersist
    public void setRealizationDate(){if(realizationDate == null) realizationDate = OffsetDateTime.now();}
}
