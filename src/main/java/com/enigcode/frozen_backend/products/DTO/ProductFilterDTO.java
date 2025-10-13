package com.enigcode.frozen_backend.products.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductFilterDTO {
    private String name;
    private String isActive;
    private String isAlcoholic;
}
