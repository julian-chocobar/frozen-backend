package com.enigcode.frozen_backend.users.DTO;

import com.enigcode.frozen_backend.users.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String username;
    private String name;
    private Role role;
    private Boolean isActive;
}
