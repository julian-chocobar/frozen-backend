package movements.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "materials_seq")
    @SequenceGenerator(name = "materials_seq", sequenceName = "materials_seq", allocationSize = 1)
    private Long id;

    @Column(name = "id_material")
    @NotNull
    private Long idMaterial;

    //private Long idUser;

    @Enumerated(EnumType.STRING)
    @NotNull
    private MovementType type;

    @Column(name = "realization_date")
    @NotNull
    private OffsetDateTime realizationDate;

    @NotNull
    private Double stock;

    @Size(max = 255)
    private String reason;

}
