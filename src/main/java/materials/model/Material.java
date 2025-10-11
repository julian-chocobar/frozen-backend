package materials.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "materials_seq")
    @SequenceGenerator(name = "materials_seq", sequenceName = "materials_seq", allocationSize = 1)
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String supplier;

    @NotNull
    private Double value;

    @NotNull
    private Double stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_measurement")
    @NotNull
    private MeasurementUnit unitMeasurement;

    @NotNull
    private Double threshold;

    @Column(name = "is_active")
    @NotNull
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "creation_date")
    @NotNull
    private OffsetDateTime creationDate;
}
