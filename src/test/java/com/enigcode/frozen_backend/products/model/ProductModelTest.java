package com.enigcode.frozen_backend.products.model;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class ProductModelTest {

    @Test
    void getProductPhasesWithOutEnvasado_excludesEnvasado() {
        Product p = Product.builder().id(1L).name("X").isAlcoholic(true).creationDate(OffsetDateTime.now()).build();
        var phases = new ArrayList<com.enigcode.frozen_backend.product_phases.model.ProductPhase>();
        phases.add(com.enigcode.frozen_backend.product_phases.model.ProductPhase.builder().phase(Phase.ENVASADO).build());
        phases.add(com.enigcode.frozen_backend.product_phases.model.ProductPhase.builder().phase(Phase.MOLIENDA).build());
        p.setPhases(phases);

        var filtered = p.getProductPhasesWithOutEnvasado();
        assertThat(filtered).allMatch(pp -> pp.getPhase() != Phase.ENVASADO);
    }

    @Test
    void getApplicablePhases_includesDesalcohol_whenNotAlcoholic() {
        Product p = Product.builder().id(2L).name("Y").isAlcoholic(false).creationDate(OffsetDateTime.now()).build();
        var list = p.getApplicablePhases();
        assertThat(list).contains(Phase.DESALCOHOLIZACION);
    }
}
