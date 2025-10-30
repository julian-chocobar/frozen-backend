package com.enigcode.frozen_backend.system_configurations.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "system_configurations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "system_configuration_gen")
    @SequenceGenerator(name = "system_configuration_gen", sequenceName = "system_configuration_seq", allocationSize = 1)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_system_configuration")
    @JsonManagedReference("system-config-workingdays") // ← LADO PADRE (se serializa)
    private List<WorkingDay> workingDays;

    @NotNull
    private Boolean isActive;
}
