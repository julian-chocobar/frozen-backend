package com.enigcode.frozen_backend.users.DTO;

import com.enigcode.frozen_backend.users.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRoleDTO {

    @NotNull(message = "El rol es obligatorio")
    private Role role;

}
