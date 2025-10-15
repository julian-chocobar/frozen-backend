package com.enigcode.frozen_backend.recipes.repository;

import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.recipes.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    boolean existsByMaterial_Type(MaterialType type);
}
