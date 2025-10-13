package com.enigcode.frozen_backend.packagings.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackagingResponseDTO {
    private Long id;
    private String name;
    private Double quantity;
    private Boolean isActive;
}
