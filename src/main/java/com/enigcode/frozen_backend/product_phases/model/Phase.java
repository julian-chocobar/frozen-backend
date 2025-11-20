package com.enigcode.frozen_backend.product_phases.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum Phase {
    // Orden(Orden, EsTiempoActivo)
    MOLIENDA(1, true),
    MACERACION(2, true),
    FILTRACION(3, true),
    COCCION(4, true),
    FERMENTACION(5, false), // <--- FALSE (Pasivo)
    MADURACION(6, false),   // <--- FALSE (Pasivo)
    GASIFICACION(7, true),
    DESALCOHOLIZACION(8, true)
    ,ENVASADO(9, true);

    private final Integer order;
    private final Boolean isTimeActive;

    Phase(Integer order, Boolean isTimeActive) {
        this.order = order;
        this.isTimeActive = isTimeActive;
    }

    public boolean comesBefore(Phase other) {
        return this.order < other.order;
    }

    public Optional<Phase> next() { // Cambiar a devolver Optional
        return Arrays.stream(values())
                .filter(p -> p.order == this.order + 1)
                .findFirst();
    }

}
