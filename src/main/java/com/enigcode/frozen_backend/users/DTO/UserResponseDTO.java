package com.enigcode.frozen_backend.users.DTO;

import com.enigcode.frozen_backend.users.model.Role;

public class UserResponseDTO {
    Long id;
    String username;
    String name;
    Role role;
    Boolean isActive;
}
