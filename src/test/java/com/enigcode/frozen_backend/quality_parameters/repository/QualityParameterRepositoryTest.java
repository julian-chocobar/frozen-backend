package com.enigcode.frozen_backend.quality_parameters.repository;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.quality_parameters.model.QualityParameter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class QualityParameterRepositoryTest {

    @Autowired
    private QualityParameterRepository repository;

    @Test
    void findByIsActiveTrueOrderByNameAsc_returnsOnlyActiveOrdered() {
        QualityParameter b = QualityParameter.builder().phase(Phase.MOLIENDA).name("B").isActive(true).isCritical(false).build();
        QualityParameter a = QualityParameter.builder().phase(Phase.MOLIENDA).name("A").isActive(true).isCritical(false).build();
        QualityParameter c = QualityParameter.builder().phase(Phase.MOLIENDA).name("C").isActive(false).isCritical(false).build();

        repository.save(b);
        repository.save(a);
        repository.save(c);

        List<QualityParameter> results = repository.findByIsActiveTrueOrderByNameAsc();
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getName()).isEqualTo("A");
        assertThat(results.get(1).getName()).isEqualTo("B");
    }

    @Test
    void findByPhaseAndIsActiveTrueOrderByNameAsc_filtersByPhase() {
        QualityParameter p1 = QualityParameter.builder().phase(Phase.FERMENTACION).name("X").isActive(true).isCritical(false).build();
        QualityParameter p2 = QualityParameter.builder().phase(Phase.COCCION).name("Y").isActive(true).isCritical(false).build();

        repository.save(p1);
        repository.save(p2);

        List<QualityParameter> res = repository.findByPhaseAndIsActiveTrueOrderByNameAsc(Phase.FERMENTACION);
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getPhase()).isEqualTo(Phase.FERMENTACION);
    }
}
