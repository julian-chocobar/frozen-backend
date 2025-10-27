package com.enigcode.frozen_backend.system_configurations.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "working_days")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkingDay {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "working_days_gen")
    @SequenceGenerator(name = "working_days_gen", sequenceName = "working_days_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_system_configuration")
    @NotNull
    private SystemConfiguration systemConfiguration;

    @Enumerated(EnumType.STRING)
    @NotNull
    private DayOfWeek dayOfWeek;

    private Boolean isWorkingDay;

    private LocalTime openingHour;

    private LocalTime closingHour;

}
