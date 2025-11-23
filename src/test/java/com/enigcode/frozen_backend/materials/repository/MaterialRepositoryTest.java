package com.enigcode.frozen_backend.materials.repository;

import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.materials.model.WarehouseZone;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MaterialRepositoryTest {

    @Autowired
    private MaterialRepository materialRepository;

    @Test
    void findTop10ByNameContainingIgnoreCase_returnsResults() {
        Material m = new Material();
        m.setName("Plástico");
        m.setType(MaterialType.OTROS);
        m.setUnitMeasurement(UnitMeasurement.KG);
        m.setThreshold(1.0);
        m.setStock(5.0);
        m.setCreationDate(OffsetDateTime.now(ZoneOffset.UTC));
        materialRepository.save(m);

        List<Material> results = materialRepository.findTop10ByNameContainingIgnoreCase("Pl");
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getName()).containsIgnoringCase("Pl");
    }

    @Test
    void findWarehouseSectionsByZone_and_countByWarehouseZone() {
        Material m = new Material();
        m.setName("Malta A");
        m.setType(MaterialType.MALTA);
        m.setUnitMeasurement(UnitMeasurement.KG);
        m.setThreshold(1.0);
        m.setStock(10.0);
        m.setCreationDate(OffsetDateTime.now(ZoneOffset.UTC));
        m.setWarehouseZone(WarehouseZone.MALTA);
        m.setWarehouseSection("S1");
        materialRepository.save(m);

        List<String> sections = materialRepository.findWarehouseSectionsByZone(WarehouseZone.MALTA);
        Long count = materialRepository.countByWarehouseZone(WarehouseZone.MALTA);

        assertThat(sections).contains("S1");
        assertThat(count).isGreaterThanOrEqualTo(1L);
    }

    @Test
    void existsByCode_returnsTrueWhenPresent() {
        Material m = new Material();
        m.setName("Envase X");
        m.setCode("ENV-100");
        m.setType(MaterialType.ENVASE);
        m.setUnitMeasurement(UnitMeasurement.UNIDAD);
        m.setThreshold(1.0);
        m.setStock(2.0);
        m.setCreationDate(OffsetDateTime.now(ZoneOffset.UTC));
        materialRepository.save(m);

        boolean exists = materialRepository.existsByCode("ENV-100");
        assertThat(exists).isTrue();
    }
}
