package com.enigcode.frozen_backend.movements.model;

import com.enigcode.frozen_backend.materials.model.Material;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "movements_gen")
    @SequenceGenerator(name = "movements_gen", sequenceName = "movements_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_material")
    @NotNull
    private Material material;

    @Column(name = "id_usuario")
    private Long idUser;

    @Enumerated(EnumType.STRING)
    @NotNull
    private MovementType type;

    @Column(name = "realization_date")
    @NotNull
    private OffsetDateTime realizationDate;

    @NotNull
    private Double stock;

    @Size(max = 255)
    private String reason;

}