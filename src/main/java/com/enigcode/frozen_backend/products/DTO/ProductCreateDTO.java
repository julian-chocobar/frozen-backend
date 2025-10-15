package com.enigcode.frozen_backend.products.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateDTO {

    @NotNull(message = "Se debe asignar un empaque estandar al producto")
    private Long packagingStandardId;

    @NotNull(message = "Se debe asignar un nombre al producto")
    private String name;

    @NotNull(message = "Se debe asignar un alcoholismo al producto")
    private Boolean isAlcoholic;

}
