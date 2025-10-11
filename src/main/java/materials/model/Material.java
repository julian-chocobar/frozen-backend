package materials.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "materials")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    private String name;

    private String supplier;

    private Double value;

    private Double stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_measurement")
    private MeasurementUnit unitMeasurement;

    private Double threshold;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "creation_date")
    private OffsetDateTime creationDate;
}
