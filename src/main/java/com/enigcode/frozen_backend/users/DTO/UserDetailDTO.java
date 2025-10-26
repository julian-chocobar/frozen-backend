package com.enigcode.frozen_backend.users.DTO;

import java.time.OffsetDateTime;
import com.enigcode.frozen_backend.users.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailDTO {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String phoneNumber;
    private Role role;
    private OffsetDateTime creationDate;
    private OffsetDateTime lastLoginDate;
    private Boolean isActive;
}
