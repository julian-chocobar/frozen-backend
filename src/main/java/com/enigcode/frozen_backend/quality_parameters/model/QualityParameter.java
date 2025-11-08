package com.enigcode.frozen_backend.quality_parameters.model;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "quality_parameters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityParameter {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "quality_parameters_gen")
    @SequenceGenerator(name = "quality_parameters_gen", sequenceName = "quality_parameters_seq", allocationSize = 1)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Phase phase;

    @NotNull
    private boolean isCritical;

    @Size(max = 20)
    @NotNull
    private String name;

    @Size(max = 255)
    private String description;

    private Boolean isActive;

    @PrePersist
    public void setIsActive(){if(isActive == null) isActive = true;}

    public void toggleActive() {
        this.isActive = !this.isActive;
    }
}
