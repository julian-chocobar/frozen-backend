package com.enigcode.frozen_backend.sectors.model;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.users.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "sectors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sectors_gen")
    @SequenceGenerator(name = "sectors_gen", sequenceName = "sectors_seq", allocationSize = 1)
    private Long id;

    @NotNull
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    @NotNull
    private User supervisor;

    @Enumerated(EnumType.STRING)
    @NotNull
    private SectorType type;

    @Enumerated(EnumType.STRING)
    private Phase phase;

    @Column(name = "production_capacity")
    private Double productionCapacity;

    @Column(name = "actual_production")
    private Double actualProduction;

    @NotNull
    private Boolean isActive;

    private Boolean isTimeActive;

    @Column(name = "creation_date")
    @NotNull
    private OffsetDateTime creationDate;

    // LÓGICA DE INICIALIZACIÓN:
    @PrePersist
    public void setActualProduction() {
        if (this.actualProduction == null) this.actualProduction = 0.0;
    }

    public void increaseActualProduction(Double quantity) {
        this.actualProduction += quantity;
    }

    public void decreaseActualProduction(Double quantity) {
        this.actualProduction -= quantity;
        if(this.actualProduction < 0.0) this.actualProduction = 0.0;
    }
}
