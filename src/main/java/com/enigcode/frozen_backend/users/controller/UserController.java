package com.enigcode.frozen_backend.users.controller;

import com.enigcode.frozen_backend.users.DTO.UserCreateDTO;
import com.enigcode.frozen_backend.users.DTO.UserDetailDTO;
import com.enigcode.frozen_backend.users.DTO.UserResponseDTO;
import com.enigcode.frozen_backend.users.DTO.UserUpdateDTO;
import com.enigcode.frozen_backend.users.DTO.UpdatePasswordDTO;
import com.enigcode.frozen_backend.users.DTO.UpdateRoleDTO;
import com.enigcode.frozen_backend.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.web.PageableDefault;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    final UserService userService;

    @Operation(summary = "Crear un usuario", description = "Crea un nuevo usuario en el sistema")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        UserResponseDTO createdUser = userService.createUser(userCreateDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @Operation(summary = "Alternar estado usuario", description = "Alternar estado producto al contrario (activo, inactivo)")
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> toggleUser(@PathVariable Long id) {
        UserResponseDTO userResponseDTO = userService.toggleActive(id);
        return new ResponseEntity<>(userResponseDTO, HttpStatus.OK);
    }

    @Operation(summary = "Modificar usuario", description = "Modificar ciertos campos de un usuario especifico")
    @PatchMapping("/{id}")
    @PreAuthorize("@userSecurity.isSelf(#id) or hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        UserResponseDTO userResponseDTO = userService.updateUser(id, userUpdateDTO);
        return new ResponseEntity<>(userResponseDTO, HttpStatus.CREATED);
    }

    @Operation(summary = "Modificar los roles", description = "Modificar los roles de un usuario especifico")
    @PatchMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUserRole(@PathVariable Long id,
            @Valid @RequestBody UpdateRoleDTO updateRolDTO) {
        UserResponseDTO userResponseDTO = userService.updateUserRole(id, updateRolDTO);
        return new ResponseEntity<>(userResponseDTO, HttpStatus.CREATED);
    }

    @Operation(summary = "Modificar la contraseña", description = "Modificar la contraseña de un usuario especifico")
    @PatchMapping("/{id}/password")
    @PreAuthorize("@userSecurity.isSelf(#id) or hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUserPassword(@PathVariable Long id,
            @Valid @RequestBody UpdatePasswordDTO updatePasswordDTO) {
        if (!updatePasswordDTO.isPasswordMatching()) {
            return ResponseEntity.badRequest().build();
        }
        UserResponseDTO userResponseDTO = userService.updateUserPassword(id, updatePasswordDTO);
        return new ResponseEntity<>(userResponseDTO, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener usuario por ID", description = "Obtiene los detalles de un usuario especifico por su ID")
    @GetMapping("/{id}")
    @PreAuthorize("@userSecurity.isSelf(#id) or hasRole('ADMIN')")
    public ResponseEntity<UserDetailDTO> getUserById(@PathVariable Long id) {
        UserDetailDTO userDetailDTO = userService.getUserById(id);
        return new ResponseEntity<>(userDetailDTO, HttpStatus.OK);
    }

    @Operation(summary = "Obtener usuarios", description = "Obtiene todos los usuarios con paginación")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> findAll(
            @PageableDefault(size = 10, sort = "creationDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<UserResponseDTO> pageResponse = userService.findAll(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", pageResponse.getContent());
        response.put("currentPage", pageResponse.getNumber());
        response.put("totalItems", pageResponse.getTotalElements());
        response.put("totalPages", pageResponse.getTotalPages());
        response.put("size", pageResponse.getSize());
        response.put("hasNext", pageResponse.hasNext());
        response.put("hasPrevious", pageResponse.hasPrevious());
        response.put("isFirst", pageResponse.isFirst());
        response.put("isLast", pageResponse.isLast());

        return ResponseEntity.ok(response);
    }

}
