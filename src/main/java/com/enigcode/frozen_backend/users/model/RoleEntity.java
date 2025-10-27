package com.enigcode.frozen_backend.users.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_gen")
    @SequenceGenerator(name = "role_gen", sequenceName = "role_seq", allocationSize = 1)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String name;

    @Column(length = 500)
    private String description;

}