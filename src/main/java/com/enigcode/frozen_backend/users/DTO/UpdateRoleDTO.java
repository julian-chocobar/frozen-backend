package com.enigcode.frozen_backend.users.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRoleDTO {

    @NotNull(message = "Los roles son obligatorios")
    @Size(min = 1, message = "El usuario debe tener al menos un rol")
    private Set<String> roles;

}
