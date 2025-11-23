package com.enigcode.frozen_backend.materials.specification;

import com.enigcode.frozen_backend.materials.DTO.MaterialFilterDTO;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MaterialSpecificationTest {

    @Autowired
    private MaterialRepository materialRepository;

    @Test
    void filterByName_supplier_type_and_isActive() {
        Material m1 = new Material();
        m1.setName("Malta A");
        m1.setType(MaterialType.MALTA);
        m1.setUnitMeasurement(UnitMeasurement.KG);
        m1.setThreshold(1.0);
        m1.setStock(10.0);
        m1.setCreationDate(OffsetDateTime.now(ZoneOffset.UTC));
        m1.setSupplier("Proveedor1");
        m1.setIsActive(true);

        Material m2 = new Material();
        m2.setName("Lupulo B");
        m2.setType(MaterialType.LUPULO);
        m2.setUnitMeasurement(UnitMeasurement.KG);
        m2.setThreshold(1.0);
        m2.setStock(5.0);
        m2.setCreationDate(OffsetDateTime.now(ZoneOffset.UTC));
        m2.setSupplier("Proveedor2");
        m2.setIsActive(false);

        materialRepository.save(m1);
        materialRepository.save(m2);

        MaterialFilterDTO filter = new MaterialFilterDTO();
        filter.setName("Malta");
        filter.setSupplier("Proveedor1");
        filter.setType(MaterialType.MALTA);
        filter.setIsActive(true);

        var page = materialRepository.findAll(MaterialSpecification.createFilter(filter), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getName()).contains("Malta");
    }
}
