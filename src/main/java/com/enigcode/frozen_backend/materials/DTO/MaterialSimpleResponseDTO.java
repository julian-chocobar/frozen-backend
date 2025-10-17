package com.enigcode.frozen_backend.materials.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialSimpleResponseDTO {
    private Long id;
    private String code;
    private String name;
}