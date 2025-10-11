package materials.DTO;

import lombok.*;
import materials.model.MeasurementUnit;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialDTO {
    private String name;
    private String supplier;
    private Double value;
    private Double stock;
    private MeasurementUnit unitMeasurement;
    private Double threshold;
}
