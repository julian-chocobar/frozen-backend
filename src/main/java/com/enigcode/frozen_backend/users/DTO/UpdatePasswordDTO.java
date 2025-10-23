package com.enigcode.frozen_backend.users.DTO;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdatePasswordDTO {

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$", message = "La contraseña debe contener al menos una letra mayúscula, una minúscula y un número")
    private String password;

    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    private String passwordConfirmacion;

    public boolean isPasswordMatching() {
        return password != null && password.equals(passwordConfirmacion);
    }

}
