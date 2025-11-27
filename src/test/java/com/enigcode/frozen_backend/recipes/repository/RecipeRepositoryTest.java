package com.enigcode.frozen_backend.recipes.repository;

import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import com.enigcode.frozen_backend.product_phases.repository.ProductPhaseRepository;
import com.enigcode.frozen_backend.products.model.Product;
import com.enigcode.frozen_backend.products.repository.ProductRepository;
import com.enigcode.frozen_backend.recipes.model.Recipe;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RecipeRepositoryTest {

    @Autowired
    RecipeRepository recipeRepository;

    @Autowired
    MaterialRepository materialRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductPhaseRepository productPhaseRepository;

    @Test
    void findByProductPhase_ProductId_and_findByProductPhase_and_existsChecks() {
        Product product = Product.builder()
                .name("TestProd")
                .standardQuantity(1.0)
                .unitMeasurement(UnitMeasurement.UNIDAD)
                .creationDate(OffsetDateTime.now())
                .build();
        product = productRepository.saveAndFlush(product);

        ProductPhase phase = ProductPhase.builder()
                .product(product)
                .phase(Phase.MOLIENDA)
                .isReady(false)
                .creationDate(OffsetDateTime.now())
                .build();
        phase = productPhaseRepository.saveAndFlush(phase);

        Material mat = Material.builder()
                .name("Malta")
                .type(MaterialType.MALTA)
                .unitMeasurement(UnitMeasurement.KG)
                .threshold(1.0)
                .stock(10.0)
                .creationDate(OffsetDateTime.now())
                .build();
        mat = materialRepository.saveAndFlush(mat);

        Recipe r = Recipe.builder()
                .productPhase(phase)
                .material(mat)
                .quantity(2.5)
                .creationDate(OffsetDateTime.now())
                .build();
        recipeRepository.saveAndFlush(r);

        List<Recipe> byProd = recipeRepository.findByProductPhase_ProductId(product.getId());
        List<Recipe> byPhase = recipeRepository.findByProductPhase(phase);

        assertThat(byProd).isNotEmpty();
        assertThat(byPhase).isNotEmpty();

        boolean existsType = recipeRepository.existsByMaterial_Type(MaterialType.MALTA);
        boolean existsByPhaseAndType = recipeRepository.existsByProductPhaseIdAndMaterial_Type(phase.getId(), MaterialType.MALTA);

        assertThat(existsType).isTrue();
        assertThat(existsByPhaseAndType).isTrue();
    }
}
