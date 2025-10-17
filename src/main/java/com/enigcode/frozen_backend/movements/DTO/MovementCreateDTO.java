package com.enigcode.frozen_backend.movements.DTO;

import com.enigcode.frozen_backend.movements.model.MovementType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovementCreateDTO {
    @NotNull(message = "El movimiento debe tener un tipo")
    private MovementType type;

    @NotNull(message = "El movimiento debe haber involucrado cierta cantidad de stock")
    @DecimalMin(value = "0.0", inclusive = false, message = "El stock no puede ser menor a 0")
    private Double stock;

    @NotNull(message = "El movimiento debe estar asociado a un material")
    private Long materialId;

    @Size(max = 255, message = "El motivo no puede tener mas de 255 caracteres")
    private String reason;
}