package com.enigcode.frozen_backend.products.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductFilterDTO {
    private String name;
    private Boolean isActive;
    private Boolean isAlcoholic;
    private Boolean isReady;
}
