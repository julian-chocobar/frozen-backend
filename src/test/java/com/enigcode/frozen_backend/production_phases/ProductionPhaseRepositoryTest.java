package com.enigcode.frozen_backend.production_phases;

import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhaseStatus;
import com.enigcode.frozen_backend.production_phases.repository.ProductionPhaseRepository;
import com.enigcode.frozen_backend.sectors.model.Sector;
import com.enigcode.frozen_backend.sectors.model.SectorType;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.users.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ProductionPhaseRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ProductionPhaseRepository repo;

    @Test
    public void findPreviousPhase_returnsPrevious_whenExists() {
        // create minimal material and packaging required by Batch
        Material mat = Material.builder()
                .name("Mat")
                .type(MaterialType.OTROS)
                .unitMeasurement(UnitMeasurement.KG)
                .stock(100.0)
                .threshold(1.0)
                .creationDate(OffsetDateTime.now())
                .build();
        em.persist(mat);

        Packaging pkg = Packaging.builder()
                .name("PKG")
                .packagingMaterial(mat)
                .labelingMaterial(mat)
                .quantity(1.0)
                .unitMeasurement(UnitMeasurement.UNIDAD)
                .creationDate(OffsetDateTime.now())
                .build();
        em.persist(pkg);

        Batch batch = Batch.builder()
                .code("BATCH1")
                .packaging(pkg)
                .status(null)
                .quantity(10)
                .creationDate(OffsetDateTime.now())
                .plannedDate(OffsetDateTime.now())
                .isActive(true)
                .build();
        em.persist(batch);

        ProductionPhase p1 = ProductionPhase.builder()
                .batch(batch)
                .phase(Phase.MOLIENDA)
                .phaseOrder(1)
                .status(ProductionPhaseStatus.PENDIENTE)
                .build();
        em.persist(p1);

        ProductionPhase p2 = ProductionPhase.builder()
                .batch(batch)
                .phase(Phase.MACERACION)
                .phaseOrder(2)
                .status(ProductionPhaseStatus.PENDIENTE)
                .build();
        em.persist(p2);

        em.flush();

        ProductionPhase prev = repo.findPreviousPhase(batch, 2);
        assertThat(prev).isNotNull();
        assertThat(prev.getId()).isEqualTo(p1.getId());
    }

    @Test
    public void existsByIdAndSector_Supervisor_Id_checksSupervisorRelation() {
        User supervisor = User.builder()
                .username("sup")
                .password("x")
                .name("Sup")
                .creationDate(OffsetDateTime.now())
                .build();
        em.persist(supervisor);

        Sector sector = Sector.builder()
                .name("S")
                .supervisor(supervisor)
                .type(SectorType.PRODUCCION)
                .phase(Phase.MOLIENDA)
                .isActive(true)
                .creationDate(OffsetDateTime.now())
                .build();
        em.persist(sector);

        // Create a production phase linked to sector
        ProductionPhase phase = ProductionPhase.builder()
                .sector(sector)
                .status(ProductionPhaseStatus.PENDIENTE)
                .phase(Phase.MOLIENDA)
                .phaseOrder(1)
                .build();
        em.persist(phase);
        em.flush();

        boolean exists = repo.existsByIdAndSector_Supervisor_Id(phase.getId(), supervisor.getId());
        assertThat(exists).isTrue();

        boolean notExists = repo.existsByIdAndSector_Supervisor_Id(phase.getId(), supervisor.getId() + 999L);
        assertThat(notExists).isFalse();
    }
}
