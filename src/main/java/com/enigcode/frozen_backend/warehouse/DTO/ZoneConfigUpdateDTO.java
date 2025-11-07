package com.enigcode.frozen_backend.warehouse.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneConfigUpdateDTO {

    @Min(value = 1, message = "Debe haber al menos 1 sección por fila")
    @Max(value = 20, message = "Máximo 20 secciones por fila")
    private Integer maxSectionsPerRow;

    @Min(value = 1, message = "Debe haber al menos 1 fila")
    @Max(value = 10, message = "Máximo 10 filas")
    private Integer maxRows;

    @DecimalMin(value = "20.0", message = "El ancho mínimo es 20")
    private Double sectionWidth;

    @DecimalMin(value = "20.0", message = "La altura mínima es 20")
    private Double sectionHeight;

    @DecimalMin(value = "30.0", message = "El espaciado X mínimo es 30")
    private Double spacingX;

    @DecimalMin(value = "30.0", message = "El espaciado Y mínimo es 30")
    private Double spacingY;

    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String description;
}