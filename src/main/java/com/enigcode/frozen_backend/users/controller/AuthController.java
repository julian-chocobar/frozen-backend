package com.enigcode.frozen_backend.users.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enigcode.frozen_backend.users.DTO.AuthResponseDTO;
import com.enigcode.frozen_backend.users.DTO.LoginRequestDTO;
import com.enigcode.frozen_backend.users.DTO.UserDetailDTO;
import com.enigcode.frozen_backend.users.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Obtener el usuario autenticado
        UserDetailDTO userDTO = userService.getUserByUsername(request.getUsername());

        return ResponseEntity.ok(AuthResponseDTO.builder()
                .token("SESSION")
                .username(userDTO.getUsername())
                .role(userDTO.getRole())
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Obtener la autenticación actual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            // Limpiar el contexto de seguridad
            SecurityContextHolder.clearContext();
        }

        return ResponseEntity.ok().build();
    }
}
