package com.enigcode.frozen_backend.packagings.model;

import java.time.OffsetDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "packagings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Packaging {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "packagings_gen")
    @SequenceGenerator(name = "packagings_gen", sequenceName = "packagings_seq", allocationSize = 1)
    private Long id;

    @NotNull
    private String name;

    private Double quantity;

    @Column(name = "is_active")
    @NotNull
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "creation_date")
    @NotNull
    private OffsetDateTime creationDate;

}
