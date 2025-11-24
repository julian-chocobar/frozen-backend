package com.enigcode.frozen_backend.batches.repository;

import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BatchRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BatchRepository batchRepository;

    @Test
    void findAllStartingToday_returnsScheduledBatches() {
        Material m1 = Material.builder().name("Mat1").type(MaterialType.ENVASE).unitMeasurement(UnitMeasurement.UNIDAD).stock(10.0).threshold(1.0).creationDate(OffsetDateTime.now()).build();
        em.persist(m1);

        Packaging p = Packaging.builder().name("PKG1").packagingMaterial(m1).labelingMaterial(m1).quantity(1.0).unitMeasurement(UnitMeasurement.UNIDAD).creationDate(OffsetDateTime.now()).build();
        em.persist(p);

        Batch b = Batch.builder().packaging(p).status(com.enigcode.frozen_backend.batches.model.BatchStatus.PENDIENTE).quantity(1).creationDate(OffsetDateTime.now()).plannedDate(OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS)).build();
        em.persist(b);

        em.flush();

        OffsetDateTime start = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS);
        OffsetDateTime end = start.plusDays(1);

        List<Batch> results = batchRepository.findAllStartingToday(start, end);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getId()).isEqualTo(b.getId());
    }
}
