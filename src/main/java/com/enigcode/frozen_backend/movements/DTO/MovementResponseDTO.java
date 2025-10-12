package com.enigcode.frozen_backend.movements.DTO;

import java.time.OffsetDateTime;
import lombok.*;
import com.enigcode.frozen_backend.movements.model.MovementType;

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
    
    private String materialType;

    private String reason;
}
