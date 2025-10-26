package com.enigcode.frozen_backend.users.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {

    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String name;

    @Email(message = "El email debe tener un formato válido")
    private String email;

    @Size(min = 7, max = 15, message = "El número de teléfono debe tener entre 7 y 15 caracteres")
    private String phoneNumber;

}
