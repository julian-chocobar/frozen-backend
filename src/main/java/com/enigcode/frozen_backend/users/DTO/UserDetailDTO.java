package com.enigcode.frozen_backend.users.DTO;

import java.time.OffsetDateTime;
import com.enigcode.frozen_backend.users.model.Role;
import lombok.Data;

@Data
public class UserDetailDTO {
    Long id;
    String username;
    String name;
    String email;
    String phoneNumber;
    Role role;
    OffsetDateTime creationDate;
    OffsetDateTime lastLoginDate;
    Boolean isActive;
}
