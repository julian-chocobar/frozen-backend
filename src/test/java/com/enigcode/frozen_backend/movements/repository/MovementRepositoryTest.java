package com.enigcode.frozen_backend.movements.repository;

import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.enigcode.frozen_backend.movements.model.Movement;
import com.enigcode.frozen_backend.movements.model.MovementStatus;
import com.enigcode.frozen_backend.movements.model.MovementType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MovementRepositoryTest {

    @Autowired
    private MovementRepository movementRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Test
    void findByStatusOrderByCreationDateAsc_returnsOrdered() {
        Material m = new Material();
        m.setName("TestMat");
        m = materialRepository.save(m);

        Movement first = Movement.builder()
                .material(m)
                .type(MovementType.INGRESO)
                .stock(1.0)
                .status(MovementStatus.PENDIENTE)
                .creationDate(OffsetDateTime.now(ZoneOffset.UTC).minusDays(2))
                .build();

        Movement second = Movement.builder()
                .material(m)
                .type(MovementType.INGRESO)
                .stock(2.0)
                .status(MovementStatus.PENDIENTE)
                .creationDate(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1))
                .build();

        movementRepository.saveAll(List.of(second, first));

        var page = movementRepository.findByStatusOrderByCreationDateAsc(MovementStatus.PENDIENTE, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
        List<Movement> content = page.getContent();
        // First element must be the oldest (first)
        assertThat(content.get(0).getCreationDate()).isBefore(content.get(1).getCreationDate());
    }
}
