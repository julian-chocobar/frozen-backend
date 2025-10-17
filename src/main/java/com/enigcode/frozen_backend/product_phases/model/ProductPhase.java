package com.enigcode.frozen_backend.product_phases.model;

import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.products.model.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "product_phases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPhase {

    private static final Map<Phase, List<MaterialType>> REQUIRED_MATERIALS;
    static {
        Map<Phase, List<MaterialType>> map = new HashMap<>();

        // Carga de datos
        map.put(Phase.MOLIENDA, List.of(MaterialType.MALTA));
        map.put(Phase.MACERACION, List.of(MaterialType.AGUA));
        map.put(Phase.FILTRACION, List.of());
        map.put(Phase.COCCION, List.of(MaterialType.AGUA, MaterialType.LUPULO));
        map.put(Phase.FERMENTACION, List.of(MaterialType.LEVADURA));
        map.put(Phase.MADURACION, List.of());
        map.put(Phase.GASIFICACION, List.of());
        map.put(Phase.ENVASADO, List.of(MaterialType.ENVASE));

        REQUIRED_MATERIALS = map;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_phases_gen")
    @SequenceGenerator(name = "product_phases_gen", sequenceName = "product_phases_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_product")
    @NotNull
    private Product product;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Phase phase;

    @DecimalMin(value = "0.0")
    private Double input;

    @DecimalMin(value = "0.0")
    private Double output;

    @Enumerated(EnumType.STRING)
    @Column(name = "output_unit")
    private UnitMeasurement outputUnit;

    @Column(name = "estimated_hours")
    private Double estimatedHours;

    @Column(name = "is_ready")
    @NotNull
    private Boolean isReady;

    @Column(name = "creation_date")
    @NotNull
    private OffsetDateTime creationDate;

    public List<MaterialType> getRequiredMaterials() {
        return REQUIRED_MATERIALS.getOrDefault(this.getPhase(), List.of());
    }

    public boolean isComplete() {
        return this.getInput() != null
                && this.getOutput() != null
                && this.getOutputUnit() != null
                && this.getEstimatedHours() != null;
    }
}
