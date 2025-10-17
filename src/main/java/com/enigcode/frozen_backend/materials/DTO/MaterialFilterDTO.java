package com.enigcode.frozen_backend.materials.DTO;

import com.enigcode.frozen_backend.materials.model.MaterialType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialFilterDTO {
    private String name;
    private String supplier;
    private Boolean isActive;
    private MaterialType type;
}