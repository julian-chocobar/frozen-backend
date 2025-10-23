package com.enigcode.frozen_backend.users.DTO;

import com.enigcode.frozen_backend.users.model.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateDTO {

    @NotNull(message = "Se debe asignar username al usuario")
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    private String username;

    @NotNull(message = "Se debe asignar password al usuario")
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$", message = "La contraseña debe contener al menos una letra mayúscula, una minúscula y un número")
    private String password;

    @NotNull(message = "Se debe asignar nombre al usuario")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String name;

    @NotNull(message = "Se debe asignar un rol al usuario")
    private Role role;

    @Email(message = "El email debe tener un formato válido")
    private String email;

    @Size(min = 7, max = 15, message = "El número de teléfono debe tener entre 7 y 15 caracteres")
    private String phoneNumber;

}
