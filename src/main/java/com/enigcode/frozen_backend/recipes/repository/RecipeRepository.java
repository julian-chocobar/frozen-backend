package com.enigcode.frozen_backend.recipes.repository;

import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import com.enigcode.frozen_backend.recipes.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    boolean existsByMaterial_Type(MaterialType type);

    List<Recipe> findByProductPhase_ProductId(Long id);

    List<Recipe> findByProductPhase(ProductPhase productPhase);

    boolean existsByProductPhaseIdAndMaterial_Type(Long productPhaseId, MaterialType type);
}
