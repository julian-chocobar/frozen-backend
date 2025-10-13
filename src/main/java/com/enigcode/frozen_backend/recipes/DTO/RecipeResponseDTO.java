package com.enigcode.frozen_backend.recipes.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeResponseDTO {
    private Long id;
    private String materialName;
    private String materialCode;
    private String materialUnit;
    private Double quantity;
}
