package com.enigcode.frozen_backend.users.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String username;
    private Set<String> roles;
    private String message;
}