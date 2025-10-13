package com.enigcode.frozen_backend.packagings.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackagingSimpleResponseDTO {
    private Long id;
    private String name;
}