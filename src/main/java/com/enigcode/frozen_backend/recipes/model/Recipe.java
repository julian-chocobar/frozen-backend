package com.enigcode.frozen_backend.recipes.model;

import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "recipes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_product_phase", "id_material"}, name = "UK_recipe_phase_material")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recipes_gen")
    @SequenceGenerator(name = "recipes_gen", sequenceName = "recipes_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_product_phase")
    @NotNull
    private ProductPhase productPhase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_material")
    @NotNull
    private Material material; 

    @NotNull
    @Min(value = 0)
    private Integer quantity;

    @Column(name = "creation_date")
    @NotNull
    private OffsetDateTime creationDate;
}
