package materials.DTO;

import lombok.*;
import materials.model.MeasurementUnit;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialDTO {
    private Long id;
    private String name;
    private String supplier;
    private Double value;
    private Double stock;
    private MeasurementUnit unitMeasurement;
    private Double threshold;
}
