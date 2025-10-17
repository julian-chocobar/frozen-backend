package com.enigcode.frozen_backend.batches.model;

import com.enigcode.frozen_backend.packagings.model.Packaging;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "batches_gen")
    @SequenceGenerator(name = "batches_gen", sequenceName = "batches_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_packaging")
    @NotNull
    private Packaging packaging;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @NotNull
    private BatchStatus status;

    @NotNull
    @Min(value = 0)
    private Integer quantity;

    @Column(name = "planned_date")
    @NotNull
    private OffsetDateTime plannedDate;

    @Column(name = "creation_date")
    @NotNull
    private OffsetDateTime creationDate;

    @Column(name = "start_date")
    private OffsetDateTime startDate;

    @Column(name = "completed_date")
    private OffsetDateTime completedDate;
}
