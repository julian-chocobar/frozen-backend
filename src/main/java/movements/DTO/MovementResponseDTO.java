package movements.DTO;

import java.time.OffsetDateTime;
import lombok.*;
import movements.model.MovementType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovementResponseDTO {
    private Long id;
    
    private MovementType type;
    
    private OffsetDateTime realizationDate;
    
    private Double stock;
    
    private String codeMaterial;

    private String reason;
}
