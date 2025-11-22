package com.enigcode.frozen_backend.sectors.repository;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.sectors.model.Sector;
import com.enigcode.frozen_backend.sectors.model.SectorType;
import com.enigcode.frozen_backend.sectors.repository.SectorRepository;
import com.enigcode.frozen_backend.users.model.Role;
import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SectorRepositoryTest {

    @Autowired
    SectorRepository sectorRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void findAvailableProductionSectorsByPhase_filtersAndOrders() {
        User supervisor = User.builder()
                .username("sup1")
                .password("pass")
                .name("Sup")
                .creationDate(OffsetDateTime.now())
                .build();
        supervisor.getRoles().add(Role.SUPERVISOR_DE_PRODUCCION);
        supervisor = userRepository.saveAndFlush(supervisor);

        Sector s1 = Sector.builder()
                .name("s1")
                .supervisor(supervisor)
                .type(SectorType.PRODUCCION)
                .phase(Phase.MOLIENDA)
                .isActive(true)
                .productionCapacity(10.0)
                .actualProduction(2.0)
                .creationDate(OffsetDateTime.now())
                .build();

        Sector s2 = Sector.builder()
                .name("s2")
                .supervisor(supervisor)
                .type(SectorType.PRODUCCION)
                .phase(Phase.MOLIENDA)
                .isActive(true)
                .productionCapacity(5.0)
                .actualProduction(1.0)
                .creationDate(OffsetDateTime.now())
                .build();

        Sector s3 = Sector.builder()
                .name("s3-full")
                .supervisor(supervisor)
                .type(SectorType.PRODUCCION)
                .phase(Phase.MOLIENDA)
                .isActive(true)
                .productionCapacity(2.0)
                .actualProduction(2.0)
                .creationDate(OffsetDateTime.now())
                .build();

        sectorRepository.saveAll(List.of(s1, s2, s3));

        List<Sector> available = sectorRepository.findAvailableProductionSectorsByPhase(Phase.MOLIENDA);

        // s3 is at capacity -> excluded; ordering asc by actualProduction -> s2 (1.0), s1 (2.0)
        assertThat(available).hasSize(2);
        assertThat(available.get(0).getName()).isEqualTo("s2");
        assertThat(available.get(1).getName()).isEqualTo("s1");
    }
}
