package com.enigcode.frozen_backend.movements.model;

import com.enigcode.frozen_backend.materials.model.Material;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
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

    /**
     * Usuario que creó el movimiento
     */
    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    /**
     * Usuario que está ejecutando el movimiento (solo para movimientos en proceso)
     */
    @Column(name = "in_progress_by_user_id")
    private Long inProgressByUserId;

    @Column(name = "taken_at")
    private LocalDateTime takenAt; // Cuándo se tomó la tarea

    /**
     * Usuario que completó/ejecutó el movimiento (solo para movimientos
     * completados)
     */
    @Column(name = "completed_by_user_id")
    private Long completedByUserId;

    /**
     * Estado del movimiento
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Builder.Default
    private MovementStatus status = MovementStatus.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @NotNull
    private MovementType type;

    /**
     * Fecha de creación del movimiento
     */
    @Column(name = "creation_date")
    @NotNull
    @Builder.Default
    private OffsetDateTime creationDate = OffsetDateTime.now();

    /**
     * Fecha de realización/completado del movimiento
     */
    @Column(name = "realization_date")
    private OffsetDateTime realizationDate;

    @NotNull
    private Double stock;

    @Size(max = 255)
    private String reason;

    /**
     * Marca el movimiento como completado por un usuario específico
     */
    public void completeMovement(Long completedByUserId) {
        this.status = MovementStatus.COMPLETADO;
        this.completedByUserId = completedByUserId;
        this.realizationDate = OffsetDateTime.now();
    }

    // Mantener compatibilidad con código existente
    @Column(name = "id_usuario")
    private Long idUser;

}