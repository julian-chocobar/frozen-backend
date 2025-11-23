package com.enigcode.frozen_backend.movements.specification;

import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.enigcode.frozen_backend.movements.DTO.MovementFilterDTO;
import com.enigcode.frozen_backend.movements.model.Movement;
import com.enigcode.frozen_backend.movements.model.MovementType;
import com.enigcode.frozen_backend.movements.model.MovementStatus;
import com.enigcode.frozen_backend.movements.repository.MovementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MovementSpecificationTest {

    @Autowired
    private MovementRepository movementRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Test
    void createFilter_filtersByMaterialAndType() {
        Material m1 = new Material();
        m1.setName("SpecMat1");
        m1 = materialRepository.save(m1);

        Material m2 = new Material();
        m2.setName("SpecMat2");
        m2 = materialRepository.save(m2);

        Movement a = Movement.builder().material(m1).type(MovementType.INGRESO).status(MovementStatus.PENDIENTE).stock(1.0).build();
        Movement b = Movement.builder().material(m2).type(MovementType.EGRESO).status(MovementStatus.PENDIENTE).stock(2.0).build();

        movementRepository.saveAll(List.of(a, b));

        MovementFilterDTO filter = new MovementFilterDTO();
        filter.setMaterialId(m1.getId());
        filter.setType(MovementType.INGRESO);

        var page = movementRepository.findAll(MovementSpecification.createFilter(filter), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getMaterial().getId()).isEqualTo(m1.getId());
    }
}
