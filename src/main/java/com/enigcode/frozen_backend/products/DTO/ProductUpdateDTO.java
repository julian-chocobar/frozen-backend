package com.enigcode.frozen_backend.products.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateDTO {
    private String name;
    private Long packagingStandardId;
    private Boolean isAlcoholic;
}
